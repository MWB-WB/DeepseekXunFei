package com.yl.deepseekxunfei.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.model.MovieResponse;

import java.util.List;

public class SearchResultAdapterMovie extends RecyclerView.Adapter<SearchResultAdapterMovie.ViewHolder> {
    private List<MovieResponse.Movie> results;
    private OnItemClickListener listener;
    private Context mContext;

    public SearchResultAdapterMovie(Context context, List<MovieResponse.Movie> results, OnItemClickListener listener) {
        this.mContext = context;
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieResponse.Movie result = results.get(position);
        Glide.with(mContext).load(result.getImg()).into(holder.movieImg);
        holder.movieTitle.setText("名称:" + result.getNm());
        holder.moviePt.setText("上映时间:" + result.getRt());
        holder.movieStart.setText("主演:" + result.getStar());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(result, position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView movieImg;
        TextView movieTitle;
        TextView moviePt;
        TextView movieStart;

        public ViewHolder(View itemView) {
            super(itemView);
            movieImg = itemView.findViewById(R.id.movie_img);
            movieTitle = itemView.findViewById(R.id.movie_title);
            moviePt = itemView.findViewById(R.id.movie_pt);
            movieStart = itemView.findViewById(R.id.movie_start);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MovieResponse.Movie result, int position);
    }
}
