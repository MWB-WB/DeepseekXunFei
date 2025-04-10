package com.yl.deepseekxunfei.model;

import java.util.List;

/**
 * 用与保存历史对话
 *
 */
public class ChatHistory {
    private String title; // 对话标题
    private List<ChatMessage> messages; // 对话内容（问题和回答的列表）

    // 构造函数
    public ChatHistory(String title, List<ChatMessage> messages) {
        this.title = title;
        this.messages = messages;
    }

    // 获取标题
    public String getTitle() {
        return title;
    }

    // 获取对话内容
    public List<ChatMessage> getMessages() {
        return messages;
    }

    // 设置标题
    public void setTitle(String title) {
        this.title = title;
    }

    // 设置对话内容
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}