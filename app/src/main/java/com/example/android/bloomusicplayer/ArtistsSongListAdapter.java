package com.example.android.bloomusicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongsArray;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ArtistsSongListAdapter extends RecyclerView.Adapter<ArtistsSongListAdapter.ViewHolder> {
    private ArrayList<Song> mArtistSongsList;
    private RecyclerView mRecyclerView;
    private Context mContext;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ArtistsSongListAdapter(ArrayList<Song> myDataset, RecyclerView recyclerView, Context context) {
        mArtistSongsList = myDataset;
        mRecyclerView = recyclerView;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ArtistsSongListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artistsongs_listitem, parent, false);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                Song song = mArtistSongsList.get(itemPosition);

                Gson gson = new Gson();
                SongsArray songsArray = new SongsArray(mArtistSongsList);
                String songsListAsAString = gson.toJson(songsArray);

                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("songsList", songsListAsAString);
                intent.putExtra("position", mRecyclerView.getChildLayoutPosition(v));
                mContext.startActivity(intent);
            }
        });
        return new ViewHolder(linearLayout);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mSongName.setText(mArtistSongsList.get(position).getTitle());
        holder.mAlbumArt.setImageBitmap(mArtistSongsList.get(position).getAlbumart());
        holder.mSongName.setSelected(true);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mArtistSongsList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mSongName;
        public ImageView mAlbumArt;

        public ViewHolder(LinearLayout linearLayout) {
            super(linearLayout);
            mSongName = linearLayout.findViewById(R.id.card_view_image_title);
            mAlbumArt = linearLayout.findViewById(R.id.card_view_image);
        }
    }
}
