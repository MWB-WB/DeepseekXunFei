package com.yl.deepseekxunfei.utlis;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class PromptUtlis {

    private static final long DELAY_MILLIS = 5000; // 设置延迟时间，防止用户多次点击
    private boolean hasExecuted = false; // 标记是否已执行

    public void promptReply(Context context){
        if (!hasExecuted) {
            hasExecuted = true; // 设置已执行标记
            Log.d("等待", "promptReply: 等待");
            Toast.makeText(context, "请先等待上一个问题回复完成在进行提问", Toast.LENGTH_SHORT).show();
            // 使用 Handler 设置定时器
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasExecuted = false; // 重置已执行标记
                }
            }, DELAY_MILLIS);
        }
    }

    public void promptInput(Context context){
        if (!hasExecuted) {
            hasExecuted = true; // 设置已执行标记
            Toast.makeText(context, "请输入一个问题", Toast.LENGTH_SHORT).show();
            // 使用 Handler 设置定时器
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasExecuted = false; // 重置已执行标记
                }
            }, DELAY_MILLIS);
        }
    }

}
