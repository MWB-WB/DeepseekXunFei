package com.yl.deepseekxunfei.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;

import java.util.List;

public class HistoryRecyAdapter extends RecyclerView.Adapter<HistoryRecyAdapter.MyViewHolder> {

    private List<ChatHistoryEntity> mData;
    private Context mContext;
    private int clickPosition = 0;

    public HistoryRecyAdapter(List<ChatHistoryEntity> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_recy_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(mData.get(position).getTitle());
        if (clickPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.answer_text_bg);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 加载自定义布局
                    View menuView = LayoutInflater.from(mContext).inflate(R.layout.custom_popup_menu, null);

                    // 创建PopupWindow
                    PopupWindow popupWindow = new PopupWindow(
                            menuView,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            true
                    );

                    // 设置样式
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.setElevation(16f);

                    // 显示在锚点视图下方
                    popupWindow.showAsDropDown(holder.imageView, 0, 0, Gravity.START);

                    // 处理点击事件
                    menuView.findViewById(R.id.btn_rename).setOnClickListener(view -> {
//                        showRenameDialog();
                        popupWindow.dismiss();
                    });

                    menuView.findViewById(R.id.btn_delete).setOnClickListener(view -> {
//                        confirmDelete();
                        popupWindow.dismiss();
                    });
                }
            });
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.imageView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPosition = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dialog_recy_item_tv);
            imageView = itemView.findViewById(R.id.dialog_recy_item_iv);
        }
    }

    public int getClickPosition() {
        return clickPosition;
    }

}
