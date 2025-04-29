package com.yl.deepseekxunfei.room;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yl.deepseekxunfei.room.entity.ChatHistoryDetailEntity;

import java.lang.reflect.Type;
import java.util.List;

//数据类型转换，需要在AppDatabase中定义
public class ChatHistoryConverter {
    @TypeConverter
    public static String fromUserList(List<ChatHistoryDetailEntity> chatHistoryDetailEntityList) {
        if (chatHistoryDetailEntityList == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<ChatHistoryDetailEntity>>() {}.getType();
        return gson.toJson(chatHistoryDetailEntityList, type);
    }

    @TypeConverter
    public static List<ChatHistoryDetailEntity> toUserList(String chatHistoryString) {
        if (chatHistoryString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<ChatHistoryDetailEntity>>() {}.getType();
        return gson.fromJson(chatHistoryString, type);
    }
}
