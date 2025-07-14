package com.yl.deepseekxunfei.room.ulti;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yl.deepseekxunfei.room.AppDatabaseAbcodeRoom;
import com.yl.deepseekxunfei.room.dao.AMapLocationDao;
import com.yl.deepseekxunfei.room.entity.AMapLocationEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JSONReader {
    public static void insertJsonFileData(Context context, String fileName) {
        try {
            // 读取 JSON 文件内容
            String jsonData = readJsonFile(context, fileName);

            //  解析 JSON 数据
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<AMapLocationEntity>>() {}.getType();
            List<AMapLocationEntity> locations = gson.fromJson(jsonData, listType);

            //  初始化数据库
            AppDatabaseAbcodeRoom db = AppDatabaseAbcodeRoom.getDatabase(context);
            AMapLocationDao dao = db.amapLocationDao();
            // 将数据插入数据库
            DatabaseInitializer.populateAsync(db, locations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readJsonFile(Context context, String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
        return stringBuilder.toString();
    }
    public static List<AMapLocationEntity> select(Context context,String name){
        AppDatabaseAbcodeRoom db = AppDatabaseAbcodeRoom.getDatabase(context);
        AMapLocationDao dao = db.amapLocationDao();
        return  DatabaseInitializer.selectAMapLocation(db,name);
    }
}
