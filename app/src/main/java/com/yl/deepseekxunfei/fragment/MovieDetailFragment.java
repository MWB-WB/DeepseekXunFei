package com.yl.deepseekxunfei.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.R;
import com.yl.tianmao.MovieDetailModel;

public class MovieDetailFragment extends Fragment implements View.OnClickListener {

    private ImageView movieImg;
    private TextView movieTitle;
    private TextView movieContent;
    private TextView movieCat;
    private TextView movieStart;
    private TextView moviePubDesc;
    private ImageView backImg;
    private Button movieCinema;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.movie_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        movieImg = view.findViewById(R.id.movie_img);
        movieTitle = view.findViewById(R.id.movie_title);
        movieContent = view.findViewById(R.id.movie_content);
        movieCat = view.findViewById(R.id.movie_cat);
        movieStart = view.findViewById(R.id.movie_start);
        moviePubDesc = view.findViewById(R.id.movie_pubDesc);
        backImg = view.findViewById(R.id.back_row);
        movieCinema = view.findViewById(R.id.movie_cinema);
        backImg.setOnClickListener(this);
        movieCinema.setOnClickListener(this);
    }

    public void setData(MovieDetailModel model) {
        Glide.with(getContext()).load(model.getDetailMovie().getImg()).into(movieImg);
        movieCat.setText(model.getDetailMovie().getCat());
        movieTitle.setText(model.getDetailMovie().getNm());
        movieStart.setText(model.getDetailMovie().getStar());
        movieContent.setText(model.getDetailMovie().getShareInfo().getContent());
        moviePubDesc.setText(model.getDetailMovie().getPubDesc());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_row) {
            ((MainActivity) getActivity()).showMovieFragment();
        } else if (v.getId() == R.id.movie_cinema) {
            ((MainActivity) getActivity()).showNearbyCinemaFragment();
        }
    }
}
