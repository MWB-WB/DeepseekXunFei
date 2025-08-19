package com.yl.deepseekxunfei.room;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.yl.deepseekxunfei.room.dao.ChatHistoryDao;
import com.yl.deepseekxunfei.room.entity.ChatHistoryDetailEntity;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;

import java.util.List;

@Database(entities = {ChatHistoryEntity.class}, version = 1)
@TypeConverters(ChatHistoryConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ChatHistoryDao chatHistoryDao();

    //数据库名称
    private static final String DB_NAME = "deepseek.db";
    private static volatile AppDatabase instance;
    private static List<ChatHistoryEntity> chatHistoryEntities;
    private static QueryCallBack queryCallBack;

    //获取数据库实体
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    //创建数据库
    private static AppDatabase create(final Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        DB_NAME)
                .allowMainThreadQueries()
                .build();
    }

    public static void insert(ChatHistoryEntity chatHistoryEntity) {
        new InsertTask().execute(chatHistoryEntity);
    }

    public static void Delete(ChatHistoryEntity chatHistoryEntity) {
        new DeleteTask().execute(chatHistoryEntity);
    }

    public void query(QueryCallBack queryCallBack) {
        this.queryCallBack = queryCallBack;
        new QueryTask().execute();
    }

    private static class InsertTask extends AsyncTask<ChatHistoryEntity, Void, Void> {
        @Override
        protected Void doInBackground(ChatHistoryEntity... chatHistoryEntities) {
            instance.chatHistoryDao().insertChatHistory(chatHistoryEntities[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("TAG", "onPostExecute: 数据插入成功");
        }
    }

    public static List<ChatHistoryEntity> getChatHistoryEntities() {
        return chatHistoryEntities;
    }

    private static class QueryTask extends AsyncTask<Void, Void, List<ChatHistoryEntity>> {
        @Override
        protected List<ChatHistoryEntity> doInBackground(Void... voids) {
            return instance.chatHistoryDao().getAll();
        }

        @Override
        protected void onPostExecute(List<ChatHistoryEntity> chatHistoryEntities) {
            AppDatabase.chatHistoryEntities = chatHistoryEntities;
            if (chatHistoryEntities != null && !chatHistoryEntities.isEmpty()) {
                for (ChatHistoryEntity entity : chatHistoryEntities) {
                    List<ChatHistoryDetailEntity> chatHistoryDetailEntityList = entity.getChatHistoryDetailEntities();
                    for (ChatHistoryDetailEntity chatHistoryDetailEntity : chatHistoryDetailEntityList) {
                        Log.e("TAG", "onPostExecute: " + "entity.id: " + entity.id + " chatHistoryDetailEntity.id : "
                                + chatHistoryDetailEntity.id + ":: " + chatHistoryDetailEntity.thinkMessage + ":: " + chatHistoryDetailEntity.message);
                    }
                }
            }
            if (queryCallBack != null) {
                queryCallBack.onCallBack(chatHistoryEntities);
            }
        }
    }

    public interface QueryCallBack {
        void onCallBack(List<ChatHistoryEntity> chatHistoryEntities);
    }

    private static class DeleteTask extends AsyncTask<ChatHistoryEntity, Void, Void> {
        @Override
        protected Void doInBackground(ChatHistoryEntity... chatHistoryEntities) {
            instance.chatHistoryDao().deleteChatHistoryEntity(chatHistoryEntities[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("TAG", "onPostExecute: 数据删除成功");
        }
    }

}
