package com.yl.deepseekxunfei.presenter;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.RecognizerListener;
import com.yl.basemvp.BasePresenter;
import com.yl.basemvp.SystemPropertiesReflection;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.fragment.MainFragment;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.deepseekxunfei.view.PopupInputManager;
import com.yl.ylcommon.utlis.BotConstResponse;
import com.yl.ylcommon.utlis.TextLineBreaker;
import com.yl.ylcommon.utlis.TimeDownUtil;
import com.yl.deepseekxunfei.VoiceManager;
import com.yl.gaodeApi.poi.PositioningUtil;

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
    private SpeechSynthesizer mTts;
    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI
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
    private PopupInputManager inputManager;

    @Override
    protected void onItemClick(View v) {
        if (v.getId() == R.id.deep_think_layout) {
            mActivity.get().changeDeepThinkMode();
        } else if (v.getId() == R.id.historyButton) {
            mActivity.get().stopSpeaking();
            mActivity.get().isNeedWakeUp = true;
            AppDatabase.getInstance(mActivity.get()).query();
            // 显示历史记录对话框
            mActivity.get().showHistoryDialog();
        } else if (v.getId() == R.id.xjianduihua) {
            mActivity.get().newChat();
        } else if (v.getId() == R.id.send_button) {
//            mActivity.get().sendBtnClick();
        } else if (v.getId() == R.id.deep_crete_layout) {
            mActivity.get().swOpenClose();
        } else if (v.getId() == R.id.wdxzskeyboard) {
            inputManager.show(mActivity.get());
        }
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
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(mActivity.get(), mInitListener);
        mSharedPreferences = mActivity.get().getSharedPreferences("ASR", Activity.MODE_PRIVATE);
        inputManager = new PopupInputManager(mActivity.get(), new PopupInputManager.InputCallback() {
            @Override
            public void onInputChanged(String text) {
                Log.e(TAG, "onInputChanged: " + text);
            }

            @Override
            public void onInputCompleted(String text) {
                Log.e(TAG, "onInputCompleted: " + text);
                mActivity.get().commitText(text);
            }
        });
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
    public void setParam() {
        String deepseekVoiceSpeed = SystemPropertiesReflection.get("persist.sys.deepseek_voice_speed", "60");
        String deepseekVoicespeaker = SystemPropertiesReflection.get("persist.sys.deepseek_voice_speaker", "aisjiuxu");
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

        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTts.setParameter(SpeechConstant.VOICE_NAME, deepseekVoicespeaker);//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, deepseekVoiceSpeed);//设置语速
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");//设置音高
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "5000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "2000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));
        mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "-1");

        mIat.setParameter(SpeechConstant.ASR_INTERRUPT_ERROR, mSharedPreferences.getString("iat_punc_preference", "0")); // 允许中断
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
//        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    public void stopSpeaking() {
        if (mTts != null) {
            mTts.stopSpeaking();
        }
    }

    //文字转语音方法
    public void TTS(String str) {
        Log.e(TAG, "123131312: " + str.trim());
        mTts.stopSpeaking();
        int code = mTts.startSpeaking(str.trim(), mSynListener);
        Log.e(TAG, "TTS code: " + code);
        mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1"); // 支持流式
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //上面的语音配置对象为初始化时：
                mActivity.get().showMsg("语音组件未安装");
            } else {
                mActivity.get().showMsg("语音合成失败,错误码: " + code);
            }
        }
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
        if (null == mIat) {
            return;
        }
        //带UI界面
        mIatDialog.setListener(mRecognizerDialogListener);
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            mActivity.get().showMsg("听写失败，错误码：" + ret);
        }
        mIatDialog.show();
        //获取字体所在控件
        TextView txt = (TextView) mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
        txt.setText("请说出您的问题！");
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置为点击无反应，避免跳转到讯飞平台
            }
        });
    }

    // 简易估算token长度（实际应调用HuggingFace tokenizer）
    private int estimateTokens(String text) {
        return text.length() / 4; // 中文≈1token/2字，英文≈1token/4字符
    }

    public void callGenerateApi(String userQuestion) {
        // 使用 JSONObject 构建 JSON 请求体
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "text");
            JSONArray messages = new JSONArray();
            int currentTokens = 0;
            //添加系统提示
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "严格根据上下文回答问题，前置规则：上下文之间必须有一定的关联，否则重新回答问题");
            messages.put(systemMessage);
            // 1. 添加历史上下文（从旧到新）
            int i = 1;
            for (ChatMessage msg : contextQueue) {
                //进行上下文关联分析
                JSONObject jsonMsg = new JSONObject();
                //用户消息为true，系统消息为false
                jsonMsg.put("role", msg.isUser() ? "user" : "assistant");
                jsonMsg.put("content", msg.getMessage());
                messages.put(jsonMsg);
                //编码句子并计算相似度
                i++;
//                if (i>2){
//                    float[] emb1 = mActivity.get().embedder.encode(msg.getMessage());//历史回答
//                    float[] emb2 =  mActivity.get().embedder.encode(userQuestion);//当前用户问题
//                    float similarity = SBERTOnnxEmbedder.cosineSimilarity(emb1, emb2);
//                    Log.d("Tokens", "相似度: " + similarity);
//                }
            }

            // 添加当前用户问题
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userQuestion);
            Log.d(TAG, "callGenerateApiuserQuestion: " + userQuestion);
            messages.put(userMessage);
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            JSONObject options = new JSONObject();
            options.put("temperature", 0.6);
            options.put("mirostat_tau", 1.0);
            options.put("num_predict", -1);
            options.put("repeat_last_n", 2048);//检查全部上下文，避免重复回答
            options.put(" repeat_penalty", 1.2);//重复惩罚
            long num = 4294967295L;
            long randomNum = (long) (Math.random() * num + 1);
            Log.d(TAG, "随机数: " + randomNum);
            options.put("seed", randomNum);
            requestBody.put("options", options);
            // 将 JSONObject 转换为字符串
            String jsonBodyRound1 = requestBody.toString();
            Log.d(TAG, "上下: " + jsonBodyRound1);
            RequestBody requestBodyRound1 = RequestBody.create(jsonBodyRound1, MediaType.parse("application/json; charset=utf-8"));
            Request requestRound1 = new Request.Builder().url(API_URL).post(requestBodyRound1).build();
            Log.d(TAG, "callGenerateApi: " + requestRound1.toString().trim() + requestBodyRound1.toString().trim());
            Log.d(TAG, "callGenerateApi: " + requestRound1);
            // 异步执行请求
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)//连接超时
                    .readTimeout(5, TimeUnit.SECONDS)//读取超时
                    .writeTimeout(5, TimeUnit.SECONDS)//写入超时
                    .build();
            currentCall = client.newCall(requestRound1);

            currentCall.enqueue(new Callback() {
                // 用于存储第一轮完整响应
                StringBuilder fullResponseRound1 = new StringBuilder();
                StringBuilder thinkText = new StringBuilder();
                // 用于存储第一轮机器人消息记录的索引
                int botMessageIndexRound1 = -1;

                @Override
                public void onFailure(Call call, IOException e) {
                    //如果不是主动关闭的话需要进行网络波动的播报
                    if (!e.getMessage().equals("Socket closed")) {
                        isStopRequested = true;
                        isNewChatCome = true;
                        mActivity.get().textFig = false;
                        mActivity.get().button.setImageResource(R.drawable.jzfason);
                        mActivity.get().aiType = BotConstResponse.AIType.FREE;
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setMessage("网络波动较大，请稍后再试");
                        chatMessage.setOver(true);
                        chatMessages.add(chatMessage);
                        mActivity.get().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.get().scrollByPosition(chatMessages.size() - 1);
                            }
                        });
                        TTS("网络波动较大，请稍后再试");
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                BufferedSource source = responseBody.source();
                                mActivity.get().aiType = BotConstResponse.AIType.TEXT_SHUCHU;
                                voiceManager = new VoiceManager();
                                voiceManager.init(mActivity.get());
                                voiceManager.startProcessing();
                                while (!source.exhausted()) {
                                    if (isStopRequested || isNewChatCome) {
                                        isNewChatCome = false;
                                        mActivity.get().textFig = false;
                                        if (botMessageIndexRound1 != -1) {
                                            chatMessages.get(botMessageIndexRound1).setOver(true);
                                        } else {
                                            ChatMessage chatMessage = new ChatMessage();
                                            chatMessage.setMessage("不好意思，请您重新提问");
                                            chatMessage.setOver(true);
                                            mActivity.get().addMessageAndTTS(chatMessage, "不好意思，请您重新提问");
                                        }
                                        mActivity.get().button.setImageResource(R.drawable.jzfason);
                                        break;
                                    }
                                    String line = source.readUtf8Line();
                                    if (line != null && !line.isEmpty()) {
                                        // 检查 JSON 格式是否正确
                                        if (isValidJson(line)) {
                                            // 如果是第一条部分响应，添加一条空的机器人消息记录
                                            if (botMessageIndexRound1 == -1) {
                                                mActivity.get().addMessageByBot("");
                                                botMessageIndexRound1 = chatMessages.size() - 1;
                                                chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(false);
                                            }
                                            // 解析 JSON
                                            JsonObject jsonResponse = new Gson().fromJson(line, JsonObject.class);
                                            // 检查 message 字段是否存在
                                            if (jsonResponse.has("message")) {
                                                JsonObject messageObject = jsonResponse.getAsJsonObject("message");
                                                if (messageObject != null && messageObject.has("content")) {
                                                    String partialResponse = messageObject.get("content").getAsString();
                                                    boolean done = jsonResponse.get("done").getAsBoolean();
                                                    String startTag = "<think>";
                                                    String endTag = "</think>";
                                                    String limendl = "<limendl>";
                                                    String imend = "<|imend|>";
                                                    String imstart = "<|imstart|>";
                                                    if (startTag.equals(partialResponse) && !chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(true);
                                                        continue;
                                                    }
                                                    if (endTag.equals(partialResponse) && chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(false);
                                                        continue;
                                                    }
                                                    if (limendl.equals(partialResponse) && chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(false);
                                                        continue;
                                                    }
                                                    if (imend.equals(partialResponse) && chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(false);
                                                        continue;
                                                    }
                                                    if (imstart.equals(partialResponse) && chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(false);
                                                        continue;
                                                    }

                                                    if (chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        //思考内容
                                                        thinkText.append(partialResponse);
                                                    } else {
                                                        //回答文本主题
                                                        fullResponseRound1.append(partialResponse);
                                                        voiceManager.appendText(partialResponse);
                                                    }
                                                    // 更新 UI
                                                    mActivity.get().runOnUiThread(() -> {
                                                        String huida = "";
                                                        Log.d(TAG, "onResponse: " + botMessageIndexRound1);
                                                        if (chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                            huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(thinkText.toString())).trim();
                                                            // 更新机器人消息记录的内容
                                                            mActivity.get().setThinkContent(botMessageIndexRound1, huida);
                                                            Log.d(TAG, "onResponse: " + huida);
                                                        } else {
                                                            huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(fullResponseRound1.toString())).trim();
                                                            //缩进
                                                            if (huida.contains(startTag)) {
                                                                huida = huida.replace(startTag, "");
                                                            }
                                                            if (huida.length() < 0) {
                                                                huida = "";
                                                                // 更新机器人消息记录的内容
                                                                Log.d(TAG, "onResponse: " + huida);
                                                                mActivity.get().setTextByIndex(botMessageIndexRound1, huida);
                                                                TTS(huida);
                                                                return;
                                                            } else {
                                                                Log.d(TAG, "onResponse: " + huida);
                                                                // 更新机器人消息记录的内容
                                                                mActivity.get().setTextByIndex(botMessageIndexRound1, huida);
                                                            }
                                                        }
                                                        // 如果完成，停止读取
                                                        if (done && !isStopRequested) {
                                                            // 添加本轮对话到队列
                                                            contextQueue.add(new ChatMessage(userQuestion, true));
                                                            contextQueue.add(new ChatMessage(fullResponseRound1.toString(), false));

                                                            // 控制队列长度
                                                            while (contextQueue.size() > MAX_HISTORY_ROUNDS * 2) {
                                                                contextQueue.removeFirst();
                                                            }
                                                            isStopRequested = true;
                                                            isNewChatCome = false;
                                                            mActivity.get().textFig = false;
                                                            chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(true);
                                                            chatMessages.get(botMessageIndexRound1).setOver(true);
                                                            // 保存上下文信息
                                                            mActivity.get().updateContext(userQuestion, fullResponseRound1.toString(), false);
                                                        } else {
                                                            chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(false);
                                                        }
                                                        mActivity.get().notifyDataChanged(botMessageIndexRound1);

                                                    });
                                                }
                                            }
                                        } else {
                                            mActivity.get().runOnUiThread(() -> {
                                                mActivity.get().showMsg("JSON格式错误");
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        String errorBody = response.body().string();
                        mActivity.get().runOnUiThread(() -> {
                            mActivity.get().showMsg("\"请求失败: \" + response.message():" + errorBody);
                        });
                    }
                }
            });
            Log.d(TAG, "callGenerateApijsonBodyRound1234: " + jsonBodyRound1);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void clearContextQueue() {
        contextQueue.clear();
    }

    /**
     * 听写UI监听器
     */
    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            mActivity.get().recognizeResult(results, isLast);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            mActivity.get().recognizeOnError(error);
        }

    };
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        //音量变化回调
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        //开始说话回调
        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "讯飞: 开始说话");
        }

        //结束说话回调
        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "讯飞: 结束说话");
        }

        //识别结果回调
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.d(TAG, "讯飞合成: " + recognizerResult.getResultString());
            mActivity.get().recognizeResult(recognizerResult, b);
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
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            Log.d(TAG, "播放完毕");
            Log.d(TAG, "error" + error);
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
                mActivity.get().onTimeFinish(position);
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
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if (mIatDialog != null && mIatDialog.isShowing()) {
            mIatDialog.dismiss(); // 关闭对话框
        }
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
