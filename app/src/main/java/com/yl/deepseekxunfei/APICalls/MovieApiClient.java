package com.yl.deepseekxunfei.APICalls;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.yl.deepseekxunfei.model.MovieDetailModel;
import com.yl.deepseekxunfei.model.MovieResponse;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieApiClient {
    private static final String NOW_PLAYING_MOVIES_URL = "https://m.maoyan.com/ajax/movieOnInfoList";
    private static final String MOVIES_DETAIL_URL = "https://m.maoyan.com/ajax/detailmovie?movieId=";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void getNowPlayingMovies(final OnMoviesLoadedListener listener) {
        Request request = new Request.Builder()
                .url(NOW_PLAYING_MOVIES_URL)
                .get()
                .addHeader("accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    MovieResponse movieResponse = new Gson().fromJson(responseData, MovieResponse.class);
                    List<MovieResponse.Movie> movies = movieResponse.getMovies();
                    handler.post(() -> listener.onSuccess(movies));
                } else {
                    handler.post(() -> listener.onFailure(new IOException("Response code: " + response.code())));
                }
            }
        });
    }

    public static void getMoviesDetail(final int movieId, final OnMoviesDetailLoadedListener listener) {
        Request request = new Request.Builder()
                .url(MOVIES_DETAIL_URL + movieId)
                .get()
                .addHeader("accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    MovieDetailModel movieDetailModel = new Gson().fromJson(responseData, MovieDetailModel.class);
                    handler.post(() -> listener.onSuccess(movieDetailModel));
                } else {
                    handler.post(() -> listener.onFailure(new IOException("Response code: " + response.code())));
                }
            }
        });
    }

    public interface OnMoviesLoadedListener {
        void onSuccess(List<MovieResponse.Movie> movies);

        void onFailure(IOException e);
    }

    public interface OnMoviesDetailLoadedListener {
        void onSuccess(MovieDetailModel movies);

        void onFailure(IOException e);
    }

}