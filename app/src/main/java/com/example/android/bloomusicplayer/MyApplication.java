package com.example.android.bloomusicplayer;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, MusicService.class).setAction(Constants.ACTION.FIRST_START));
    }
}
