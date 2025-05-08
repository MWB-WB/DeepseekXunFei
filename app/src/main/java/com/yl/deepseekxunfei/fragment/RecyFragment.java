package com.yl.deepseekxunfei.fragment;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.ybq.android.spinkit.SpinKitView;
import com.yl.deepseekxunfei.APICalls.MovieApiClient;
import com.yl.deepseekxunfei.APICalls.NeighborhoodSearch;
import com.yl.deepseekxunfei.APICalls.SongPlaybackAPI;
import com.yl.deepseekxunfei.OnPoiSearchListener;
import com.yl.deepseekxunfei.utlis.AmapNavigator;
import com.yl.deepseekxunfei.MainActivity;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.adapter.SearchResultAdapter;
import com.yl.deepseekxunfei.adapter.SearchResultAdapterMovie;
import com.yl.deepseekxunfei.adapter.SearchResultAdapterMusical;
import com.yl.deepseekxunfei.model.MovieDetailModel;
import com.yl.deepseekxunfei.model.MovieResponse;
import com.yl.deepseekxunfei.page.LocationMusccarResult;
import com.yl.deepseekxunfei.page.LocationResult;

import java.io.IOException;
import java.util.List;

public class RecyFragment extends Fragment {

    private RecyclerView searchResultsRecyclerView;
    //动画效果
    private SpinKitView spinKitView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_recy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        // 初始化视图
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        spinKitView = view.findViewById(R.id.spin_kit);

