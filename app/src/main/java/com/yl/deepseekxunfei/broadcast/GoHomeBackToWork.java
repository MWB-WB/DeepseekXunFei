package com.yl.deepseekxunfei.broadcast;

import android.content.Context;
import android.content.Intent;

import com.yl.deepseekxunfei.R;

/**
 * 回家以及回公司广播
 */
public class GoHomeBackToWork {
    public static  void goHomeToWord(int code,Context context) {
        Intent intent = new Intent();
        String appName = context.getString(R.string.app_name);
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10040);
        intent.putExtra("DEST", code);//0 回家；1 回公司(int
        intent.putExtra("IS_START_NAVI", 0);//是否直接开始导航 0 是；1 否
        intent.putExtra("SOURCE_APP", appName);
        context.sendBroadcast(intent);
    }
}
