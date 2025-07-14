package com.yl.deepseekxunfei.room.ulti;



import android.content.Context;

import com.yl.deepseekxunfei.room.AppDatabaseAbcodeRoom;
import com.yl.deepseekxunfei.room.entity.AMapLocationEntity;

import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    public static void populateAsync(final AppDatabaseAbcodeRoom db, List<AMapLocationEntity> locations) {
        new Thread(() -> {
            populateSync(db, locations);
        }).start();
    }

    public static void populateSync(final AppDatabaseAbcodeRoom db, List<AMapLocationEntity> locations) {
        db.amapLocationDao().delete();
        db.amapLocationDao().resetAutoIncrement();
        db.amapLocationDao().insertAll(locations.toArray(new AMapLocationEntity[0]));
    }
    public static List<AMapLocationEntity> selectAMapLocation(final AppDatabaseAbcodeRoom db,String  name){
        return db.amapLocationDao().findByAdcode(name);
    }
}
