package com.yl.deepseekxunfei.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yl.deepseekxunfei.room.dao.AMapLocationDao;
import com.yl.deepseekxunfei.room.entity.AMapLocationEntity;

/**
 * 区县编码数据库
 */
@Database(entities = {AMapLocationEntity.class}, version = 2)
public abstract class AppDatabaseAbcodeRoom extends RoomDatabase {
    public abstract AMapLocationDao amapLocationDao();

    private static volatile AppDatabaseAbcodeRoom INSTANCE;

    public static AppDatabaseAbcodeRoom getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabaseAbcodeRoom.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabaseAbcodeRoom.class, "app_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
