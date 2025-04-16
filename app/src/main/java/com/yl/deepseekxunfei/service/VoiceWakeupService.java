package com.yl.deepseekxunfei.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.yl.deepseekxunfei.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class VoiceWakeupService extends Service {

    private int curThresh = 1450;
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    private String keep_alive = "1";
    private String ivwNetMode = "0";
    private final String APP_ID = "27b3a946";

    private String TAG = VoiceWakeupService.class.getSimpleName();

    // 唤醒结果内容
    private String resultString;
    private final static String ACTION = "com.yl.voice.wakeup";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(this, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH,
                    getExternalFilesDir("msc").getAbsolutePath() + "/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            /*	mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");*/

            mIvw.startListening(mWakeuperListener);
        } else {
            showTip("唤醒未初始化");
        }
        return START_STICKY;
    }

    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + APP_ID + ".jet");
        Log.d(TAG, "resPath: " + resPath);
        return resPath;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            if (!"1".equalsIgnoreCase(keep_alive)) {

            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            Log.e(TAG, "onResult: " + resultString);
            if ("com.yl.deepseekxunfei".equals(getForegroundActivity())) {
                Intent broadcastIntent = new Intent(ACTION);
                sendBroadcast(broadcastIntent);
            } else {
                Intent xunFeiDialogIntent = new Intent(getApplicationContext(), MainActivity.class);
                xunFeiDialogIntent.putExtra("isStartRecord", true);
                xunFeiDialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(xunFeiDialogIntent);
            }
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));

        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    private void showTip(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public String getForegroundActivity() {
        ActivityManager mActivityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager.getRunningTasks(1) == null) {
            Log.e(TAG, "running task is null, ams is abnormal!!!");
            return null;
        }
        ActivityManager.RunningTaskInfo mRunningTask =
                mActivityManager.getRunningTasks(1).get(0);
        if (mRunningTask == null) {
            Log.e(TAG, "failed to get RunningTaskInfo");
            return null;
        }

        String pkgName = mRunningTask.topActivity.getPackageName();
        //String activityName =  mRunningTask.topActivity.getClassName();
        Log.e(TAG, "getForegroundActivity: " + pkgName);
        return pkgName;
    }

    /**
     * 释放连接
     */
    @Override
    public void onDestroy() {
        if (mIvw != null) {
            mIvw.cancel();
            mIvw.destroy();
        }
        super.onDestroy();
    }

}
