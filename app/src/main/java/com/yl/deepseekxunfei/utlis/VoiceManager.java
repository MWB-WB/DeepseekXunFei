package com.yl.deepseekxunfei.utlis;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.yl.deepseekxunfei.MainActivity;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.model.ChatMessage;

import java.util.Arrays;
import java.util.List;

public class VoiceManager {
    private final StringBuilder mTextBuffer = new StringBuilder();
    private final Object mLock = new Object();
    private volatile boolean mIsSpeaking = false;
    public SpeechSynthesizer mTts;
    private HandlerThread mWorkThread;
    public MainActivity mainActivity;
    private Handler mHandler;
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "DeepSeek", "deepseek", "DEEPSEEK", "Deepseek", "deep seek", "Deep Seek"
    );//敏感词列表

    // 初始化语音引擎
    public void init(Context context) {
        // 创建后台工作线程
        mWorkThread = new HandlerThread("VoiceThread");
        mWorkThread.start();
        //初始化动画效果
        String deepseekVoiceSpeed = SystemPropertiesReflection.get("deepseek_voice_speed", "55");
        String deepseekVoicespeaker = SystemPropertiesReflection.get("deepseek_voice_speaker", "aisjiuxu");
        if (deepseekVoicespeaker.equals("许久")) {
            deepseekVoicespeaker = "aisjiuxu";
        } else if (deepseekVoicespeaker.equals("小萍")) {
            deepseekVoicespeaker = "aisxping";
        } else if (deepseekVoicespeaker.equals("小婧")) {
            deepseekVoicespeaker = "aisjinger";
        } else if (deepseekVoicespeaker.equals("许小宝")) {
            deepseekVoicespeaker = "aisbabyxu";
        } else if (deepseekVoicespeaker.equals("小燕")) {
            deepseekVoicespeaker = "xiaoyan";
        }

        String deepseekFontSize = SystemPropertiesReflection.get("deepseek_font_size", "20dp");
        String deepseekFontColor = SystemPropertiesReflection.get("deepseek_font_color", "黑色");
        String deepseekBackgroundColor = SystemPropertiesReflection.get("deepseek_background_color", "白色");
        mTts = SpeechSynthesizer.createSynthesizer(context, null);
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTts.setParameter(SpeechConstant.VOICE_NAME, deepseekVoicespeaker);//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, deepseekVoiceSpeed);//设置语速
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "55");//设置音高
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        //设置超时时间
//        mTts.setParameter(SpeechConstant.NET_TIMEOUT, "200");
        // 开启缓冲时间
        mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME, "100");
        if (context instanceof MainActivity) {
            this.mainActivity = (MainActivity) context;
        }
    }

    // 启动文本处理循环
    public void startProcessing() {
        if (mHandler == null) {
            mHandler = new Handler(mWorkThread.getLooper());
        }
        mHandler.post(() -> {
            while (!Thread.interrupted()) {
                processText();
                Log.e("1231231", "startProcessing: " + mTextBuffer.toString());
                try {
                    Thread.sleep(100); // 降低CPU占用
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    // 外部输入文本
    public void appendText(String newText) {
        synchronized (mLock) {
            mTextBuffer.append(newText);
        }
    }

    private ChatMessage chatMessage = new ChatMessage();

    private void processText() {
        synchronized (mLock) {
            if (!TextUtils.isEmpty(mTextBuffer) && mTextBuffer.toString().trim().length() >= 2 && !mIsSpeaking) {
                mainActivity.chatMessages.get(mainActivity.chatMessages.size() - 1).setSpeaking(true);
                String speakText = mTextBuffer.toString();
                mTextBuffer.delete(0, mTextBuffer.length());
                startSpeech(speakText);
            }
        }
    }

    private void startSpeech(String text) {
        Log.e("1231231", "startSpeech: " + text);
        mIsSpeaking = true;
        String TTSTexte = filterSensitiveContent(text);
        String TTSBiao = TextLineBreaker.breakTextByPunctuation(TTSTexte);
        // 在主线程执行语音播报
        new Handler(Looper.getMainLooper()).post(() -> {
            mTts.startSpeaking(TTSBiao, new SynthesizerListener() {
                @Override
                public void onSpeakBegin() {

                }

                @Override
                public void onBufferProgress(int i, int i1, int i2, String s) {

                }

                @Override
                public void onSpeakPaused() {

                }

                @Override
                public void onSpeakResumed() {

                }

                @Override
                public void onSpeakProgress(int i, int i1, int i2) {

                }

                @Override
                public void onCompleted(SpeechError error) {
                    if (mTextBuffer.toString().trim().length() <= 0) {
                        Log.d("TAG", "onCompleted: " + "");
                        mainActivity.button.setImageResource(R.drawable.jzfason);
                        mainActivity.aiType = BotConstResponse.AIType.FREE;
                        mainActivity.chatMessages.get(mainActivity.chatMessages.size() - 1).setSpeaking(false);
                        mainActivity.chatAdapter.notifyItemChanged(mainActivity.chatMessages.size() - 1);
                        release();
                        mainActivity.TTSbutton.performClick();
                    }
                    mIsSpeaking = false;
                    Log.d("TAG", "onCompleted: " + error);
                    if (error == null) {
                        Log.d("停止", "onCompleted: ");
                    }
                }

                @Override
                public void onEvent(int i, int i1, int i2, Bundle bundle) {

                }
            });
        });
    }

    // 释放资源
    public void release() {
        mTextBuffer.delete(0, mTextBuffer.length());
        if (mWorkThread != null) {
            mWorkThread.interrupt();
            mWorkThread.quitSafely();
        }
        if (mTts != null) {
            mTts.destroy();
        }
    }

    private String filterSensitiveContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        // 这里假设SENSITIVE_WORDS是已定义好的敏感词列表
        for (String word : SENSITIVE_WORDS) {
            // 定义你想要保留的提示信息
            String tipMessage = "实在不好意思，这个问题暂时难倒我啦。您可以换个方式提问，或者给我些相关线索，咱们一起探索答案～";
            // 检查当前敏感词是否在content中（忽略大小写）
            if (content.toLowerCase().contains(word.toLowerCase())) {
                // 如果包含，直接返回提示信息
                return tipMessage;
            }
        }
        // 如果没有匹配到任何敏感词，返回原内容
        return content;
    }
}