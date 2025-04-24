package com.yl.deepseekxunfei.utlis;

import android.os.Handler;

public class TimeDownUtil {

    /**
     * 通过Handler延迟发送消息的形式实现定时任务。
     */
    public static final int CHANGE_TIPS_TIMER_INTERVAL = 1000;
    private static int count = 30;
    private static Handler mChangeTipsHandler = new Handler();

    public static void startTimeDown(CountTimeListener countTimeListener) {
        Runnable mChangeTipsRunnable = new Runnable() {
            @Override
            public void run() {
                if (count < 0) {
                    count = 10;
                    if (countTimeListener != null) {
                        countTimeListener.onTimeFinish();
                    }
                    return;
                }
                count--;
                mChangeTipsHandler.postDelayed(this, CHANGE_TIPS_TIMER_INTERVAL);
            }
        };

        mChangeTipsHandler.post(mChangeTipsRunnable);
    }

    public interface CountTimeListener {
        void onTimeFinish();
    }


}
