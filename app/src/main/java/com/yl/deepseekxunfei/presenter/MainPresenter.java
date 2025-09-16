package com.yl.deepseekxunfei.presenter;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.RecognizerListener;
import com.yl.basemvp.BasePresenter;
import com.yl.basemvp.SystemPropertiesReflection;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.fragment.MainFragment;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;
import com.yl.deepseekxunfei.view.PopupInputManager;
import com.yl.kuwo.MusicKuwo;
import com.yl.ylcommon.utlis.BotConstResponse;
import com.yl.ylcommon.utlis.TextLineBreaker;
import com.yl.ylcommon.utlis.TimeDownUtil;
import com.yl.deepseekxunfei.VoiceManager;
import com.yl.gaodeApi.poi.PositioningUtil;
import com.yl.ylcommon.utlis.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;


public class MainPresenter extends BasePresenter<MainActivity> {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private static final String API_URL = "http://47.106.73.32:11434/api/chat";
    public SpeechSynthesizer mTts;
    public SpeechRecognizer mIat;// 语音听写对象
    private SharedPreferences mSharedPreferences;//缓存
    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String language = "zh_cn";//识别语言
    private String resultType = "json";//结果内容数据格式
    public Deque<ChatMessage> contextQueue = new ArrayDeque<>();
    private static final int MAX_CONTEXT_TOKENS = 29000; // 预留3K tokens给新问题,因为上限为约32万避免新问题太长而导致问题不完整
    private static final int MAX_HISTORY_ROUNDS = 20; // 最多10轮对话
    private Call currentCall;
    private VoiceManager voiceManager = null;
    //是否停止输出
    public boolean isStopRequested = false;
    public boolean isNewChatCome = false;
    public List<ChatMessage> chatMessages = new ArrayList<>();
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "DeepSeek", "deepseek", "DEEPSEEK", "Deepseek", "deep seek", "Deep Seek"
    );//敏感词列表
    // XML标签常量
    private static final String START_TAG = "<think>";
    private static final String END_TAG = "</think>";
    private static final String LIMENDL_TAG = "<limendl>";
    private static final String IMEND_TAG = "<|imend|>";
    private static final String IMSTART_TAG = "<|imstart|>";
    private PopupInputManager inputManager;
    private OkHttpClient httpClient;
    public Boolean done = null;//是否正在输出

    @Override
    protected void onItemClick(View v) {
        if (v.getId() == R.id.deep_think_layout) {
            mActivity.get().changeDeepThinkMode();
        } else if (v.getId() == R.id.historyButton) {
            handleHistoryClick();
        } else if (v.getId() == R.id.xjianduihua) {
            mActivity.get().newChat();
        } else if (v.getId() == R.id.send_button) {
            mActivity.get().isRecognize = false;
            mActivity.get().handleSendButtonClick();

        } else if (v.getId() == R.id.deep_crete_layout) {
            mActivity.get().swOpenClose();
        } else if (v.getId() == R.id.wdxzskeyboard) {
            if (mIat != null && mIat.isListening()) {
                ToastUtil.show(mActivity.get(), "正在语音识别");
                return;
            }
            if (inputManager != null) {
                inputManager.show(mActivity.get());
            }
        }
    }

    private void handleHistoryClick() {
        mActivity.get().TTSbutton.setVisibility(View.VISIBLE);
        mActivity.get().animFree.start();
        mActivity.get().animRead.stop();
        mActivity.get().read_button.setVisibility(View.INVISIBLE);
        mActivity.get().animThink.stop();
        mActivity.get().think_button.setVisibility(View.INVISIBLE);
        mIat.stopListening();//停止
        mActivity.get().texte_microphone.setVisibility(View.INVISIBLE);
        mActivity.get().stopButton.setVisibility(View.INVISIBLE);
        if (mActivity.get() == null) return;
        mActivity.get().stopSpeaking();
        mActivity.get().isNeedWakeUp = true;
        mActivity.get().aiType = BotConstResponse.AIType.FREE;
        if (mActivity.get().uiHandler != null) {
            Log.d(TAG, "newChat: 执行");
            mActivity.get().uiHandler.removeCallbacks(mActivity.get().weatherStreamRunnable);
        }
        AppDatabase.getInstance(mActivity.get()).query(new AppDatabase.QueryCallBack() {
            @Override
            public void onCallBack(List<ChatHistoryEntity> chatHistoryEntities) {
                mActivity.get().showHistoryDialog(chatHistoryEntities);
            }
        });
    }

    public void initThirdApi() {
        //初始化动画效果
        mTts = SpeechSynthesizer.createSynthesizer(mActivity.get(), mInitListener);
        //初始话声纹识别必须参数

        // 加载本地知识库
        PositioningUtil positioning = new PositioningUtil();
        try {
            positioning.initLocation(mActivity.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(mActivity.get(), mInitListener);
        mSharedPreferences = mActivity.get().getSharedPreferences("ASR", Activity.MODE_PRIVATE);
        // 初始化输入管理器
        initInputManager();
        // 初始化HTTP客户端
        initHttpClient();
    }

    private void initInputManager() {
        inputManager = new PopupInputManager(mActivity.get(), new PopupInputManager.InputCallback() {
            @Override
            public void onInputChanged(String text) {
                Log.d(TAG, "输入变化: " + text);
            }

            @Override
            public void onInputCompleted(String text) {
                Log.d(TAG, "输入完成: " + text);
                if (mActivity.get() != null) {
                    mActivity.get().commitText(text);
                }
            }
        });
    }

    private void initHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = code -> {
        if (code != ErrorCode.SUCCESS) {
            mActivity.get().showMsg("初始化失败，错误码：" + code + ",请联系开发人员解决方案");
        }
    };

    /**
     * 参数设置
     */
    // 语音参数设置
    public void setParam() {
        if (mActivity.get() == null) return;

        String deepseekVoiceSpeed = SystemPropertiesReflection.get("persist.sys.deepseek_voice_speed", "60");
        String deepseekVoicespeaker = mapSpeakerName(
                SystemPropertiesReflection.get("persist.sys.deepseek_voice_speaker", "aisjiuxu")
        );

        // 设置语音合成参数
        setTtsParams(deepseekVoicespeaker, deepseekVoiceSpeed);

        // 设置语音识别参数
        setIatParams();
    }

    private String mapSpeakerName(String speakerName) {
        switch (speakerName) {
            case "许久":
                return "aisjiuxu";
            case "小萍":
                return "aisxping";
            case "小婧":
                return "aisjinger";
            case "许小宝":
                return "aisbabyxu";
            case "小燕":
                return "xiaoyan";
            default:
                return speakerName;
        }
    }

    private void setTtsParams(String speaker, String speed) {
        if (mTts == null) return;

        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME, speaker);
        mTts.setParameter(SpeechConstant.SPEED, speed);
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
    }

    private void setIatParams() {
        if (mIat == null) return;

        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);
        mIat.setParameter(SpeechConstant.LANGUAGE, language);
        mIat.setParameter("dwa", "wpgs");

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "5000"));
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "2000"));
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));
        mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "-1");
        mIat.setParameter(SpeechConstant.ASR_INTERRUPT_ERROR, mSharedPreferences.getString("iat_punc_preference", "0"));
    }

    public void stopSpeaking() {
        if (mTts != null) {
            // 1. 先获取Activity实例并判断是否为null
            MainActivity activity = mActivity.get();
            if (activity != null) {
                // 2. 再访问Activity的控件
                activity.texte_microphone.setVisibility(View.INVISIBLE);
                activity.stopButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    //文字转语音方法
    public void TTS(String str) {
        if (mTts == null || str == null || str.trim().isEmpty()) return;
        mActivity.get().aiType = BotConstResponse.AIType.READING;
        mActivity.get().TTSbutton.setVisibility(View.INVISIBLE);
        mActivity.get().animFree.stop();
        mActivity.get().read_button.setVisibility(View.VISIBLE);
        mActivity.get().animRead.start();
        mActivity.get().stopButton.setVisibility(View.VISIBLE);
        mActivity.get().animThink.stop();
        mActivity.get().think_button.setVisibility(View.INVISIBLE);
        Log.e(TAG, "123131312: " + str.trim());
        mTts.stopSpeaking();
        int code = mTts.startSpeaking(str.trim(), mSynListener);
        mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");

        if (code != ErrorCode.SUCCESS && mActivity.get() != null) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                mActivity.get().showMsg("语音组件未安装");
            } else {
                mActivity.get().showMsg("语音合成失败,错误码: " + code);
            }
        }
    }


    public void stopCurrentActivities() {
        stopSpeaking();
        stopCurrentCall();
        voiceManagerStop();
    }

    public void voiceManagerStop() {
        if (voiceManager != null) {
            voiceManager.mTts.stopSpeaking();
            voiceManager.release();
        }
    }

    public void stopCurrentCall() {
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
    }

    //开始监听
    public void startVoiceRecognize() {
        if (mIat == null || mActivity.get() == null) return;

        int ret = mIat.startListening(mRecognizerListener);

        if (ret != ErrorCode.SUCCESS) {
            mActivity.get().showMsg("听写失败，错误码：" + ret);
            return;
        }
    }

    // 简易估算token长度（实际应调用HuggingFace tokenizer）
    private int estimateTokens(String text) {
        return text.length() / 4; // 中文≈1token/2字，英文≈1token/4字符
    }

    public void callGenerateApi(String userQuestion) {
        mActivity.get().think_button.setVisibility(View.VISIBLE);
        mActivity.get().animThink.start();
        mActivity.get().animRead.stop();
        mActivity.get().animFree.stop();
        mActivity.get().TTSbutton.setVisibility(View.GONE);
        mActivity.get().read_button.setVisibility(View.GONE);
        if (userQuestion == null || userQuestion.isEmpty() || mActivity.get() == null) return;

        try {
            JSONObject requestBody = buildApiRequest(userQuestion);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            currentCall = httpClient.newCall(request);
            currentCall.enqueue(new ApiCallback(userQuestion));
        } catch (JSONException e) {
            Log.e(TAG, "JSON构建失败", e);
        }
    }

    private JSONObject buildApiRequest(String userQuestion) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text");

        JSONArray messages = new JSONArray();
        addSystemMessage(messages);
        addContextMessages(messages);
        addUserMessage(messages, userQuestion);

        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        requestBody.put("options", buildOptions());

        return requestBody;
    }

    private void addSystemMessage(JSONArray messages) throws JSONException {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "严格根据上下文回答问题，前置规则：上下文之间必须有一定的关联，否则重新回答问题");
        messages.put(systemMessage);
    }

    private void addContextMessages(JSONArray messages) throws JSONException {
        for (ChatMessage msg : contextQueue) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("role", msg.isUser() ? "user" : "assistant");
            jsonMsg.put("content", msg.getMessage());
            messages.put(jsonMsg);
        }
    }

    private void addUserMessage(JSONArray messages, String userQuestion) throws JSONException {
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userQuestion);
        messages.put(userMessage);
    }

    private JSONObject buildOptions() throws JSONException {
        JSONObject options = new JSONObject();
        options.put("temperature", 0.6);
        options.put("mirostat_tau", 1.0);
        options.put("num_predict", -1);
        options.put("repeat_last_n", 2048);
        options.put("repeat_penalty", 1.2);
        options.put("seed", generateRandomSeed());
        return options;
    }

    private long generateRandomSeed() {
        long num = 4294967295L;
        return (long) (Math.random() * num + 1);
    }

    // API回调处理
    private class ApiCallback implements Callback {
        private final String userQuestion;
        private final StringBuilder fullResponse = new StringBuilder();
        private final StringBuilder thinkText = new StringBuilder();
        private int botMessageIndex = -1;

        public ApiCallback(String userQuestion) {
            this.userQuestion = userQuestion;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            if (!"Socket closed".equals(e.getMessage()) && mActivity.get() != null) {
                handleNetworkError();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                mActivity.get().aiType = BotConstResponse.AIType.THINKING;
                processResponseStream(response.body());
            } else if (mActivity.get() != null) {
                mActivity.get().showMsg("请求失败: " + response.message());
            }
        }

        private void processResponseStream(ResponseBody body) throws IOException {
            if (body == null || mActivity.get() == null) return;

            try (BufferedSource source = body.source()) {
                voiceManager = new VoiceManager();
                voiceManager.init(mActivity.get());
                voiceManager.startProcessing();

                while (!source.exhausted()) {
                    if (shouldBreakProcessing()) break;

                    String line = source.readUtf8Line();
                    if (line != null && !line.isEmpty() && isValidJson(line)) {
                        processJsonResponse(line);
                    }
                }
            } finally {
                ensureCleanup();
            }
        }

        private boolean shouldBreakProcessing() {
            return isStopRequested || isNewChatCome || mActivity.get() == null;
        }

        private void processJsonResponse(String jsonLine) {
            JsonObject jsonResponse = new Gson().fromJson(jsonLine, JsonObject.class);

            if (!jsonResponse.has("message")) return;

            JsonObject messageObject = jsonResponse.getAsJsonObject("message");
            if (messageObject == null || !messageObject.has("content")) return;

            String partialResponse = messageObject.get("content").getAsString();
            done = jsonResponse.get("done").getAsBoolean();
            Log.d(TAG, "processJsonResponse: " + done);
            if (botMessageIndex == -1) {
                mActivity.get().addMessageByBot("");
                botMessageIndex = chatMessages.size() - 1;
                chatMessages.get(botMessageIndex).setNeedShowFoldText(false);
            }
            Log.d(TAG, "processJsonResponse: " + partialResponse);
            handleResponseContent(partialResponse, done);
        }

        private void handleResponseContent(String content, boolean done) {
            if (mActivity.get().aiType == BotConstResponse.AIType.THINKING) {
                mActivity.get().setAnimatorShowOrNo(2, 500);
            }
            if (isThinkTag(content)) {
                handleThinkTag(content);
            } else if (chatMessages.get(botMessageIndex).isThinkContent()) {
                thinkText.append(content);
                updateThinkContent();
            } else {
                fullResponse.append(content);
                if (voiceManager != null) {
                    voiceManager.appendText(content);
                }
                updateResponseContent(done);
            }
        }

        private boolean isThinkTag(String content) {
            return START_TAG.equals(content) || END_TAG.equals(content) ||
                    LIMENDL_TAG.equals(content) || IMEND_TAG.equals(content) ||
                    IMSTART_TAG.equals(content);
        }

        private void handleThinkTag(String tag) {
            if (START_TAG.equals(tag)) {
                chatMessages.get(botMessageIndex).setThinkContent(true);
            } else if (END_TAG.equals(tag) || LIMENDL_TAG.equals(tag) ||
                    IMEND_TAG.equals(tag) || IMSTART_TAG.equals(tag)) {
                chatMessages.get(botMessageIndex).setThinkContent(false);
            }
        }

        private void updateThinkContent() {
            if (mActivity.get() == null) return;

            mActivity.get().runOnUiThread(() -> {
                String filteredContent = filterSensitiveContent(
                        TextLineBreaker.breakTextByPunctuation(thinkText.toString()).trim());
                mActivity.get().setThinkContent(botMessageIndex, filteredContent);
                mActivity.get().notifyDataChanged(botMessageIndex);
            });
        }

        public void updateResponseContent(boolean done) {
            if (mActivity.get() == null) return;

            mActivity.get().runOnUiThread(() -> {
                String response = filterSensitiveContent(
                        TextLineBreaker.breakTextByPunctuation(fullResponse.toString()).trim());

                response = response.replace(START_TAG, "");
                mActivity.get().setTextByIndex(botMessageIndex, response);

                if (done && !isStopRequested) {
                    Log.d(TAG, "updateResponseContent: " + response + "\t" + done);
                    handleCompletedResponse(response);
                }

                mActivity.get().notifyDataChanged(botMessageIndex);
            });
        }

        private void handleCompletedResponse(String response) {
            contextQueue.add(new ChatMessage(userQuestion, true));
            contextQueue.add(new ChatMessage(fullResponse.toString(), false));

            while (contextQueue.size() > MAX_HISTORY_ROUNDS * 2) {
                contextQueue.removeFirst();
            }
            isStopRequested = true;
            isNewChatCome = false;
            mActivity.get().textFig = false;
            chatMessages.get(botMessageIndex).setNeedShowFoldText(true);
            chatMessages.get(botMessageIndex).setOver(true);
            mActivity.get().updateContext(userQuestion, fullResponse.toString(), false);
        }

        private void handleNetworkError() {
            isStopRequested = true;
            isNewChatCome = true;
            mActivity.get().textFig = false;
            mActivity.get().aiType = BotConstResponse.AIType.FREE;

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage("网络波动较大，请稍后再试");
            chatMessage.setOver(true);
            chatMessages.add(chatMessage);

            mActivity.get().runOnUiThread(() -> {
                mActivity.get().scrollByPosition(chatMessages.size() - 1);
            });
            mActivity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TTS("网络波动较大，请稍后再试");
                }
            });
        }

        private void ensureCleanup() {
            if (isStopRequested || isNewChatCome) {
                isNewChatCome = false;
                mActivity.get().textFig = false;

                if (botMessageIndex != -1) {
                    if (botMessageIndex > 0 && botMessageIndex < chatMessages.size()) {
                        chatMessages.get(botMessageIndex).setOver(true);
                    } else {
                        mActivity.get().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(mActivity.get(), "请等待当前问题回复完成或者点击停止");
                            }
                        });
                    }
                } else if (mActivity.get() != null) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage("不好意思，请您重新提问");
                    chatMessage.setOver(true);
                    mActivity.get().addMessageAndTTS(chatMessage, "不好意思，请您重新提问");
                }
            }
        }
    }

    public void clearContextQueue() {
        contextQueue.clear();
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        //音量变化回调
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        //开始说话回调
        @Override
        public void onBeginOfSpeech() {
            mActivity.get().stopButton.setVisibility(View.VISIBLE);
            Log.d(TAG, "讯飞: 开始说话");
        }

        //结束说话回调
        @Override
        public void onEndOfSpeech() {
            mActivity.get().hasAddMessageAtRecg = false;
            mActivity.get().texte_microphone.setVisibility(View.INVISIBLE);
            mActivity.get().stopButton.setVisibility(View.INVISIBLE);
            Log.d(TAG, "讯飞: 结束说话");
        }

        //识别结果回调
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.d(TAG, "讯飞合成: " + recognizerResult.getResultString());
            if (mActivity != null && mActivity.get() != null) {
                mActivity.get().recognizeResult(recognizerResult, b);
            } else {
                Log.d(TAG, "onResult: 为空");
            }
        }

        //识别错误回调
        @Override
        public void onError(SpeechError speechError) {
            mActivity.get().recognizeOnError(speechError);
        }

        //其他事件回调
        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    //合成监听器
    public SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            Log.d(TAG, "播放完毕");
            Log.d(TAG, "error" + error);
            mActivity.get().aiType = BotConstResponse.AIType.STANDBY;
            mActivity.get().TTSbutton.setVisibility(View.VISIBLE);
            mActivity.get().animStart();
            mActivity.get().animRead.stop();
            mActivity.get().read_button.setVisibility(View.INVISIBLE);
            mActivity.get().animRead.stop();
            mActivity.get().stopButton.setVisibility(View.INVISIBLE);
            if (error == null) {
                mActivity.get().onSpeakCompleted();
            }

        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

        }

        //开始播放
        public void onSpeakBegin() {
            mActivity.get().onSpeakBegin();
        }

        //暂停播放
        public void onSpeakPaused() {

        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {

        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

        }
    };

    //超时逻辑处理
    public void startTime(int position) {
        Log.d(TAG, "startTime12456888: 超时");
        Log.d(TAG, "超时: " + position);
        TimeDownUtil.startTimeDown(new TimeDownUtil.CountTimeListener() {
            @Override
            public void onTimeFinish() {
                if (mActivity != null && mActivity.get() != null) {
                    mActivity.get().onTimeFinish(position);
                }
            }
        });
    }

    // 检查JSON格式是否正确的方法
    private boolean isValidJson(String json) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.beginObject();
            while (reader.hasNext()) {
                reader.nextName();
                // 这里可以根据JSON数据的结构进行更详细的检查
                reader.skipValue();
            }
            reader.endObject();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void detach() {
        super.detach();
        releaseResources();
    }

    private void releaseResources() {
        if (mIat != null) {
            mIat.cancel();
            mIat.destroy();
            mIat = null;
        }
        // 检查Activity是否还存在再停止活动
        if (mActivity.get() != null) {
            stopCurrentActivities();
        }
        stopCurrentActivities();
    }


    public void stopListening() {
        mIat.stopListening();
    }

    public String filterSensitiveContent(String content) {
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

    /**
     * android 6.0 以上需要动态申请权限
     */
    public static final int REQUEST_CODE_RECORD_AUDIO = 100;
    public static final int REQUEST_CODE_STORAGE = 101;
    public static final int REQUEST_CODE_LOCATION = 102;
    public static final int REQUEST_CODE_MAC_ADDRESS = 103; // 新增MAC地址权限请求码

    /**
     * 请求录音权限
     */
    public void requestRecordAudioPermission() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(mActivity.get(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_RECORD_AUDIO);
        }
    }

    // 修改后的对话框方法
    private void showMacAddressPermissionDialog(PermissionRequestCallback callback) {
        new AlertDialog.Builder(mActivity.get())
                .setTitle("设备识别需要")
                .setMessage("我们需获取你的MAC地址以生成设备标识符...")
                .setPositiveButton("理解", (d, w) -> callback.onPermissionResult(true))
                .setNegativeButton("拒绝", (d, w) -> callback.onPermissionResult(false))
                .setOnCancelListener(d -> callback.onPermissionResult(false))
                .show();
    }

    public void MACAddressMain() {
        showMacAddressPermissionDialog(new PermissionRequestCallback() {
            @Override
            public void onPermissionResult(boolean isGranted) {
                if (isGranted) {
                    requestMacAddressPermission();
                } else {
                    //拒绝MAC权限处理

                }
            }
        });
    }

    /**
     * 请求MAC地址相关权限（需要WIFI状态和位置权限）
     */
    private void requestMacAddressPermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Android 6.0+ 需要WIFI状态权限
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.ACCESS_WIFI_STATE)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        // Android 10+ 需要位置权限才能获取真实MAC地址
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            if (shouldShowAnyPermissionRationale(permissionsNeeded)) {
                showMacAddressPermissionRationale(permissionsNeeded);
            } else {
                ActivityCompat.requestPermissions(mActivity.get(),
                        permissionsNeeded.toArray(new String[0]),
                        REQUEST_CODE_MAC_ADDRESS);
            }
        } else {
            // 已有权限，直接获取MAC地址
        }
    }

    public void handleMacAddressPermissionResult(String[] permissions, int[] grantResults) {
        boolean allGranted = true;
        boolean shouldShowRationale = false;

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity.get(), permissions[i])) {
                    shouldShowRationale = true;
                }
            }
        }

        if (allGranted) {
            mActivity.get().showMsg("权限已授予，正在获取MAC地址");
        } else {
            if (shouldShowRationale) {
                showMacAddressPermissionRationale(Arrays.asList(permissions));
            }
            mActivity.get().showMsg("无法获取MAC地址");
        }
    }

    /**
     * 检查是否需要显示权限解释
     */
    private boolean shouldShowAnyPermissionRationale(List<String> permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity.get(), permission)) {
                return true;
            }
        }
        return false;
    }

    // 定义回调接口
    public interface PermissionRequestCallback {
        void onPermissionResult(boolean isGranted);
    }

    /**
     * 显示MAC地址权限解释对话框
     */
    private void showMacAddressPermissionRationale(List<String> permissions) {
        new AlertDialog.Builder(mActivity.get())
                .setTitle("需要权限")
                .setMessage("获取设备MAC地址需要WIFI状态和位置权限")
                .setPositiveButton("授予权限", (dialog, which) ->
                        ActivityCompat.requestPermissions(mActivity.get(),
                                permissions.toArray(new String[0]),
                                REQUEST_CODE_MAC_ADDRESS))
                .setNegativeButton("取消", (dialog, which) ->
                        mActivity.get().showMsg("MAC地址功能将不可用"))
                .show();
    }

    /**
     * 请求文件存储权限 (包括读写)
     */
    private void requestStoragePermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mActivity.get(),
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_CODE_STORAGE);
        }
    }

    /**
     * 请求定位权限 (包括精确和粗略定位)
     */
    public void requestLocationPermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity.get(),
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mActivity.get(),
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_CODE_LOCATION);
        }
    }

    public void setNewChatCome(boolean newChatCome) {
        isNewChatCome = newChatCome;
    }

    public void setStopRequested(boolean stopRequested) {
        isStopRequested = stopRequested;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public int getChatMessagesSizeIndex() {
        if (chatMessages == null || chatMessages.isEmpty()) {
            return 0;
        }
        return chatMessages.size() - 1;
    }

    public int getChatMessagesSize() {
        if (chatMessages == null) {
            return 0;
        }
        return chatMessages.size();
    }

}
