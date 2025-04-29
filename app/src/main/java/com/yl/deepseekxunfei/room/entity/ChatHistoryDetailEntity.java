package com.yl.deepseekxunfei.room.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_history_detail")
public class ChatHistoryDetailEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public boolean isUser;
    public String thinkMessage;
    public String message;

    public ChatHistoryDetailEntity(boolean isUser, String thinkMessage, String message) {
        this.isUser = isUser;
        this.thinkMessage = thinkMessage;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getThinkMessage() {
        return thinkMessage;
    }

    public void setThinkMessage(String thinkMessage) {
        this.thinkMessage = thinkMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
