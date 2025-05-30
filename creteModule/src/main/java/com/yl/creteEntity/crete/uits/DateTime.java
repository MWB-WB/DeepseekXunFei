package com.yl.creteEntity.crete.uits;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.Date;

public class DateTime {
    public String Time(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 ");
// 获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String formattedDate = simpleDateFormat.format(date);
        Log.d("当前时间：", "Time: "+formattedDate);
        Log.d("当前时间：", "Time: "+formattedDate.toString());
        return formattedDate;
    }
}
