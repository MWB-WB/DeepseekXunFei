package com.yl.deepseekxunfei;

import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> chatMessages;
    private static final int TYPE_QUESTION = 0;
    private static final int TYPE_ANSWER = 1;
    private static final int TYPE_THINKING = 2;
    private boolean isFold = true;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        if (viewType == TYPE_QUESTION) {
            layoutRes = R.layout.item_question_message;
        } else if (viewType == TYPE_ANSWER) {
            layoutRes = R.layout.item_answer_message;
        } else {
            layoutRes = R.layout.item_thinking_message;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        Log.e("TAG", "onBindViewHolder: " + message.isThinkContent() + ":: message.getThinkContent(): " + message.getThinkContent()
                + ":: message.getMessage(): " + message.getMessage() + ":: position: " + position);
        if (message.isThinkContent()) {
            holder.thinkMessage.setText(message.getThinkContent());
            holder.textView.setText("");
        } else {
            if (message.isNeedShowFoldText()) {
                if (!TextUtils.isEmpty(message.getThinkContent())) {
                    holder.thinkFlodUnFlod.setVisibility(View.VISIBLE);
                    holder.thinkMessage.setMaxLines(3);
                    holder.thinkFlodUnFlod.setText("展开");
                }
                holder.thinkFlodUnFlod.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isFold) {
                            holder.thinkFlodUnFlod.setText("收起");
                            holder.thinkMessage.setMaxLines(100);
                        } else {
                            holder.thinkFlodUnFlod.setText("展开");
                            holder.thinkMessage.setMaxLines(3);
                        }
                        isFold = !isFold;
                    }
                });
            } else {
                if (holder.thinkFlodUnFlod != null) {
                    holder.thinkFlodUnFlod.setVisibility(View.GONE);
                }
                if (holder.thinkMessage != null) {
                    holder.thinkMessage.setMaxLines(100);
                }
            }
            holder.textView.setText(message.getMessage());
        }

        // 为机器人回答添加布局变化监听器
        if (!message.isUser()) {
            ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // 移除监听器，避免重复调用
                    holder.textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // 检查 Layout 是否为 null
                    Layout layout = holder.textView.getLayout();
                    if (layout != null) {
                        int scrollAmount = layout.getLineTop(holder.textView.getLineCount()) - holder.textView.getHeight();
                        if (scrollAmount > 0) {
                            holder.textView.scrollTo(0, scrollAmount);
                        }
                    }
                }
            };
            // 添加全局布局监听器
            holder.textView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.isUser()) {
            return TYPE_QUESTION;
        } else if (message.getMessage().equals("机器人正在思考...")) {
            return TYPE_THINKING;
        } else {
            return TYPE_ANSWER;
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView thinkMessage;
        TextView thinkFlodUnFlod;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.message_text_view);
            thinkMessage = itemView.findViewById(R.id.think_message);
            thinkFlodUnFlod = itemView.findViewById(R.id.think_flod_unFlod);
            textView.setMovementMethod(new android.text.method.ScrollingMovementMethod()); // 设置滚动方法
        }
    }
}