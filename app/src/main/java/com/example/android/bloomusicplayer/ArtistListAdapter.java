package com.example.android.bloomusicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boswelja.lastfm.Callback;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.artist.Image;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistListAdapter extends ArrayAdapter<String> {
    LastFMRequest request;
    private ImageCache mArtistCache;
    private Context mContext;
    private SongList mSongList;


    public ArtistListAdapter(Context mContext, int artistListItem, ArrayList<String> artists, SongList songList) {
        super(mContext, artistListItem, artists);
        this.mContext = mContext;
        mSongList = songList;
        mArtistCache = new ImageCache();
        request = new LastFMRequest().setApiKey(getContext().getString(R.string.lastfm_api_key));
    }

    @Override
    public int getCount() {
        return mSongList.getArtists().size();
    }

    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        ArtistItemHolder artistItemHolder;


        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.artistlistitem, null);

            artistItemHolder = new ArtistItemHolder();
            artistItemHolder.artist = v.findViewById(R.id.artist);
            artistItemHolder.artistDescriptor = v.findViewById(R.id.artistdescriptor);
            artistItemHolder.artistImage = v.findViewById(R.id.artistimage);
            v.setTag(artistItemHolder);
        } else {
            artistItemHolder = (ArtistItemHolder) v.getTag();
        }

        final String p = getItem(position);

        if (p != null) {
            artistItemHolder.artist.setText(p);
            artistItemHolder.artistImage.setTag(position);
            ArrayList<String> albums = mSongList.getAlbumsByArtist(p);

            ArrayList<Song> artistSongs = mSongList.getSongsByArtist(p);
            int totalDuration = 0;
            for (int i = 0; i < artistSongs.size(); i++) {
                totalDuration += artistSongs.get(i).getDurationSeconds();
            }

            int numAlbums = albums.size();
            String artistdescriptor = String.valueOf(numAlbums) + " album, " + secondsToString(totalDuration);
            artistItemHolder.artistDescriptor.setText(artistdescriptor);
            final String resId = String.valueOf(position);

            Bitmap bitmap = mArtistCache.getBitmapFromMemCache(p);
            if (bitmap != null) {
                artistItemHolder.artistImage.setImageBitmap(bitmap);
            } else {
                loadBitmap(p, artistItemHolder.artistImage);
            }
        }
        return v;
    }

    public void loadBitmap(final String mArtist, final ImageView v) {

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

                            if (result != null && (Integer) v.getTag() == getPosition(mArtist)) {
                                try {
                                    Picasso.get().load(Uri.parse(result)).into(v);
                                } catch (Exception e) {
                                    onResultEmpty();
                                }

                            } else if ((Integer) v.getTag() == getPosition(mArtist)) {
                                onResultEmpty();
                            }
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
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    static class ArtistItemHolder {
        private CircleImageView artistImage;
        private TextView artist;
        private TextView artistDescriptor;
    }
}
