package com.example.android.bloomusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.boswelja.lastfm.Callback;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.artist.Image;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.example.android.bloomusicplayer.MusicService.LocalBinder;
import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongsArray;
import com.example.android.bloomusicplayer.util.BlooColorUtil;
import com.example.android.bloomusicplayer.util.ColorUtil;
import com.example.android.bloomusicplayer.views.CoverArtViewPager;
import com.example.android.bloomusicplayer.views.SquareImageView;
import com.google.gson.Gson;
import com.ohoussein.playpause.PlayPauseView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class PlayerActivity extends AppCompatActivity {
    public BroadcastReceiver mBroadcastReceiver;
    public ServiceManager mMusicService = new ServiceManager(this);
    public int position;
    public String path;
    public ArrayList<Song> mSongs;
    public SeekBar mSeekBar;
    public Song mSong;
    public CoverArtViewPager vpPager;
    public String mLoopType = "no";
    MusicService musicService;
    boolean mBound = false;
    TextView endPosition;
    TextView startPosition;
    TextView playerTitle;
    TextView playerArtist;
    CardView playerCard;
    RelativeLayout playerBackground;
    PlayPauseView playPauseView;
    LastFMRequest request;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            musicService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    private LocalBroadcastManager broadcaster;

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
        // Bind to LocalService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        broadcaster = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION.NEXT_ACTION);
        intentFilter.addAction(Constants.ACTION.PREV_ACTION);
        intentFilter.addAction(Constants.ACTION.PLAY_ACTION);
        intentFilter.addAction(Constants.ACTION.SEEKTO_ACTION);
        intentFilter.addAction(Constants.ACTION.UPDATE_SONG);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), intentFilter);

        mMusicService.updateSong();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mMusicService.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        resizeStatusBar();
        request = new LastFMRequest().setApiKey(getApplicationContext().getString(R.string.lastfm_api_key));

        Gson gson = new Gson();
        String songsListAsAString = getIntent().getStringExtra("songsList");
        SongsArray mSongsArray = gson.fromJson(songsListAsAString, SongsArray.class);
        mSongs = mSongsArray.getSongs();
        position = getIntent().getIntExtra("position", -1);
        mSong = mSongs.get(position);

        mSeekBar = findViewById(R.id.seekbar);
        startPosition = findViewById(R.id.startposition);
        endPosition = findViewById(R.id.endposition);
        playerTitle = findViewById(R.id.playertitle);
        playerArtist = findViewById(R.id.playerartist);
        playerCard = findViewById(R.id.playercard);
        playerBackground = findViewById(R.id.playerBackground);

        broadcaster = LocalBroadcastManager.getInstance(this);
        vpPager = findViewById(R.id.vpPager);
        vpPager.setAdapter(new CoverStatePagerAdapter(getSupportFragmentManager(), mSongs));
        vpPager.setCurrentItem(position, true);
        vpPager.setOffscreenPageLimit(2);
        /*vpPager.post(new Runnable(){
            @Override
            public void run() {
                addOnPageChangeListener.onPageSelected(viewPager.getCurrentItem());
            }
        });*/
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int previous;
            private int previousState;
            private boolean userScrollChange;

            @Override
            public void onPageScrolled(int mposition, float positionOffset, int positionOffsetPixels) {
                previous = position;
            }

            @Override
            public void onPageSelected(int mPosition) {
                if (userScrollChange) {
                    if (mPosition > previous) {
                        Intent nextIntent = new Intent(getApplicationContext(), MusicService.class);
                        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
                        nextIntent.putExtra("user", "fromActivity");
                        broadcaster.sendBroadcast(nextIntent);
                    } else if (mPosition < previous) {
                        Intent previousIntent = new Intent(getApplicationContext(), MusicService.class);
                        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
                        previousIntent.putExtra("user", "fromActivity");
                        broadcaster.sendBroadcast(previousIntent);
                    }
                }
                Toast.makeText(PlayerActivity.this, "Funziono", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING)
                    userScrollChange = true;

                else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE)
                    userScrollChange = false;

                previousState = state;
            }
        });

        playPauseView = findViewById(R.id.play_pause_view);
        final EqualizerView equalizerView = findViewById(R.id.equalizer);
        equalizerView.animateBars();
        playPauseView.toggle();
        playPauseView.setOnClickListener(v -> {
            if (!playPauseView.isPlay()) {
                equalizerView.stopBars();
                playPauseView.toggle();
                mMusicService.pause();
            } else {
                equalizerView.animateBars();
                playPauseView.toggle();
                mMusicService.start();
            }
        });

        final ImageButton repeat = findViewById(R.id.repeat);
        mLoopType = "all";
        repeat.setAlpha((float) 1);
        repeat.setOnClickListener(v -> {
            switch (mLoopType) {
                case "no":
                    mLoopType = "all";
                    mMusicService.setLooping(mLoopType);
                    repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_repeat_24px));
                    repeat.setAlpha((float) 1);
                    break;
                case "all":
                    mLoopType = "self";
                    mMusicService.setLooping(mLoopType);
                    repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_repeat_one_24px));
                    repeat.setAlpha((float) 1);
                    break;
                case "self":
                    mLoopType = "no";
                    mMusicService.setLooping(mLoopType);
                    repeat.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_repeat_24px));
                    repeat.setAlpha((float) 0.50);
                    break;
            }
            mMusicService.setLooping(mLoopType);
        });

        final ImageButton lyrics = findViewById(R.id.lyrics);
        PackageManager pm = getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo("com.geecko.QuickLyric", PackageManager.GET_ACTIVITIES);
            lyrics.setOnClickListener(v -> {
                startActivity(new Intent("com.geecko.QuickLyric.getLyrics").putExtra("TAGS", new String[]{mSong.getArtist(), mSong.getTitle()}));
            });
        } catch (PackageManager.NameNotFoundException ignored) {
            lyrics.setOnClickListener(v -> {
                Toast.makeText(this, "Installa QuickLyrics dal Playstore per avere accesso ai testi delle canzoni", Toast.LENGTH_SHORT).show();
            });
        }

        final ImageButton previous = findViewById(R.id.previous);
        previous.setOnClickListener(v -> previous("fromActivity"));

        final ImageButton next = findViewById(R.id.next);
        next.setOnClickListener(v -> next("fromActivity"));


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMusicService.seekTo(progress);
                }
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String user = intent.getStringExtra("user");
                String action = intent.getAction();
                switch (action) {
                    case Constants.ACTION.NEXT_ACTION:
                        next(user);
                        if (playPauseView.isPlay()) {
                            playPauseView.toggle();
                            equalizerView.animateBars();
                        }
                        break;
                    case Constants.ACTION.PREV_ACTION:
                        previous(user);
                        if (playPauseView.isPlay()) {
                            playPauseView.toggle();
                            equalizerView.animateBars();
                        }
                        break;
                    case Constants.ACTION.PLAY_ACTION:
                        String type = intent.getStringExtra("type");
                        if (type.equals("start")) {
                            if (playPauseView.isPlay()) {
                                playPauseView.toggle();
                            }
                            equalizerView.animateBars();
                        } else if (type.equals("pause")) {
                            if (!playPauseView.isPlay()) {
                                playPauseView.toggle();
                            }
                            equalizerView.stopBars();
                        }
                        break;
                    case Constants.ACTION.SEEKTO_ACTION:
                        int dur1 = intent.getIntExtra("progress", 0);
                        mSeekBar.setProgress(dur1);
                        int mns1 = dur1 / 60000 % 60000;
                        int scs1 = dur1 % 60000 / 1000;
                        String progress = String.format(Locale.ITALY, "%02d:%02d", mns1, scs1);
                        startPosition.setText(progress);
                        break;
                    case Constants.ACTION.UPDATE_SONG:
                        position = intent.getIntExtra("position", -100);
                        mSong = mSongs.get(position);
                        setupViews();
                        break;
                }
            }
        };


        mMusicService.stop();
        //START SERVICE
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("song", gson.toJson(mSong));
        intent.putExtra("songList", gson.toJson(mSongsArray));
        intent.putExtra("position", position);
        intent.putExtra("user", "fromActivity");
        intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(intent);

        setupViews();
    }

    private void previous(String user) {
        if (mLoopType.equals("all")) {
            position--;
            if (position <= -1) {
                position = mSongs.size() - 1;
            }

            mSong = mSongs.get(position);
            setupViews();
            if (user.equals("fromActivity")) {
                mMusicService.previous();
            }
        }

    }

    private void next(String user) {
        if (mLoopType.equals("all")) {
            position++;
            if (position >= mSongs.size()) {
                position = 0;
            }
            mSong = mSongs.get(position);
            setupViews();
            if (user.equals("fromActivity")) {
                mMusicService.next();
            }
        }
    }

    private void setupViews() {

        vpPager.setCurrentItem(position, true);
        position = mSongs.indexOf(mSong);
        mSeekBar.setProgress(0);

        playerTitle.setText(mSong.getTitle());
        playerTitle.setSelected(true);
        playerArtist.setText(mSong.getArtist());

        Bitmap bm = mSong.getAlbumart();
        if (bm != null)
            generateLayoutPalette(bm);
        else
            generateLayoutPaletteNoCover();

        if (playPauseView.isPlay())
            playPauseView.toggle();

        int dur = (int) mSong.getDuration();
        mSeekBar.setMax(dur);
        //convert the song duration into string reading hours, mins seconds
        int mns = dur / 60000 % 60000;
        int scs = dur % 60000 / 1000;
        String songTime = String.format(Locale.ITALY, "%02d:%02d", mns, scs);
        startPosition.setText("00:00");
        endPosition.setText(songTime);
        loadBitmap(mSong.getArtist(), findViewById(R.id.artistplayerimage));
    }

    public void loadBitmap(final String mArtist, final ImageView v) {
        v.setImageBitmap(null);
        request.requestArtist()    //Returns a new ArtistTask for fetching info about an artist
                .withName(mArtist)    //Name of the artist you want
                .setCallback(new Callback<LastFMArtist>() {    //Lets you do something with the result
                    String result;

                    @Override
                    public void onDataRetrieved(LastFMArtist data) {
                        List<Image> imageList = data.getArtist().getImage();
                        result = imageList.get(3).getText();

                        if (result != null) {
                            try {
                                Picasso.get().load(Uri.parse(result)).into(v);
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

    private void resizeStatusBar() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        View transparencystatusbar = findViewById(R.id.transparencystatusbar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) transparencystatusbar.getLayoutParams();
        params.height = statusBarHeight;
        transparencystatusbar.setLayoutParams(params);
        transparencystatusbar.bringToFront();
    }

    public void generateLayoutPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(p -> {
            LinearLayout ll = findViewById(R.id.playerLabel);
            int color = BlooColorUtil.getColor(p, getResources().getColor(R.color.colorAccent));
            ll.setBackgroundColor(BlooColorUtil.shiftBackgroundColorForLightText(color));
            playerBackground.setBackgroundColor(BlooColorUtil.shiftBackgroundColorForLightText(color));
            playerTitle.setTextColor(BlooColorUtil.shiftBackgroundColorForLightText(color));
            playerArtist.setTextColor(BlooColorUtil.shiftBackgroundColorForLightText(color));
            CardView progressCard = findViewById(R.id.progresscard);
            progressCard.setCardBackgroundColor(ColorUtil.darkenColor(color));

            color = p.getDominantColor(getResources().getColor(R.color.cardview_dark_background));
            playerCard.setCardBackgroundColor(BlooColorUtil.shiftBackgroundColorForDarkText(color));
            LinearLayout labelBackground = findViewById(R.id.labelBackground);

            labelBackground.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));
            mSeekBar.getProgressDrawable().setColorFilter(BlooColorUtil.shiftBackgroundColorForDarkText(color), PorterDuff.Mode.MULTIPLY);
        });
    }

    private void generateLayoutPaletteNoCover() {
        LinearLayout ll = findViewById(R.id.playerLabel);
        int color = ContextCompat.getColor(PlayerActivity.this, R.color.cardview_dark_background);
        ll.setBackgroundColor(color);
        SeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.cardview_light_background), PorterDuff.Mode.MULTIPLY);

    }

    public static class CoverFragment extends Fragment {
        public Song mSong;

        static CoverFragment newInstance(Song song) {
            CoverFragment coverFragment = new CoverFragment();
            Bundle args = new Bundle();
            Gson gson = new Gson();
            String songAsAString = gson.toJson(song);
            args.putString("song", songAsAString);
            coverFragment.setArguments(args);
            return coverFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String songsAsAString = getArguments().getString("song");
            Gson gson = new Gson();
            mSong = gson.fromJson(songsAsAString, Song.class);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.playerpagercoverart, container, false);
            SquareImageView coverArt = v.findViewById(R.id.playercoverart);
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    return mSong.getAlbumart();
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    coverArt.setImageBitmap(bitmap);
                }
            }.execute();
            return v;
        }

    }

    public class CoverStatePagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Song> mSongs;

        public CoverStatePagerAdapter(FragmentManager fragmentManager, ArrayList<Song> songList) {
            super(fragmentManager);
            mSongs = songList;
        }

        @Override
        public int getCount() {
            return mSongs.size();
        }

        @Override
        public Fragment getItem(int position) {
            Song song = mSongs.get(position);
            return CoverFragment.newInstance(song);
        }
    }

    public class ServiceManager {
        Context mContext;

        ServiceManager(Context context) {
            mContext = context;
        }

        public void setLooping(String looping) {
            Intent loopingIntent = new Intent(mContext, MusicService.class);
            loopingIntent.setAction(Constants.ACTION.SETLOOPING_ACTION);
            loopingIntent.putExtra("type", looping);
            loopingIntent.putExtra("user", "fromActivity");
            startService(loopingIntent);
        }

        public void previous() {
            Intent previousIntent = new Intent(mContext, MusicService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            previousIntent.putExtra("user", "fromActivity");
            startService(previousIntent);
        }

        public void next() {
            Intent nextIntent = new Intent(mContext, MusicService.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            nextIntent.putExtra("user", "fromActivity");
            startService(nextIntent);
        }

        public void start() {
            Intent playIntent = new Intent(mContext, MusicService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            playIntent.putExtra("type", "play");
            playIntent.putExtra("user", "fromActivity");
            startService(playIntent);
        }

        public void pause() {
            Intent playIntent = new Intent(mContext, MusicService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            playIntent.putExtra("type", "pause");
            playIntent.putExtra("user", "fromActivity");
            startService(playIntent);
        }

        public void seekTo(int progress) {
            Intent seekIntent = new Intent(mContext, MusicService.class);
            seekIntent.setAction(Constants.ACTION.SEEKTO_ACTION);
            seekIntent.putExtra("progress", progress);
            seekIntent.putExtra("user", "fromActivity");
            startService(seekIntent);
        }

        public void stop() {
            Intent stopIntent = new Intent(mContext, MusicService.class);
            stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            stopIntent.putExtra("user", "fromActivity");
            startService(stopIntent);
        }

        public void updateSong() {
            Intent updateIntent = new Intent(mContext, MusicService.class);
            updateIntent.setAction(Constants.ACTION.UPDATE_SONG);
            updateIntent.putExtra("user", "fromActivity");
            startService(updateIntent);
        }
    }
}