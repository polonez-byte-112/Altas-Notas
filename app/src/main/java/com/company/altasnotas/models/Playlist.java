package com.company.altasnotas.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class Playlist implements Parcelable {
    private String title;
    private String description;
    private String image_id;
    private String year;
    private boolean isAlbum;
    private ArrayList <Song> songs;

    public Playlist(){

    }


    protected Playlist(Parcel in) {
        title = in.readString();
        description = in.readString();
        image_id = in.readString();
        year = in.readString();
        isAlbum = in.readByte() != 0;
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }


    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public void setAlbum(boolean album) {
        this.isAlbum = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(image_id);
        dest.writeString(year);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(isAlbum);
        }

        dest.writeParcelableArray(songs.toArray(new Song[0]), flags);
    }


    public class MyCreator implements Parcelable.Creator<Playlist> {

        @Override
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    }
}
