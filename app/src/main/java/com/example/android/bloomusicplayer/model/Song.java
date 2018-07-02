package com.example.android.bloomusicplayer.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

public class Song {

    static BitmapFactory.Options options = null;
    private long id;
    private String filePath;
    // optional metadata
    private String title = "";
    private String artist = "";
    private String album = "";
    private int year = -1;
    private String genre = "";
    private int track_no = -1;
    private long duration_ms = -1;

    /**
     * Creates a new Song, with specified `songID` and `filePath`.
     *
     * @note It's a unique Android identifier for a media file
     * anywhere on the system.
     */
    public Song(long id, String filePath) {
        this.id = id;
        this.filePath = filePath;
        if (options == null) {
            options = new BitmapFactory.Options();
            options.inSampleSize = 10;
            options.inJustDecodeBounds = false;
        }
    }

    /**
     * Identifier for the song on the Android system.
     * (so we can locate the file anywhere)
     */
    public long getId() {
        return id;
    }

    /**
     * Full path for the music file within the filesystem.
     */
    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getTrackNumber() {
        return track_no;
    }

    public void setTrackNumber(int track_no) {
        this.track_no = track_no;
    }

    /**
     * Returns the duration of the song, in miliseconds.
     */
    public long getDuration() {
        return duration_ms;
    }

    /**
     * Sets the duration of the song, in miliseconds.
     */
    public void setDuration(long duration_ms) {
        this.duration_ms = duration_ms;
    }

    public long getDurationSeconds() {
        return getDuration() / 1000;
    }

    public long getDurationMinutes() {
        return getDurationSeconds() / 60;
    }

    public Bitmap getAlbumart() {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        byte[] data = mmr.getEmbeddedPicture();
        //coverart is an Imageview object
        // convert the byte array to a bitmap
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } else {
            return null;
        }
    }

    public Bitmap getThumbnail() {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            byte[] data = mmr.getEmbeddedPicture();
            //coverart is an Imageview object
            // convert the byte array to a bitmap
            if (data != null) {
                return BitmapFactory.decodeByteArray(data, 0, data.length, options);
            } else {
                return null;
            }
        } catch (RuntimeException ex) {
            return null;
        }

    }

    public byte[] getAlbumBytes() {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        byte[] data = mmr.getEmbeddedPicture();
        return data;
    }

}