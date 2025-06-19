package com.yl.deepseekxunfei.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yl.deepseekxunfei.R;
import com.yl.gaodeApi.page.LocationResult;

import java.util.List;

/**
 * 设配器
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<LocationResult> results;
    private OnItemClickListener listener;

    public SearchResultAdapter(List<LocationResult> results, OnItemClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationResult result = results.get(position);
        holder.nameText.setText(result.getName());
        holder.addressText.setText(result.getAddress());
        // 加载图片（使用 Glide）
        if (result.getPhotoUrl() != null && !result.getPhotoUrl().isEmpty()) {
            Log.d("TAG", "onBindViewHolder: "+            result.getPhotoUrl());
            Glide.with(holder.itemView.getContext())
                    .load(result.getPhotoUrl())
                    .placeholder(R.drawable.zanwu) // 占位图
                    .error(R.drawable.zanwu)            // 错误图
                    .centerCrop()                          // 图片裁剪方式
                    .into(holder.imageView);

        } else {
            // 无图片时显示默认图或隐藏 ImageView
            Log.d("TAG", "onBindViewHolder: "+            result.getPhotoUrl());
            holder.imageView.setImageResource(R.drawable.zanwu);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(result));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView addressText;
        ImageView imageView;


        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            addressText = itemView.findViewById(R.id.addressText);
            imageView = itemView.findViewById(R.id.imgView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(LocationResult result);
    }
}
