package com.example.android.bloomusicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongList;

import java.util.ArrayList;

/**
 * Created by 5biin-14 on 31/05/2018.
 */

public class SongTask extends AsyncTask<Void, String, SongList> {
    private Context mContext;
    private SongList mSongList;
    private View mRootView;


    SongTask(Context context, View rootView, SongList songList) {
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
        ArrayList<Song> songs = mSongList.getSongs();
        ListView yourListView = mRootView.findViewById(R.id.songslist);
        // get data from the table by the ListAdapter
        SongsListAdapter customAdapter = new SongsListAdapter(mContext, R.layout.songlistitem, songs) {
        };

        yourListView.setAdapter(customAdapter);
    }
}
