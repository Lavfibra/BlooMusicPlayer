package com.example.android.bloomusicplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.boswelja.lastfm.Callback;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.artist.Image;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongList;
import com.example.android.bloomusicplayer.util.BlooColorUtil;
import com.example.android.bloomusicplayer.util.ColorUtil;
import com.google.gson.Gson;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.bloomusicplayer.util.BlooColorUtil.generatePalette;

public class ArtistActivity extends Activity {
    String mArtist;
    LastFMRequest mRequest;
    TextView mArtistAlbums;
    TextView mArtistSongNumber;
    TextView mArtistDuration;
    Toolbar mToolbar;
    ExpandableTextView mBiography;
    SongList mSongList;
    Gson gson;
    ArrayList<Song> mArtistSongs;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_artists);
        mToolbar = findViewById(R.id.artisttoolbar);
        mBiography = findViewById(R.id.biography);

        mRequest = new LastFMRequest().setApiKey(getResources().getString(R.string.lastfm_api_key));
        gson = new Gson();
        mArtist = getIntent().getStringExtra("artist");
        String str = getIntent().getStringExtra("songlist");
        mToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        mToolbar.setTitle(mArtist);
        setActionBar(mToolbar);

        mSongList = gson.fromJson(str, SongList.class);

        loadBitmap(mArtist, findViewById(R.id.artistactivityimage));
        mArtistAlbums = findViewById(R.id.artistalbums);
        mArtistSongNumber = findViewById(R.id.artistsongs);
        mArtistDuration = findViewById(R.id.artistduration);

        str = "Album: ";
        str += String.valueOf(mSongList.getAlbumsByArtist(mArtist).size());
        mArtistAlbums.setText(str);
        str = "Brani: ";
        str += String.valueOf(mSongList.getSongsByArtist(mArtist).size());
        mArtistSongNumber.setText(str);

        mArtistSongs = mSongList.getSongsByArtist(mArtist);
        int totalDuration = 0;
        for (int i = 0; i < mArtistSongs.size(); i++) {
            totalDuration += mArtistSongs.get(i).getDurationSeconds();
        }
        str = secondsToString(totalDuration);
        mArtistDuration.setText(str);

        mRecyclerView = findViewById(R.id.artistsonglist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ArtistsSongListAdapter(mArtistSongs, mRecyclerView, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void loadBitmap(final String mArtist, final ImageView v) {
        v.setImageBitmap(null);
        mRequest.requestArtist()    //Returns a new ArtistTask for fetching info about an artist
                .withName(mArtist)    //Name of the artist you want
                .setCallback(new Callback<LastFMArtist>() {    //Lets you do something with the result
                    String result;

                    @Override
                    public void onDataRetrieved(LastFMArtist data) {
                        List<Image> imageList = data.getArtist().getImage();
                        String bio = data.getArtist().getBio().getContent();
                        bio = bio.replace(". ", ".<br>");
                        mBiography.setText(Html.fromHtml(bio));

                        result = imageList.get(3).getText();
                        if (result != null) {
                            try {
                                Picasso.get().load(Uri.parse(result)).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        v.setImageBitmap(bitmap);
                                        Palette palette = generatePalette(bitmap);
                                        RelativeLayout artistBackground = findViewById(R.id.artistBackground);
                                        int color = BlooColorUtil.getColor(palette, getResources().getColor(R.color.colorAccent));
                                        artistBackground.setBackgroundColor(color);
                                        mToolbar.setBackgroundColor(color);
                                        Window window = ArtistActivity.this.getWindow();
                                        window.setStatusBarColor(ColorUtil.darkenColor(color));
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    }
                                });
                            } catch (Exception e) {
                                onResultEmpty();
                            }
                        } else {
                            onResultEmpty();
                        }
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        onResultEmpty();
                    }

                    @Override
                    public void onResultEmpty() {
                        Drawable d = getApplicationContext().getResources().getDrawable(R.drawable.placeholder_artist);
                        v.setImageDrawable(d);
                    }
                })
                .build(); //Create and send the request
    }

    private String secondsToString(int pTime) {
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }
}
