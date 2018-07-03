package com.example.android.bloomusicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongList;
import com.example.android.bloomusicplayer.model.SongsArray;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by 5biin-14 on 31/05/2018.
 */


public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.ViewHolder> {
    private ImageCache mAlbumCache;
    private Context mContext;
    private SongList mSongList;
    private ArrayList<Song> mSongs;

    private RecyclerView mRecyclerView;

    SongsListAdapter(Context context, RecyclerView recyclerView, SongList songList) {
        mContext = context;
        mRecyclerView = recyclerView;
        mSongList = songList;
        mSongs = mSongList.getSongs();

        mAlbumCache = new ImageCache();
    }

    private void loadBitmap(final Song mP, ImageView mImageView) {
        final String resId = String.valueOf(mP.getId());

        final Bitmap bitmap = mAlbumCache.getBitmapFromMemCache(resId);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageBitmap(null);
            new AsyncTask<ImageView, Void, Bitmap>() {
                private ImageView v;

                @Override
                protected Bitmap doInBackground(ImageView... params) {
                    v = params[0];
                    return mP.getThumbnail();
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    if (isCancelled()) {
                        result = null;
                    }
                    if (result != null) {
                        v.setImageBitmap(result);
                        mAlbumCache.addBitmapToMemoryCache(String.valueOf(resId), result);
                    } else {
                        Picasso.get().load(R.drawable.placeholder_coverart).into(v);
                    }

                }
            }.execute(mImageView);
        }
    }

    @NonNull
    @Override
    public SongsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi;
        vi = LayoutInflater.from(mContext);
        RelativeLayout relativeLayout = (RelativeLayout) vi.inflate(R.layout.songlistitem, parent, false);
        relativeLayout = relativeLayout.findViewById(R.id.songlistitem);

        relativeLayout.setOnClickListener(v -> {
            Gson gson = new Gson();

            TextView listitemposition = v.findViewById(R.id.listitemposition);
            int passposition = Integer.parseInt(listitemposition.getText().toString());

            SongsArray songsArray = new SongsArray(mSongList.getSongs());
            String songsListAsAString = gson.toJson(songsArray);

            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("songsList", songsListAsAString);
            intent.putExtra("position", passposition);
            mContext.startActivity(intent);
        });

        return new ViewHolder(relativeLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song p = mSongs.get(position);

        holder.title.setText(p.getTitle());
        holder.artist.setText(p.getArtist());
        holder.filepath.setText(p.getFilePath());
        holder.songid.setText(String.valueOf(p.getId()));
        holder.listitemposition.setText(String.valueOf(position));
        holder.albumart.setTag(position);
        holder.albumart.setImageBitmap(null);
        holder.albumart.setTransitionName("transition_coverart");

        final String resId = String.valueOf(p.getId());

        final Bitmap bitmap = mAlbumCache.getBitmapFromMemCache(resId);
        if (bitmap != null) {
            holder.albumart.setImageBitmap(bitmap);
        } else {
            loadBitmap(p, holder.albumart);
        }
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView albumart;
        public TextView title;
        public TextView artist;
        public TextView filepath;
        public TextView songid;
        public TextView listitemposition;


        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            artist = v.findViewById(R.id.artist);
            filepath = v.findViewById(R.id.filepath);
            songid = v.findViewById(R.id.songid);
            listitemposition = v.findViewById(R.id.listitemposition);
            albumart = v.findViewById(R.id.albumart);
        }
    }
}
