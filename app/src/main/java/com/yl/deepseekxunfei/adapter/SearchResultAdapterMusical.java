package com.yl.deepseekxunfei.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.page.LocationMusccarResult;

import java.util.List;

public class SearchResultAdapterMusical extends RecyclerView.Adapter<SearchResultAdapterMusical.ViewHolder> {
    private List<LocationMusccarResult> results;
    private OnItemClickListener listener;

    public SearchResultAdapterMusical(List<LocationMusccarResult> results, OnItemClickListener listener) {
        this.results = results;
        this.listener = listener;
    }
    @NonNull
    @Override
    public SearchResultAdapterMusical.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchResultAdapterMusical.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationMusccarResult result = results.get(position);
        holder.nameText.setText(result.getSongName());
        holder.addressText.setText(result.getAlbum());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(result));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView addressText;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            addressText = itemView.findViewById(R.id.addressText);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(LocationMusccarResult result);
    }
}
