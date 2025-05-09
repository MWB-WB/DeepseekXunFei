package com.yl.cretemodule.crete.roomCrete;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yl.cretemodule.crete.roomCrete.dao.creteDao;
import com.yl.cretemodule.crete.roomCrete.entity.creteEntity;

@Database(entities = {creteEntity.class}, version = 2)
public abstract class DatabaseCrete extends RoomDatabase {
    private static volatile DatabaseCrete mAppDatabase;

    // TODO 在实例化 AppDatabase 对象时应遵循单例设计模式。每个 RoomDatabase 实例的成本相当高，几乎不需要在单个进程中访问多个实例。
    public static DatabaseCrete getInstance(Context context) {
        if (mAppDatabase == null) {
            synchronized (DatabaseCrete.class) {
                if (mAppDatabase == null) {
                    mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), DatabaseCrete.class, "CreteRoomDB.db")
                            .addMigrations()
                            //允许在主线程中连接数据库
                             .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return mAppDatabase;
    }
    public abstract  creteDao creteDao();
}