        // 设置RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void showNavSearchResult(List<LocationResult> results) {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
        SearchResultAdapter adapter = new SearchResultAdapter(results, result -> {
            // 点击结果后导航
            AmapNavigator.startNavigationByUri(
                    getContext(),
                    result.getName(),
                    result.getLongitude(),
                    result.getLatitude()
            );

        });
        searchResultsRecyclerView.setAdapter(adapter);
    }

    public void showWaypointsResult(List<LocationResult> results, OnWayPointClick onWayPointClick) {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
        SearchResultAdapter adapter = new SearchResultAdapter(results, onWayPointClick::onClick);
        searchResultsRecyclerView.setAdapter(adapter);
    }

    public void showStartWaypointsResult(List<LocationResult> results, LocationResult wayPoint) {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
        SearchResultAdapter adapter = new SearchResultAdapter(results, result -> {
            AmapNavigator.startNavigationWithWayPoint(getContext()
                    , null, 0.0, 0.0
                    , result.getName(), result.getLongitude(), result.getLatitude(),
                    wayPoint.getLongitude() + "," + wayPoint.getLatitude() + "," + wayPoint.getName());
        });
        searchResultsRecyclerView.setAdapter(adapter);
    }

    public int getItemCount() {
        if (searchResultsRecyclerView != null) {
            return getVisibleItemCount(searchResultsRecyclerView);
        }
        return -1;
    }

    private int getVisibleItemCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            return lastVisibleItemPosition - firstVisibleItemPosition + 1;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] firstVisibleItemPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            int[] lastVisibleItemPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisibleItemPositions);
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastVisibleItemPositions);
            int firstVisibleItemPosition = getMinPosition(firstVisibleItemPositions);
            int lastVisibleItemPosition = getMaxPosition(lastVisibleItemPositions);
            return lastVisibleItemPosition - firstVisibleItemPosition + 1;
        }
        return 0;
    }

    private int getMinPosition(int[] positions) {
        int minPosition = Integer.MAX_VALUE;
        for (int position : positions) {
            if (position < minPosition) {
                minPosition = position;
            }
        }
        return minPosition;
    }

    private int getMaxPosition(int[] positions) {
        int maxPosition = Integer.MIN_VALUE;
        for (int position : positions) {
            if (position > maxPosition) {
                maxPosition = position;
            }
        }
        return maxPosition;
    }

    public void showMusicSearchResult(List<LocationMusccarResult> results) {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
        SearchResultAdapterMusical adapter = new SearchResultAdapterMusical(results, result -> {
            // 点击结果后跳转
            showSearchResultsMusccar(results);
        });
        searchResultsRecyclerView.setAdapter(adapter);
    }

    public void performClickItem(int position) {
        if (searchResultsRecyclerView != null) {
            RecyclerView.LayoutManager layoutManager = searchResultsRecyclerView.getLayoutManager();
            View view = layoutManager.findViewByPosition(position);
            if (view != null) {
                view.performClick();
            }
        }
    }

    public void showSearchResultsMusccar(List<LocationMusccarResult> results) {
        Log.d("TAG", "showSearchResultsMusccar: " + results);
        getActivity().runOnUiThread(() -> {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            spinKitView.setVisibility(View.GONE);
            SearchResultAdapterMusical adapter = new SearchResultAdapterMusical(results, result -> {
                Log.d("歌曲HASH值", "歌曲HASH值: " + result.getMusicId().toString());
                SongPlaybackAPI.playBack(getContext(), result.toString());
//                //预留播放实现
//                String hash ="1c404ebb2a6e062bea20f5627831c89c"; // 歌曲hash
//                Intent  kugou = new Intent();
//                // 检查是否有应用能处理该Intent
////                kugou.setPackage("com.kugou.android.auto");
//                kugou.setClassName("com.kugou.and roid.auto","com.kugou.android.auto/.ui.activity.SplashPureActivity");
////                kugou.setData (Uri.parse("kugoucar://play?hash=" + hash));
//                startActivity(kugou);
//                String hash = "1c404ebb2a6e062bea20f5627831c89c";
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("kugouplayer://play/hash/"+ hash)); // 车机版可能用这个
//                intent.setPackage("com.kugou.android.auto"); // 限制只用车机版打开
//                    startActivity(intent);
                // 5. 打开酷狗音乐车机版
                try {
                    Intent kugou = new Intent();
                    kugou.setComponent(new ComponentName(
                            "com.kugou.android.auto",
                            "com.kugou.android.auto.ui.activity.SplashPureActivity"));
                    startActivity(kugou);
                } catch (ActivityNotFoundException e) {
                    Log.d("TAG", "onCreate: " + e.getMessage());
                    Toast.makeText(getContext(), "未安装酷狗音乐车机版", Toast.LENGTH_SHORT).show();
                }
            });
            searchResultsRecyclerView.setAdapter(adapter);
        });
    }

    public void getNearbyCinema() {
        NeighborhoodSearch.search("电影院", "", 1000, new OnPoiSearchListener() {
            @Override
            public void onSuccess(List<LocationResult> results) {
                getActivity().runOnUiThread(() -> {
                    searchResultsRecyclerView.setVisibility(View.VISIBLE);
                    spinKitView.setVisibility(View.GONE);
                    SearchResultAdapter adapter = new SearchResultAdapter(results, result -> {
                        // 点击结果后导航
                        AmapNavigator.startNavigationByUri(
                                getContext(),
                                result.getName(),
                                result.getLongitude(),
                                result.getLatitude()
                        );
                    });
                    searchResultsRecyclerView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String error) {

            }
        }, getContext());
    }

    public void getNowPlayingMovies() {
        MovieApiClient.getNowPlayingMovies(new MovieApiClient.OnMoviesLoadedListener() {
            @Override
            public void onSuccess(List<MovieResponse.Movie> movies) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) getActivity()).setCurrentChatOver();
                        searchResultsRecyclerView.setVisibility(View.VISIBLE);
                        spinKitView.setVisibility(View.GONE);
                        SearchResultAdapterMovie adapterMovie = new SearchResultAdapterMovie(getContext(), movies, new SearchResultAdapterMovie.OnItemClickListener() {
                            @Override
                            public void onItemClick(MovieResponse.Movie result, int position) {
                                getMovieDetail(result.getId());
                            }
                        }, new SearchResultAdapterMovie.OnButtonClickListener() {
                            @Override
                            public void onButtonClick() {
                                searchResultsRecyclerView.setVisibility(View.GONE);
                                spinKitView.setVisibility(View.VISIBLE);
                                getNearbyCinema();
                            }
                        });
                        searchResultsRecyclerView.setAdapter(adapterMovie);
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                Log.e("TAG", "Failed to load movies: " + e.getMessage());
            }
        });
    }

    private void getMovieDetail(int id) {
        MovieApiClient.getMoviesDetail(id, new MovieApiClient.OnMoviesDetailLoadedListener() {
            @Override
            public void onSuccess(MovieDetailModel model) {
                ((MainActivity) getActivity()).showDetailFragment(model);
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }

    public void setRecyGone() {
        searchResultsRecyclerView.setVisibility(View.GONE);
        spinKitView.setVisibility(View.VISIBLE);
    }

    public interface OnWayPointClick {
        void onClick(LocationResult result);
    }

}
