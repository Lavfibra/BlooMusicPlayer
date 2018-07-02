package com.example.android.bloomusicplayer.lastfm.rest.service;

import android.support.annotation.Nullable;

import com.example.android.bloomusicplayer.lastfm.rest.model.LastFmAlbum;
import com.example.android.bloomusicplayer.lastfm.rest.model.LastFmArtist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface LastFMService {
    String API_KEY = "75034ef1fcfd0ed0cd8176b25725f169";
    String BASE_QUERY_PARAMETERS = "?format=json&autocorrect=1&api_key=" + API_KEY;

    @GET(BASE_QUERY_PARAMETERS + "&method=album.getinfo")
    Call<LastFmAlbum> getAlbumInfo(@Query("album") String albumName, @Query("artist") String artistName, @Nullable @Query("lang") String language);

    @GET(BASE_QUERY_PARAMETERS + "&method=artist.getinfo")
    Call<LastFmArtist> getArtistInfo(@Query("artist") String artistName, @Nullable @Query("lang") String language, @Nullable @Header("Cache-Control") String cacheControl);
}