package com.example.android.bloomusicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boswelja.lastfm.Callback;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.artist.Image;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongList;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ViewHolder> {
    private LastFMRequest request;
    private ImageCache mArtistCache;
    private Context mContext;
    private SongList mSongList;
    private ArrayList<String> mArtists;

    private RecyclerView mRecyclerView;


    ArtistListAdapter(Context context, RecyclerView recyclerView, SongList songList) {
        mContext = context;
        mRecyclerView = recyclerView;
        mSongList = songList;
        mArtists = mSongList.getArtists();
        mArtistCache = new ImageCache();
        request = new LastFMRequest().setApiKey(mContext.getString(R.string.lastfm_api_key));
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }

    private void loadBitmap(final String mArtist, final ImageView v) {

        v.setImageBitmap(null);
        request.requestArtist()    //Returns a new ArtistTask for fetching info about an artist
                .withName(mArtist)    //Name of the artist you want
                .setCallback(new Callback<LastFMArtist>() {    //Lets you do something with the result
                    String result;

                    @Override
                    public void onDataRetrieved(LastFMArtist data) {
                        if (data.getArtist() != null) {
                            List<Image> imageList = data.getArtist().getImage();
                            result = imageList.get(1).getText();
                            Picasso.get().load(Uri.parse(result)).into(v);
                        }
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        onResultEmpty();
                    }

                    @Override
                    public void onResultEmpty() {
                        Drawable d = mContext.getResources().getDrawable(R.drawable.placeholder_artist);
                        v.setImageDrawable(d);
                    }
                })
                .build(); //Create and send the request
    }

    private String secondsToString(int pTime) {
        return String.format(Locale.ENGLISH, "%02d:%02d", pTime / 60, pTime % 60);
    }

    @NonNull
    @Override
    public ArtistListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi;
        vi = LayoutInflater.from(mContext);
        RelativeLayout relativeLayout = (RelativeLayout) vi.inflate(R.layout.artistlistitem, parent, false);
        relativeLayout = relativeLayout.findViewById(R.id.artistlistitem);
        relativeLayout.setOnClickListener(v -> {
            Gson gson = new Gson();
            Intent intent = new Intent(mContext, ArtistActivity.class);
            intent.putExtra("artist", mArtists.get(mRecyclerView.getChildLayoutPosition(v)));
            intent.putExtra("songlist", gson.toJson(mSongList));
            mContext.startActivity(intent);
        });

        return new ViewHolder(relativeLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String p = mArtists.get(position);
        holder.artist.setText(p);
        holder.artistImage.setTag(position);
        ArrayList<String> albums = mSongList.getAlbumsByArtist(p);

        ArrayList<Song> artistSongs = mSongList.getSongsByArtist(p);
        int totalDuration = 0;
        for (int i = 0; i < artistSongs.size(); i++) {
            totalDuration += artistSongs.get(i).getDurationSeconds();
        }

        int numAlbums = albums.size();
        String artistdescriptor = String.valueOf(numAlbums) + " album, " + secondsToString(totalDuration);
        holder.artistDescriptor.setText(artistdescriptor);

        Bitmap bitmap = mArtistCache.getBitmapFromMemCache(p);
        if (bitmap != null) {
            holder.artistImage.setImageBitmap(bitmap);
        } else {
            loadBitmap(p, holder.artistImage);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView artistImage;
        private TextView artist;
        private TextView artistDescriptor;

        ViewHolder(View v) {
            super(v);
            artist = v.findViewById(R.id.artist);
            artistDescriptor = v.findViewById(R.id.artistdescriptor);
            artistImage = v.findViewById(R.id.artistimage);
        }
    }
}
