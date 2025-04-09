package com.yl.deepseekxunfei.model;

import com.google.gson.annotations.SerializedName;

public class MovieDetailModel {

    @SerializedName("detailMovie")
    private MovieDetail detailMovie;

    public MovieDetail getDetailMovie() {
        return detailMovie;
    }

    public static class MovieDetail {
        @SerializedName("nm")
        private String nm;
        @SerializedName("shareInfo")
        private ShareInfo shareInfo;

        public String getNm() {
            return nm;
        }

        public ShareInfo getShareInfo() {
            return shareInfo;
        }

        private class ShareInfo {
            @SerializedName("content")
            private String content;

            @SerializedName("img")
            private String img;

            @SerializedName("title")
            private String title;

            public String getContent() {
                return content;
            }

            public String getImg() {
                return img;
            }

            public String getTitle() {
                return title;
            }
        }
    }
}