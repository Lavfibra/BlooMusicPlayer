package com.example.android.bloomusicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import com.example.android.bloomusicplayer.model.SongList;

import java.util.ArrayList;

public class ArtistTask extends AsyncTask<Void, String, SongList> {
    private Context mContext;
    private SongList mSongList;
    private View mRootView;


    ArtistTask(Context context, View rootView, SongList songList) {
        mContext = context;
        mRootView = rootView;
        mSongList = songList;
    }

    @Override
    protected SongList doInBackground(Void... params) {
        mSongList.scanSongs(mContext, "external");
        return mSongList;
    }

    @Override
    protected void onPostExecute(SongList songList) {
        ArrayList<String> artists = mSongList.getArtists();
        ListView artistListView = mRootView.findViewById(R.id.artistsview);
        // get data from the table by the ListAdapter
        ArtistListAdapter customAdapter = new ArtistListAdapter(mContext, R.layout.artistlistitem, artists, mSongList) {
        };

        artistListView.setAdapter(customAdapter);
    }
}
