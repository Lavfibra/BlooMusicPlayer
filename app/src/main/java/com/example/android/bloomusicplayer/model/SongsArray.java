package com.example.android.bloomusicplayer.model;

import java.util.ArrayList;

public class SongsArray {
    private ArrayList<Song> mSongs;

    public SongsArray(ArrayList<Song> mSongs) {
        this.mSongs = mSongs;
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }
}
