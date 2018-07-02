package com.example.android.bloomusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bloomusicplayer.model.SongList;
import com.google.gson.Gson;

public class ArtistsFragment extends Fragment {
    public SongList mSongList;
    ListView mArtistsListView;
    Gson gson;

    static ArtistsFragment newInstance(SongList songList) {
        ArtistsFragment artistsFragment = new ArtistsFragment();
        Bundle args = new Bundle();
        Gson gson = new Gson();
        String songAsAString = gson.toJson(songList);
        args.putString("songList", songAsAString);
        artistsFragment.setArguments(args);
        return artistsFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.artists_fragment, container, false);
        mArtistsListView = v.findViewById(R.id.artistsview);
        return v;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String songsAsAString = getArguments().getString("songList");
        gson = new Gson();
        mSongList = gson.fromJson(songsAsAString, SongList.class);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArtistTask artistTask = new ArtistTask(getContext(), view, mSongList);
        artistTask.execute();

        mArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView artist = view.findViewById(R.id.artist);

                Intent intent = new Intent(getContext(), ArtistActivity.class);
                intent.putExtra("artist", artist.getText().toString());
                intent.putExtra("songlist", gson.toJson(mSongList));
                startActivity(intent);
            }
        });
    }
}
