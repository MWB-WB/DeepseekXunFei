package com.yl.deepseekxunfei.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;

import java.util.List;

@Dao
public interface ChatHistoryDao {

    @Insert
    void insertChatHistory(ChatHistoryEntity historyEntity);

    @Insert
    void insertChatHistories(List<ChatHistoryEntity> histories);

    //编写自己的 SQL 查询(query)方法
    //查询 chat_history 表
    @Query("SELECT * FROM chat_history")
    List<ChatHistoryEntity> getAll();
}
