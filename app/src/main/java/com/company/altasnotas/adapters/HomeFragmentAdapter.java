package com.company.altasnotas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;

import java.util.ArrayList;

public class HomeFragmentAdapter extends RecyclerView.Adapter<HomeFragmentAdapter.MyViewHolder> {
    MainActivity mainActivity;
    ArrayList<Playlist> playlists;
    ArrayList<String> authors;
    ArrayList<String> albums;

    public HomeFragmentAdapter(MainActivity mainActivity, ArrayList<String> authors, ArrayList<String> albums, ArrayList<Playlist> playlists) {
        this.mainActivity = mainActivity;
        this.authors = authors;
        this.albums = albums;
        this.playlists = playlists;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeFragmentAdapter
                .MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_home_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        updateDefaultData(holder,position);

        holder.home_row_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left)
                        .replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(authors.get(position), albums.get(position), playlists.get(position), 1))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void updateDefaultData(MyViewHolder holder, int position) {
        Glide.with(mainActivity.getApplicationContext()).load(playlists.get(position).getImage_id()).apply(RequestOptions.centerCropTransform()).error(R.drawable.img_not_found).into(holder.home_row_img);
        holder.home_row_year.setText(playlists.get(position).getYear());
        holder.home_row_title.setText(playlists.get(position).getTitle());
        holder.home_row_author.setText(playlists.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView home_row_img;
        TextView home_row_year, home_row_author;
        AppCompatTextView home_row_title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            home_row_img = itemView.findViewById(R.id.playlistHomeRowImg);
            home_row_year = itemView.findViewById(R.id.playlistHomeRowYear);
            home_row_title = itemView.findViewById(R.id.playlistHomeRowTitle);
            home_row_author = itemView.findViewById(R.id.playlistHomeRowDescription);
        }
    }
}
