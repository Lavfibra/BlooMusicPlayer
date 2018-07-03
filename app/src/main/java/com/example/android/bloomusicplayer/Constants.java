package com.example.android.bloomusicplayer;

public class Constants {


    public interface ACTION {
        String MAIN_ACTION = "com.bloomusicplayer.foregroundservice.action.main";
        String PREV_ACTION = "com.bloomusicplayer.foregroundservice.action.prev";
        String PLAY_ACTION = "com.bloomusicplayer.foregroundservice.action.play";
        String NEXT_ACTION = "com.bloomusicplayer.foregroundservice.action.next";
        String STARTFOREGROUND_ACTION = "com.bloomusicplayer.foregroundservice.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.bloomusicplayer.foregroundservice.action.stopforeground";
        String SETLOOPING_ACTION = "com.bloomusicplayer.foregroundservice.action.setlooping";
        String SEEKTO_ACTION = "com.bloomusicplayer.foregroundservice.action.seekto";
        String UPDATE_SONG = "com.bloomusicplayer.foregroundservice.action.updatesong";
        String FIRST_START = "com.bloomusicplayer.foregroundservice.action.firststart";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}