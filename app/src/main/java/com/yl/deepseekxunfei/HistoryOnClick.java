//package com.yl.deepseekxunfei;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.ImageButton;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 打开历史对话
// */
//public class HistoryOnClick {
//    private ImageButton historyButton;//历史对话按钮
//    private ImageButton titleTextView ;//对话标题
//    private ImageButton newChatButton;//新建对话按钮
//    private List<ChatHistory> chatHistories = new ArrayList<>(); // 保存所有历史记录
//    private String currentTitle = ""; // 当前对话的标题
//
//    public void historyDialogue(Bundle s){
//        historyButton.findViewById(R.id.historyButton);
//        historyButton.setOnClickListener(v->{
//            if (!chatHistories.isEmpty()) {
//                showHistoryDialog(); // 显示历史记录对话框
//            } else {
//                Log.d("历史记录", "historyDialogue: 没有历史记录");
//            }
//        });
//    }
//    private void showHistoryDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("历史记录");
//
//        // 获取历史记录标题列表
//        List<String> historyTitles = new ArrayList<>();
//        for (ChatHistory history : chatHistories) {
//            historyTitles.add(history.getTitle());
//        }
//
//        // 将标题列表显示在对话框中
//        builder.setItems(historyTitles.toArray(new String[0]), (dialog, which) -> {
//            // 用户选择某个历史记录
//            ChatHistory selectedHistory = chatHistories.get(which);
//            // 显示选中的历史记录
//            showHistoryMessages(selectedHistory);
//        });
//
//        builder.setNegativeButton("取消", null);
//        builder.show();
//    }
//    private void showHistoryMessages(ChatHistory history) {
//        // 清空当前对话
//        chatMessages.clear();
//        // 加载历史记录的对话内容
//        chatMessages.addAll(history.getMessages());
//        chatAdapter.notifyDataSetChanged();
//        // 更新标题
//        currentTitle = history.getTitle();
//        titleTextView.setText(currentTitle);
//    }
//}
