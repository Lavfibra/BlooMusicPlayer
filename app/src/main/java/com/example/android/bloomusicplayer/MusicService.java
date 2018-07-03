package com.example.android.bloomusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.bloomusicplayer.model.Song;
import com.example.android.bloomusicplayer.model.SongsArray;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MusicService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    public BroadcastReceiver mBroadcastReceiver;
    int position;
    ServiceManager serviceManager;
    Handler mHandler;
    private Gson gson;
    private SongsArray mSongsArray;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
            mHandler.postDelayed(run, 1000);
        }
    };
    private Song mSong;
    private ArrayList<Song> mSongs;
    private String mLoopType;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void seekUpdation() {
        serviceManager.seekTo(mMediaPlayer.getCurrentPosition());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gson = new Gson();
        serviceManager = new ServiceManager(getApplicationContext());
        mLoopType = "all";
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String user = intent.getStringExtra("user");
                if (action != null) {
                    switch (action) {
                        case Constants.ACTION.NEXT_ACTION:
                            next(user);
                            break;
                        case Constants.ACTION.PREV_ACTION:
                            previous(user);
                            break;
                        case Constants.ACTION.PLAY_ACTION:
                            if (intent.getStringExtra("type").equals("start"))
                                start(user);
                            else if (intent.getStringExtra("type").equals("pause"))
                                pause(user);
                            break;
                    }
                }
            }
        };
        mHandler = new Handler();
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), new IntentFilter("mediaPlayer"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String user = intent.getStringExtra("user");
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Constants.ACTION.FIRST_START:
                    break;
                case Constants.ACTION.STARTFOREGROUND_ACTION:
                    Gson gson = new Gson();
                    Log.i("tag", "Received Start Foreground Intent ");
                    mSongsArray = gson.fromJson(intent.getStringExtra("songList"), SongsArray.class);
                    mSongs = mSongsArray.getSongs();
                    position = intent.getIntExtra("position", -1);
                    mSong = gson.fromJson(intent.getStringExtra("song"), Song.class);
                    create();
                    start(user);
                    showNotification();

                    break;
                case Constants.ACTION.PREV_ACTION:
                    Log.i("tag", "Clicked Previous");
                    previous(user);
                    break;
                case Constants.ACTION.PLAY_ACTION:

                    if (user.equals("fromActivity")) {
                        String type = intent.getStringExtra("type");
                        if (type.equals("play")) {
                            Log.i("tag", "Clicked Play");
                            start(user);
                        } else if (type.equals("pause")) {
                            Log.i("tag", "Clicked Pause");
                            pause(user);
                        }
                    } else if (user.equals("fromService")) {
                        if (mMediaPlayer.isPlaying())
                            pause(user);
                        else
                            start(user);
                    }
                    break;
                case Constants.ACTION.NEXT_ACTION:
                    Log.i("tag", "Clicked Next");
                    next(user);
                    break;
                case Constants.ACTION.SEEKTO_ACTION:
                    seekTo(intent.getIntExtra("progress", 0));
                    break;
                case Constants.ACTION.UPDATE_SONG:
                    updateUi();
                    break;
                case Constants.ACTION.SETLOOPING_ACTION:
                    setLooping(intent.getStringExtra("type"));
                    break;
                case Constants.ACTION.STOPFOREGROUND_ACTION:
                    Log.i("tag", "Received Stop Foreground Intent");
                    mHandler.removeCallbacks(run);
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return START_STICKY;
    }

    private void updateUi() {
        serviceManager.updateUi();
    }

    public void previous(String user) {
        if (mLoopType.equals("all")) {
            position--;
            if (position <= -1) {
                position = mSongs.size() - 1;
            }
            mSong = mSongs.get(position);
            create();
            if (user.equals("fromService")) {
                serviceManager.previous();
            }
            start(user);
            showNotification();
        }

    }

    public void next(String user) {
        if (mLoopType.equals("all")) {
            position++;
            if (position >= mSongs.size()) {
                position = 0;
            }
            mSong = mSongs.get(position);
            create();
            if (user.equals("fromService")) {
                serviceManager.next();
            }
            start(user);
            showNotification();
        }
    }

    public void create() {
        release();
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(mSong.getFilePath()));
        mMediaPlayer.setOnCompletionListener(mp -> {
            if (mLoopType.equals("all")) {
                next("fromService");
            } else if (mLoopType.equals("no")) {
                mMediaPlayer.seekTo(0);
                mMediaPlayer.pause();
            }
        });
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    public void pause(String user) {
        mMediaPlayer.pause();
        mHandler.removeCallbacks(run);
        if (user.equals("fromService")) {
            serviceManager.pause();
        }
        showNotification();
    }

    public void start(String user) {
        mMediaPlayer.start();
        mHandler.postDelayed(run, 0);
        if (user.equals("fromService")) {
            serviceManager.start();
        }
        showNotification();

    }

    public void setLooping(String type) {
        mLoopType = type;
        switch (type) {
            case "all":
                mMediaPlayer.setLooping(false);
                break;
            case "self":
                mMediaPlayer.setLooping(true);
                break;
            case "no":
                mMediaPlayer.setLooping(false);
                break;
        }
    }

    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.putExtra("songsList", gson.toJson(mSongsArray));
        notificationIntent.putExtra("position", position);
        notificationIntent.putExtra("song", gson.toJson(mSong));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        previousIntent.putExtra("user", "fromService");
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        playIntent.putExtra("user", "fromService");
        if (mMediaPlayer.isPlaying())
            playIntent.putExtra("type", "pause");
        else
            playIntent.putExtra("type", "start");

        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        nextIntent.putExtra("user", "fromService");
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(mSong.getTitle())
                .setTicker(mSong.getTitle() + " - " + mSong.getArtist())
                .setContentText(mSong.getArtist())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(mSong.getThumbnail())
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_baseline_skip_previous_24px, "Previous",
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(R.drawable.ic_baseline_skip_next_24px, "Next",
                        pnextIntent).build();

        //notification is an object of class android.app.Notification
        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            try {
                Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
                Object miuiNotification = miuiNotificationClass.newInstance();
                Field field = miuiNotification.getClass().getDeclaredField("customizedIcon");
                field.setAccessible(true);

                field.set(miuiNotification, true);
                field = notification.getClass().getField("extraNotification");
                field.setAccessible(true);

                field.set(notification, miuiNotification);
            } catch (Exception ignored) {

            }
        }

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("tag", "In onDestroy");
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    public class ServiceManager {
        Context mContext;
        private LocalBroadcastManager broadcaster;


        ServiceManager(Context context) {
            mContext = context;
            broadcaster = LocalBroadcastManager.getInstance(mContext);
        }


        void updateUi() {
            Intent updateIntent = new Intent(mContext, MusicService.class);
            updateIntent.setAction(Constants.ACTION.UPDATE_SONG);
            updateIntent.putExtra("user", "fromService");
            updateIntent.putExtra("position", position);
            broadcaster.sendBroadcast(updateIntent);
        }

        public void previous() {
            Intent previousIntent = new Intent(mContext, MusicService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            previousIntent.putExtra("user", "fromService");
            broadcaster.sendBroadcast(previousIntent);
        }

        public void next() {
            Intent nextIntent = new Intent(mContext, MusicService.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            nextIntent.putExtra("user", "fromService");
            broadcaster.sendBroadcast(nextIntent);
        }

        public void start() {
            Intent playIntent = new Intent(mContext, MusicService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            playIntent.putExtra("type", "start");
            playIntent.putExtra("user", "fromService");
            broadcaster.sendBroadcast(playIntent);
        }

        void pause() {
            Intent playIntent = new Intent(mContext, MusicService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            playIntent.putExtra("type", "pause");
            playIntent.putExtra("user", "fromService");
            broadcaster.sendBroadcast(playIntent);
        }

        void seekTo(int progress) {
            Intent seekIntent = new Intent(mContext, MusicService.class);
            seekIntent.setAction(Constants.ACTION.SEEKTO_ACTION);
            seekIntent.putExtra("progress", progress);
            seekIntent.putExtra("user", "fromActivity");
            broadcaster.sendBroadcast(seekIntent);
        }
    }


}
