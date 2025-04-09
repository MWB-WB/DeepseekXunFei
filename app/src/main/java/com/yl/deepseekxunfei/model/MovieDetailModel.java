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

        @SerializedName("cat")
        private String cat;

        @SerializedName("img")
        private String img;

        @SerializedName("star")
        private String star;

        @SerializedName("pubDesc")
        private String pubDesc;

        public String getPubDesc() {
            return pubDesc;
        }

        public String getStar() {
            return star;
        }

        public String getCat() {
            return cat;
        }

        public String getImg() {
            return img;
        }

        public String getNm() {
            return nm;
        }

        public ShareInfo getShareInfo() {
            return shareInfo;
        }

        public class ShareInfo {
            @SerializedName("content")
            private String content;

            public String getContent() {
                return content;
            }

        }
    }
}