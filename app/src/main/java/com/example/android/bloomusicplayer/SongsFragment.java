package com.example.android.bloomusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bloomusicplayer.model.SongList;
import com.example.android.bloomusicplayer.model.SongsArray;
import com.google.gson.Gson;

public class SongsFragment extends Fragment {
    public SongList mSongList;
    ListView mSongsListView;

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
        assert getArguments() != null;
        String songsAsAString = getArguments().getString("songList");
        Gson gson = new Gson();
        mSongList = gson.fromJson(songsAsAString, SongList.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.songs_fragment, container, false);
        mSongsListView = v.findViewById(R.id.songslist);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SongTask songTask = new SongTask(getContext(), view, mSongList);
        songTask.execute();

        mSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView songid = view.findViewById(R.id.songid);
                TextView listitemposition = view.findViewById(R.id.listitemposition);
                int passposition = Integer.parseInt(listitemposition.getText().toString());

                long id = Long.parseLong(songid.getText().toString());

                Gson gson = new Gson();
                SongsArray songsArray = new SongsArray(mSongList.getSongs());
                String songsListAsAString = gson.toJson(songsArray);

                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra("songsList", songsListAsAString);
                intent.putExtra("position", passposition);
                startActivity(intent);

            }
        });
    }

}