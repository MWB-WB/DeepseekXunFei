package com.yl.douyinapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class DouyinVideoSearcher {
    private static final String BASE_URL = "https://open.douyin.com/partner/api/v2/";

    public interface OnSearchListener {
        void onSuccess(List<VideoItem> videos);

        void onFailure(String error);
    }

    // 最新搜索API请求结构
    public static class SearchRequest {
        @SerializedName("keyword")
        private String keyword;

        @SerializedName("search_type")
        private String searchType = "video";

        @SerializedName("cursor")
        private long cursor = 0;

        @SerializedName("count")
        private int count = 20;

        // 构造方法...


        public SearchRequest(String keyword) {
            this.keyword = keyword;
        }
    }

    // 响应数据结构
    public static class SearchResponse {
        @SerializedName("data")
        private Data data;

        public static class Data {
            @SerializedName("videos")
            private List<VideoItem> videos;

            @SerializedName("has_more")
            private boolean hasMore;

            public List<VideoItem> getVideos() {
                return videos;
            }

            public void setVideos(List<VideoItem> videos) {
                this.videos = videos;
            }

            public boolean isHasMore() {
                return hasMore;
            }

            public void setHasMore(boolean hasMore) {
                this.hasMore = hasMore;
            }
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    public static class VideoItem {
        @SerializedName("item_id")
        private String itemId;

        @SerializedName("desc")
        private String description;

//        @SerializedName("cover")
//        private CoverImage cover;
//
//        @SerializedName("author")
//        private Author author;

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        // Getters...
    }

    public static void search(String accessToken, String keyword, OnSearchListener listener) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + accessToken)
                            .header("X-Tt-Env", "boe_sg") // 最新要求的Header
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<SearchResponse> call = service.searchVideo(new SearchRequest(keyword));

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VideoItem> videos = response.body().getData().getVideos();
                    listener.onSuccess(videos);
                } else {
                    listener.onFailure("API响应错误: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                listener.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    private interface ApiService {
        @POST("video/search/")
        Call<SearchResponse> searchVideo(@Body SearchRequest request);
    }
}