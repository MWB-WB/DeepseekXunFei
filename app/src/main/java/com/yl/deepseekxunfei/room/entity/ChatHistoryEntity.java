package com.yl.deepseekxunfei.room.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "chat_history")
public class ChatHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public List<ChatHistoryDetailEntity> chatHistoryDetailEntities;

    public ChatHistoryEntity(List<ChatHistoryDetailEntity> chatHistoryDetailEntities, String title) {
        this.chatHistoryDetailEntities = chatHistoryDetailEntities;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChatHistoryDetailEntity> getChatHistoryDetailEntities() {
        return chatHistoryDetailEntities;
    }

    public void setChatHistoryDetailEntities(List<ChatHistoryDetailEntity> chatHistoryDetailEntities) {
        this.chatHistoryDetailEntities = chatHistoryDetailEntities;
    }
}
