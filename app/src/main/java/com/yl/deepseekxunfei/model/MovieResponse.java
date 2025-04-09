package com.yl.deepseekxunfei.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieResponse {
    @SerializedName("movieList")
    private List<Movie> movieList;

    public List<Movie> getMovies() {
        return movieList;
    }

    public static class Movie {
        @SerializedName("id")
        private int id;
        @SerializedName("nm")
        private String nm;
        @SerializedName("star")
        private String star;
        @SerializedName("img")
        private String img;
        @SerializedName("rt")
        private String rt;

        public int getId() {
            return id;
        }

        public String getNm() {
            return nm;
        }

        public String getStar() {
            return star;
        }

        public String getImg() {
            return img;
        }

        public String getRt() {
            return rt;
        }
    }
}