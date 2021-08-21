package com.company.altasnotas.viewmodels.fragments.profile;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.models.User;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragmentViewModel extends ViewModel {
    private  StorageReference storageReference;
    private FirebaseAuth mAuth;
    private DatabaseReference database_ref;
    public String uid;
    private   ArrayList<String> keys;
    private Integer[] x={0};

    private MutableLiveData<Integer> _shouldDeleteUser = new MutableLiveData<>();
    public LiveData<Integer> getShouldDeleteUser(){
        return  _shouldDeleteUser;
    }


    /**
     * Two functions below are copied from : https://programming.vip/docs/android-uri-to-bitmap-image-and-compress.html
     * It helps me compress the photo to 5 times worse quality than the original (5MB -> 1MB)
     */
    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //Image resolution is based on 480x800
        float hh = 800f;//The height is set as 800f here
        float ww = 480f;//Set the width here to 480f
        //Zoom ratio. Because it is a fixed scale, only one data of height or width is used for calculation
        int be = 1;//be=1 means no scaling
        if (originalWidth > originalHeight && originalWidth > ww) {//If the width is large, scale according to the fixed size of the width
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//If the height is high, scale according to the fixed size of the width
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //Proportional compression
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//Set scaling
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//Mass compression again
    }
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//Quality compression method, here 100 means no compression, store the compressed data in the BIOS
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //Cycle to determine if the compressed image is greater than 100kb, greater than continue compression
            baos.reset();

            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//Here, the compression options are used to store the compressed data in the BIOS
            if (options >= 30) {
                options -= 10;
            } else {
                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//Generate image from ByteArrayInputStream data
                return bitmap;
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public void deleteProfile(){
        _shouldDeleteUser.setValue(0);
        initializeFirebase();

        uid = mAuth.getCurrentUser().getUid();
         keys = new ArrayList<>();




        /*
        We need to delete
        - Playlists data   + img
        - Profile data +img
        - Fav music
        */

       deleteImgProfile();

        deletePlaylists();
        deleteFavMusic();

       _shouldDeleteUser.setValue(1);
    }

    private void deleteFavMusic() {
        database_ref.child("fav_music").child(uid).removeValue();
    }

    private void deletePlaylists() {
        database_ref.child("music").child("playlists").child(uid).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){
                    keys.add(ds.getKey());
                }

                if(keys.size() == snapshot.getChildrenCount()){
                    for(String key: keys){
                        storageReference.child("images/playlists/"+uid+"/"+ key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                x[0]++;
                                System.out.println("Playlist img deleted, X: "+x[0]+", "+keys.size());

                                if(x[0]==keys.size()){
                                    database_ref.child("music").child("playlists").child(uid).removeValue();
                                }
                            }
                        });



                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initializeFirebase() {
        mAuth= FirebaseAuth.getInstance();
        database_ref = FirebaseDatabase.getInstance().getReference();
        storageReference= FirebaseStorage.getInstance().getReference();
    }

    private void deleteImgProfile() {
        storageReference.child("images").child("profiles").child(mAuth.getCurrentUser().getUid()).delete();
    }

    public void downloadProfile(MainActivity mainActivity, FirebaseAuth mAuth, DatabaseReference database_ref,  TextView profile_name, TextView profile_email, CircleImageView profile_img, TextView creationText, TextView creationDate) {

        User localUser = new User();
        if (mAuth.getCurrentUser() != null) {
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        localUser.name = snapshot.child("name").getValue().toString();
                        localUser.mail = mAuth.getCurrentUser().getEmail();
                        localUser.photoUrl = snapshot.child("photoUrl").getValue().toString();
                        profile_email.setText(localUser.mail);
                        profile_name.setText(localUser.name);

                        creationText.setVisibility(View.VISIBLE);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy ");
                        Long creationDateLong =mAuth.getCurrentUser().getMetadata().getCreationTimestamp();
                        Date date = new Date( creationDateLong);
                        creationDate.setVisibility(View.VISIBLE);
                        creationDate.setText(formatter.format(date));

                    Glide.with(mainActivity).load(MainActivity.viewModel.getPhotoUrl().getValue()).error(R.drawable.img_not_found).into(profile_img);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(MainActivity.FIREBASE + error.getMessage(), "Firebase error: "+error.getMessage());
                }
            });

        }
    }

    public void updateProfile(FirebaseAuth mAuth, DatabaseReference database_ref, TextView profile_name) {


        User localUser = new User();
        if (mAuth.getCurrentUser() != null) {
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        localUser.name = snapshot.child("name").getValue().toString();
                        localUser.mail = mAuth.getCurrentUser().getEmail();
                        localUser.photoUrl = snapshot.child("photoUrl").getValue().toString();
                        localUser.login_method = Integer.parseInt(snapshot.child("login_method").getValue().toString());


                        //After we download data from db, We update its according to Inputed Data


                        localUser.name = profile_name.getText().toString();
                        database_ref.child("users").child(mAuth.getCurrentUser().getUid()).setValue(localUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}
