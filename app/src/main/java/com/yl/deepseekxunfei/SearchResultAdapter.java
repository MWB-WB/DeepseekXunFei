package com.yl.deepseekxunfei;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.deepseekxunfei.page.LocationResult;

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
        void onItemClick(LocationResult result);
    }
}
