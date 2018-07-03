package com.example.android.bloomusicplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bloomusicplayer.model.SongList;
import com.google.gson.Gson;

public class SongsFragment extends Fragment {
    public SongList mSongList;
    Gson gson;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    static SongsFragment newInstance(SongList songList) {
        SongsFragment songsFragment = new SongsFragment();
        Bundle args = new Bundle();
        Gson gson = new Gson();
        String songAsAString = gson.toJson(songList);
        args.putString("songList", songAsAString);
        songsFragment.setArguments(args);
        return songsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String songsAsAString = getArguments().getString("songList");
        gson = new Gson();
        mSongList = gson.fromJson(songsAsAString, SongList.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.songs_fragment, container, false);
        mRecyclerView = v.findViewById(R.id.songslist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SongsListAdapter(getContext(), mRecyclerView, mSongList);
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

}