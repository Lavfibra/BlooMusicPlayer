package com.example.android.bloomusicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bloomusicplayer.model.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by 5biin-14 on 31/05/2018.
 */


public class SongsListAdapter extends ArrayAdapter<Song> {
    private ImageCache mAlbumCache;
    private Context mContext;

    SongsListAdapter(Context mContext, int songlistitem, ArrayList<Song> songs) {
        super(mContext, songlistitem, songs);
        this.mContext = mContext;
        mAlbumCache = new ImageCache();
    }

    @SuppressLint({"InflateParams", "StaticFieldLeak"})
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        SongItemHolder songItemHolder;


        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.songlistitem, null);

            songItemHolder = new SongItemHolder();
            songItemHolder.title = v.findViewById(R.id.title);
            songItemHolder.artist = v.findViewById(R.id.artist);
            songItemHolder.filepath = v.findViewById(R.id.filepath);
            songItemHolder.songid = v.findViewById(R.id.songid);
            songItemHolder.listitemposition = v.findViewById(R.id.listitemposition);
            songItemHolder.albumart = v.findViewById(R.id.albumart);
            songItemHolder.albumart.setTransitionName("transition_coverart");
            v.setTag(songItemHolder);
        } else {
            songItemHolder = (SongItemHolder) v.getTag();
        }

        final Song p = getItem(position);

        if (p != null) {
            songItemHolder.title.setText(p.getTitle());
            songItemHolder.artist.setText(p.getArtist());
            songItemHolder.filepath.setText(p.getFilePath());
            songItemHolder.songid.setText(String.valueOf(p.getId()));
            songItemHolder.listitemposition.setText(String.valueOf(position));
            songItemHolder.albumart.setTag(position);
            songItemHolder.albumart.setImageBitmap(null);
            songItemHolder.albumart.setTransitionName("transition_coverart");

            final String resId = String.valueOf(p.getId());

            final Bitmap bitmap = mAlbumCache.getBitmapFromMemCache(resId);
            if (bitmap != null) {
                songItemHolder.albumart.setImageBitmap(bitmap);
            } else {
                loadBitmap(p, songItemHolder.albumart, position);
            }
        }
        return v;
    }

    public void loadBitmap(final Song mP, ImageView mImageView, final int mPosition) {
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
                    if (result != null && (Integer) v.getTag() == getPosition(mP)) {
                        v.setImageBitmap(result);
                        mAlbumCache.addBitmapToMemoryCache(String.valueOf(resId), result);
                    } else if ((Integer) v.getTag() == getPosition(mP)) {
                        Picasso.get().load(R.drawable.placeholder_coverart).into(v);
                    }

                }
            }.execute(mImageView);
        }
    }

    static class SongItemHolder {
        public ImageView albumart;
        public TextView title;
        public TextView artist;
        public TextView filepath;
        public TextView songid;
        public TextView listitemposition;


    }
}
