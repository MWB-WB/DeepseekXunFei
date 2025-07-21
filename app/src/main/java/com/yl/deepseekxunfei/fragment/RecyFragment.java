package com.yl.deepseekxunfei.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.ybq.android.spinkit.SpinKitView;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.gaodeApi.poi.ReverseGeography;
import com.yl.ylcommon.utlis.AmapNavEntity;
import com.yl.ylcommon.utlis.AmapNavigator;
import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.adapter.SearchResultAdapter;
import com.yl.deepseekxunfei.adapter.SearchResultAdapterMovie;
import com.yl.deepseekxunfei.adapter.SearchResultAdapterMusical;
import com.yl.gaodeApi.page.LocationResult;
import com.yl.gaodeApi.poi.NeighborhoodSearch;
import com.yl.gaodeApi.poi.OnPoiSearchListener;
import com.yl.kuwo.MusicKuwo;
import com.yl.kuwo.PluginMediaModel;
import com.yl.tianmao.MovieApiClient;
import com.yl.tianmao.MovieDetailModel;
import com.yl.tianmao.MovieResponse;

import java.io.IOException;
import java.util.List;

public class RecyFragment extends Fragment {

    private RecyclerView searchResultsRecyclerView;
    //动画效果
    private SpinKitView spinKitView;
    String addressName;
    MainActivity mainActivity;
    ReverseGeography reverseGeography;

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
        mainActivity = (MainActivity) getActivity();
        reverseGeography = new ReverseGeography();
    }

    public void showNavSearchResult(List<LocationResult> results) {
        Log.d("选择LocationResult", "showNavSearchResult: " + results);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
        SearchResultAdapter adapter = new SearchResultAdapter(results, result -> {
            // 点击结果后导航
            String lonLat = AmapNavigator.startNavigationByUri(
                    getContext(),
                    result.getName(),
                    result.getLongitude(),
                    result.getLatitude(),
                    result.getAddress(),
                    new AmapNavEntity(result.getName(), result.getLongitude() + "," + result.getLatitude(), result.getAddress())
            );
            addressName = reverseGeography.reverseGeographyApi(lonLat, new ReverseGeography.successApi() {
                @Override
                public void success(String formattedAddress) {
                    addressName = formattedAddress;
                }
            });
            Log.d("导航选择：", "上下: " + lonLat);
            Log.d("导航选择：", "上下: " + addressName);
            if (addressName != null) {
                mainActivity.updateContext(null, "我当前所在" + addressName, true);
            } else {
                mainActivity.updateContext(null, "我当前所在，坐标：" + lonLat, true);
            }
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

    public void showMusicSearchResult(List<PluginMediaModel> results, MusicKuwo kuwo) {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
        SearchResultAdapterMusical adapter = new SearchResultAdapterMusical(results, new SearchResultAdapterMusical.OnItemClickListener() {
            @Override
            public void onItemClick(List<PluginMediaModel> result, int position) {
                Log.e("TAG", "onItemClick: " + result.get(position).getKeyWords() + ":: " + result.get(position).getArtists() + ":: " + result.get(position).getTitle());
                kuwo.play(result, position);
            }
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

    public void showSearchResultsMusccar(List<PluginMediaModel> results) {
        Log.d("TAG", "showSearchResultsMusccar: " + results);
        getActivity().runOnUiThread(() -> {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            spinKitView.setVisibility(View.GONE);
            SearchResultAdapterMusical adapter = new SearchResultAdapterMusical(results, new SearchResultAdapterMusical.OnItemClickListener() {
                @Override
                public void onItemClick(List<PluginMediaModel> result, int position) {

                }
            });
            searchResultsRecyclerView.setAdapter(adapter);
        });
    }

    //点击查看影院
    public void getNearbyCinema() {
        NeighborhoodSearch.search("电影院", "", 1000, new OnPoiSearchListener() {
            @Override
            public void onSuccess(List<LocationResult> results) {
                getActivity().runOnUiThread(() -> {
                    searchResultsRecyclerView.setVisibility(View.VISIBLE);
                    spinKitView.setVisibility(View.GONE);
                    SearchResultAdapter adapter = new SearchResultAdapter(results, result -> {
                        // 点击结果后导航
                        String lonLat = AmapNavigator.startNavigationByUri(
                                getContext(),
                                result.getName(),
                                result.getLongitude(),
                                result.getLatitude(),
                                result.getAddress(),
                                new AmapNavEntity(result.getName(), result.getLongitude() + "," + result.getLatitude(), result.getAddress())
                        );
                        Log.d("TAG", "上下: callGenerateApi");
                        addressName = reverseGeography.reverseGeographyApi(lonLat, new ReverseGeography.successApi() {
                            @Override
                            public void success(String formattedAddress) {
                                addressName = formattedAddress;
                            }
                        });
                        if (addressName != null) {
                            mainActivity.updateContext(null, "我当前所在" + addressName, true);
                        } else {
                            mainActivity.updateContext(null, "我当前所在，坐标：" + lonLat, true);
                        }
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
                                Log.d("diany", "onItemClick: 23");
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
