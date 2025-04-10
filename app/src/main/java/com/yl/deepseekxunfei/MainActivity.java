package com.yl.deepseekxunfei;


import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.widget.ImageButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iflytek.cloud.SpeechUtility;

import com.yl.deepseekxunfei.APICalls.NeighborhoodSearch;
import com.yl.deepseekxunfei.APICalls.WeatherAPI;
import com.yl.deepseekxunfei.adapter.ChatAdapter;
import com.yl.deepseekxunfei.fragment.MainFragment;
import com.yl.deepseekxunfei.fragment.MovieDetailFragment;
import com.yl.deepseekxunfei.fragment.RecyFragment;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ChatHistory;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.model.KnowledgeEntry;
import com.yl.deepseekxunfei.model.MovieDetailModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.page.LocationResult;
import com.yl.deepseekxunfei.scene.SceneManager;
import com.yl.deepseekxunfei.utlis.BotConstResponse;
import com.yl.deepseekxunfei.utlis.ContextHolder;
import com.yl.deepseekxunfei.utlis.JsonParser;
import com.yl.deepseekxunfei.utlis.KeyboardUtils;
import com.yl.deepseekxunfei.utlis.KnowledgeBaseLoader;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;
import com.yl.deepseekxunfei.utlis.SystemPropertiesReflection;
import com.yl.deepseekxunfei.utlis.TextLineBreaker;
import com.yl.deepseekxunfei.utlis.positioning;
import com.yl.deepseekxunfei.utlis.searchIn;

import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements WeatherAPI.OnWeatherListener, WeatherAPI.OnForecastWeatherListener {

    private static final String API_URL = "http://47.106.86.30:11434/api/chat ";

    private EditText editTextQuestion;

    private SpeechSynthesizer mTts;

    private static final String TAG = "MainActivity";

    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private SharedPreferences mSharedPreferences;//缓存

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String language = "zh_cn";//识别语言

    private String resultType = "json";//结果内容数据格式

    private EditText inputEditText;
    private ImageButton button;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    //是否停止输出
    private boolean isStopRequested = false;
    private boolean textFig;

    private List<KnowledgeEntry> knowledgeBase;
    private SceneManager sceneManager;

    private LinearLayout inputLayout;
    private ImageButton TTSbutton;
    //是否隐藏停止输出按钮
    boolean found = false;
    private List<ChatMessage> context = new ArrayList<>(); // 用于保存多轮对话的上下文

    //自定义语音识别UI
    private static final int SAMPLE_RATE = 44100; // 采样率
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private List<ChatHistory> chatHistories = new ArrayList<>(); // 保存所有历史记录
    private String currentTitle = ""; // 当前对话的标题
    private ImageButton history;//历史对话
    private ImageButton newDialogue;//新建对话
    private TextView titleTextView;//对话标题

    private long lastClickTime = 0;//第一次点击时间
    private static final long MIN_CLICK_INTERVAL = 3000; // 最小时间间隔为1秒
    private boolean YesNo = false;//是否改变按钮状态
    private WeatherAPI weatherAPI;
    private MainFragment mainFragment;
    private RecyFragment recyFragment;
    private MovieDetailFragment movieDetailFragment;
    private MyHandler myHandler;
    private String mWeatherResult;
    private BotConstResponse.AIType aiType = BotConstResponse.AIType.TEXT_NO_READY;
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "DeepSeek", "deepseek", "DEEPSEEK", "Deepseek", "deep seek", "Deep Seek"
    );//敏感词列表

    // 检查当前标题是否已经存在于历史记录中
    boolean isDuplicate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
        setContentView(R.layout.activity_main);
        initPermission(); // 权限请求
        ContextHolder.init(this); // 保存全局 Context
        initView();
        initThirdApi();
        myHandler = new MyHandler(this);
        sceneManager = new SceneManager();
        textFig = false;
    }

    private void initView() {
        //标题置顶
        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.bringToFront();
        editTextQuestion = findViewById(R.id.inputEditText);//输入框
        button = findViewById(R.id.send_button);//发送按钮
        TTSbutton = findViewById(R.id.wdxzs);
        // 获取输入区域的布局
        inputLayout = findViewById(R.id.submitLayout); // 输入区域布局的 id 为 layoutInput
        button.setImageResource(R.drawable.jzfason);
        button.setOnClickListener(v -> {
            if (aiType == BotConstResponse.AIType.TEXT_NO_READY) {
                Toast.makeText(MainActivity.this, "请输入一个问题", Toast.LENGTH_SHORT).show();
            } else if (aiType == BotConstResponse.AIType.TEXT_READY) {
                try {
                    replaceFragment(0);
                    sendMessage();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                mTts.stopSpeaking();
                isStopRequested = true;
                textFig = false;
                button.setImageResource(R.drawable.jzfason);
            }
        });
        // 添加全局布局监听器
        final View activityRootView = findViewById(android.R.id.content);
        ViewTreeObserver viewTreeObserver = activityRootView.getViewTreeObserver();
        editTextQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String shuru = s.toString().trim();
                Log.d(TAG, "是否为空: " + shuru);
                // 如果正在输出，不更新按钮状态
                if (textFig) {
                    return;
                }
                if (shuru.isEmpty()) {
                    button.setImageResource(R.drawable.jzfason);
                    aiType = BotConstResponse.AIType.TEXT_NO_READY;
                } else {
                    button.setImageResource(R.drawable.fason);
                    aiType = BotConstResponse.AIType.TEXT_READY;
                }
            }
        });

        //启动语音识别
        TTSbutton.setOnClickListener(v -> {
            isDuplicate = true;
            isStopRequested = false;
            editTextQuestion.setText("");
            //停止播放文本
            mTts.stopSpeaking();
            if (null == mIat) {
                Log.d(TAG, "创建对象失败，请确认libmsc.so放置正确，且有调用createUtility进行初始化");
                return;
            }
            mIatResults.clear();
            setParam();
            //带UI界面
            mIatDialog.setListener(mRecognizerDialogListener);
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
        });
        //输入框
        inputEditText = findViewById(R.id.inputEditText);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        // 隐藏主面板并添加历史记录
        inputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                try {
                    sendMessage();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            return false;
        });

        // 初始化视图
        titleTextView = findViewById(R.id.titleTextView);
        history = findViewById(R.id.historyButton);
        newDialogue = findViewById(R.id.xjianduihua);
        // 历史记录按钮点击事件
        history.setOnClickListener(v -> {
            mTts.stopSpeaking();
            if (!chatHistories.isEmpty()) {
                showHistoryDialog(); // 显示历史记录对话框
            } else {
                Toast.makeText(MainActivity.this, "没有历史记录", Toast.LENGTH_SHORT).show();
            }
        });
        // 新建对话按钮点击事件
        newDialogue.setOnClickListener(v -> {
            if (isStopRequested) {
                mTts.stopSpeaking();
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime >= MIN_CLICK_INTERVAL) {
                    lastClickTime = currentTime;
                    // 获取当前时间
                    String formattedDateTime = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        formattedDateTime = now.format(formatter);
                        System.out.println("当前日期和时间: " + formattedDateTime);
                    }


                    for (ChatHistory history : chatHistories) {
                        if (history.getTitle().equals(currentTitle)) {
                            isDuplicate = true;
                            Toast.makeText(MainActivity.this, "当前对话已存在历史记录，已将历史对话更新为最新内容", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (isDuplicate) {
                        chatMessages.clear();
                        chatAdapter.notifyDataSetChanged();
                        currentTitle = "";
                        titleTextView.setText("新对话"); // 重置标题
                    } else {
                        if (!currentTitle.isEmpty() && !chatMessages.isEmpty()) {
                            //清空上下文对话
                            context.clear();
                            // 保存当前对话到历史记录
                            chatHistories.add(new ChatHistory(currentTitle + formattedDateTime, new ArrayList<>(chatMessages)));
                            // 清空当前对话
                            chatMessages.clear();
                            chatAdapter.notifyDataSetChanged();
                            currentTitle = "";
                            titleTextView.setText("新对话"); // 重置标题
                        } else {
                            Toast.makeText(MainActivity.this, "当前没有历史记录", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请不要频繁点击", Toast.LENGTH_SHORT).show();
                }
            } else if (currentTitle.isEmpty()) {
                Toast.makeText(MainActivity.this, "当前没有对话记录", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "请等待输出完成，或者点击停止输出在新建对话 ", Toast.LENGTH_SHORT).show();
            }
        });
        mainFragment = new MainFragment();
        recyFragment = new RecyFragment();
        movieDetailFragment = new MovieDetailFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, mainFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, recyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, movieDetailFragment).commit();
        replaceFragment(0);
    }

    public void showDetailFragment(MovieDetailModel model) {
        replaceFragment(2);
        movieDetailFragment.setData(model);
    }

    public void showMovieFragment() {
        replaceFragment(1);
        recyFragment.getNowPlayingMovies();
    }

    public void commitText(String text) {
        chatMessages.add(new ChatMessage(text, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        SceneModel sceneModel = sceneManager.parseQuestionToScene(text);
        BaseChildModel baseChildModel = sceneManager.distributeScene(sceneModel);
        actionByType(baseChildModel);
    }

    private void initThirdApi() {
        //初始化动画效果
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
        // 加载本地知识库
        knowledgeBase = KnowledgeBaseLoader.loadKnowledgeBase(this);
        positioning positioning = new positioning();
        try {
            positioning.initLocation(MainActivity.this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
        mSharedPreferences = getSharedPreferences("ASR", Activity.MODE_PRIVATE);
        weatherAPI = new WeatherAPI();
        weatherAPI.setOnWeatherListener(this);
        weatherAPI.setOnForecastWeatherListener(this);
    }


    private void showHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("历史记录");

        // 获取历史记录标题列表
        List<String> historyTitles = new ArrayList<>();
        for (ChatHistory history : chatHistories) {
            historyTitles.add(history.getTitle());
        }

        // 将标题列表显示在对话框中
        builder.setItems(historyTitles.toArray(new String[0]), (dialog, which) -> {
            // 用户选择某个历史记录
            ChatHistory selectedHistory = chatHistories.get(which);
            // 显示选中的历史记录
            showHistoryMessages(selectedHistory);

        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showHistoryMessages(ChatHistory history) {
        // 清空当前对话
        chatMessages.clear();
        // 加载历史记录的对话内容
        chatMessages.addAll(history.getMessages());
        chatAdapter.notifyDataSetChanged();
        // 更新标题
        currentTitle = history.getTitle();
        titleTextView.setText(currentTitle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                KeyboardUtils.hideKeyboard(ev, view, this);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //添加到对话列表
    private void sendMessage() throws JSONException {
        found = false;
        String input = inputEditText.getText().toString().trim();
        if (!input.isEmpty()) {
            // 如果是第一次提问，将问题设置为对话标题
            if (currentTitle.isEmpty()) {
                currentTitle = input;
                titleTextView.setText(currentTitle); // 更新标题
            }
            // 添加问题到对话列表
            chatMessages.add(new ChatMessage(input, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            inputEditText.setText("");
            SceneModel sceneModel = sceneManager.parseQuestionToScene(input);
            BaseChildModel baseChildModel = sceneManager.distributeScene(sceneModel);
            actionByType(baseChildModel);
        }
    }

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = code -> {

        if (code != ErrorCode.SUCCESS) {
            showMsg("初始化失败，错误码：" + code + ",请联系开发人员解决方案");
        }
    };


    /**
     * 提示消息
     */
    private void showMsg(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void replaceFragment(int id) {
        hideFragment();
        if (id == 0) {
            if (mainFragment == null) {
                //设置fragment
                mainFragment = new MainFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.right_layout, mainFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().show(mainFragment).commit();
            }
        } else if (id == 1) {
            if (recyFragment == null) {
                //设置fragment
                recyFragment = new RecyFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.right_layout, recyFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().show(recyFragment).commit();
            }
            recyFragment.setRecyGone();
        } else if (id == 2) {
            if (movieDetailFragment == null) {
                //设置fragment
                movieDetailFragment = new MovieDetailFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.right_layout, movieDetailFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().show(movieDetailFragment).commit();
            }
        }
    }

    private void hideFragment() {
        if (mainFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(mainFragment).commit();
        }
        if (recyFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(recyFragment).commit();
        }
        if (movieDetailFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(movieDetailFragment).commit();
        }
    }

    /**
     * 释放连接
     */
    @Override
    protected void onDestroy() {


        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if (mIatDialog != null && mIatDialog.isShowing()) {
            mIatDialog.dismiss(); // 关闭对话框
        }
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        super.onDestroy();
    }

    /**
     * 参数设置
     */
    public void setParam() {
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
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "2000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "2000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
//        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 数据解析
     *
     * @param results
     */
    private void printResult(RecognizerResult results, boolean isLast) throws JSONException {
        String text = JsonParser.parseIatResult(results.getResultString());
        Log.d("识别内容", "printResult: " + text);

        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        // 只有最后一段才做最终判断
        if (!isLast) {
            return;
        }

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String finalText = resultBuffer.toString().trim();

        if (finalText.isEmpty()) {
            Toast.makeText(this, "您还没开始说话", Toast.LENGTH_SHORT).show();
            return;
        }
        chatMessages.add(new ChatMessage(finalText, true)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        SceneModel sceneModel = sceneManager.parseQuestionToScene(finalText);
        BaseChildModel baseChildModel = sceneManager.distributeScene(sceneModel);
        actionByType(baseChildModel);
    }

    /**
     * 听写UI监听器
     */
    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            try {
                printResult(results, isLast);//结果数据解析
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showMsg(error.getPlainDescription(true));
        }

    };
    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            Log.d(TAG, "播放完毕");
            if (error == null) {
                button.setImageResource(R.drawable.jzfason);
            }
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
            Log.d("SpeechSynthesizer", "开始播放");
            button.setImageResource(R.drawable.tingzhi);
            aiType = BotConstResponse.AIType.SPEAK;
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

    //初始话监听器
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Log.e("SpeechSynthesizer", "初始化失败, 错误码：" + code);
                Toast.makeText(MainActivity.this, "语音合成初始化失败, 错误码：" + code, Toast.LENGTH_SHORT).show();
            } else {
                Log.i("SpeechSynthesizer", "初始化成功");
            }
        }
    };

    private void callGenerateApi(String userQuestion) throws JSONException {
        textFig = true;
        button.setImageResource(R.drawable.tingzhi);
        mTts.stopSpeaking();
        // 使用 JSONObject 构建 JSON 请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-r1:32b");

        JSONArray messages = new JSONArray();

        // 添加上下文（如果有）
        for (ChatMessage message : context) {
            JSONObject contextMessage = new JSONObject();
            contextMessage.put("role", message.isUser() ? "user" : "assistant");
            contextMessage.put("content", message.getMessage());
            messages.put(contextMessage);
        }

        // 添加当前用户问题
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userQuestion); // userQuestion 已经过转义处理
        messages.put(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        JSONObject options = new JSONObject();
        options.put("temperature", 0.6);
        options.put("mirostat_tau", 0.5);
        requestBody.put("options", options);

        // 将 JSONObject 转换为字符串
        String jsonBodyRound1 = requestBody.toString();


        RequestBody requestBodyRound1 = RequestBody.create(jsonBodyRound1, MediaType.parse("application/json; charset=utf-8"));
        Request requestRound1 = new Request.Builder().url(API_URL).post(requestBodyRound1).build();
        Log.d(TAG, "请求: " + API_URL);
        // 异步执行请求
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        client.newCall(requestRound1).enqueue(new Callback() {
            // 用于存储第一轮完整响应
            StringBuilder fullResponseRound1 = new StringBuilder();
            StringBuilder thinkText = new StringBuilder();
            // 用于存储第一轮机器人消息记录的索引
            int botMessageIndexRound1 = -1;

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "错误: " + e.getMessage());
                isStopRequested = true;
                textFig = false;
                button.setImageResource(R.drawable.jzfason);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "网络波动较大，请稍后再试", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            BufferedSource source = responseBody.source();
                            aiType = BotConstResponse.AIType.TEXT_SHUCHU;
                            while (!source.exhausted()) {
                                Log.e(TAG, "onResponse: " + botMessageIndexRound1);
                                if (isStopRequested) {
                                    textFig = false;
                                    button.setImageResource(R.drawable.jzfason);
                                    break;
                                }
                                String line = source.readUtf8Line();
                                if (line != null && !line.isEmpty()) {
                                    // 检查 JSON 格式是否正确
                                    if (isValidJson(line)) {
                                        // 如果是第一条部分响应，添加一条空的机器人消息记录
                                        if (botMessageIndexRound1 == -1) {
                                            chatMessages.add(new ChatMessage("", false, "", false));
                                            botMessageIndexRound1 = chatMessages.size() - 1;
                                        }
                                        // 解析 JSON
                                        JsonObject jsonResponse = new Gson().fromJson(line, JsonObject.class);
                                        // 检查 message 字段是否存在
                                        if (jsonResponse.has("message")) {
                                            JsonObject messageObject = jsonResponse.getAsJsonObject("message");
                                            if (messageObject != null && messageObject.has("content")) {
                                                String partialResponse = messageObject.get("content").getAsString().trim();
                                                boolean done = jsonResponse.get("done").getAsBoolean();
                                                Log.d(TAG, "onResponse: " + jsonResponse);
                                                Log.d(TAG, "partialResponse: " + partialResponse);
                                                String startTag = "<think>";
                                                String endTag = "</think>";
                                                if (startTag.equals(partialResponse) && !chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                    chatMessages.get(botMessageIndexRound1).setThinkContent(true);
                                                    continue;
                                                }
                                                if (endTag.equals(partialResponse) && chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                    chatMessages.get(botMessageIndexRound1).setThinkContent(false);
                                                    chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(true);
                                                    continue;
                                                }

                                                if (chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                    //思考内容
                                                    thinkText.append(partialResponse);
                                                } else {
                                                    //回答文本主题
                                                    fullResponseRound1.append(partialResponse);
                                                }
                                                // 更新 UI
                                                runOnUiThread(() -> {
                                                    String huida = "";
                                                    if (chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(thinkText.toString()));
                                                        // 更新机器人消息记录的内容
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(huida);
                                                    } else {
                                                        //缩进
                                                        huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(fullResponseRound1.toString()));
                                                        // 更新机器人消息记录的内容
                                                        chatMessages.get(botMessageIndexRound1).setMessage(huida);
                                                    }
                                                    chatAdapter.notifyDataSetChanged();
                                                    chatRecyclerView.smoothScrollBy(0, chatRecyclerView.getLayoutManager().getHeight());
                                                    // 如果完成，停止读取
                                                    if (done) {
                                                        Log.d(TAG, "onResponse: 回答" + huida);
                                                        TTS(huida);
                                                        isStopRequested = true;
                                                        textFig = false;
                                                        button.setImageResource(R.drawable.jzfason);
                                                        Log.d("保存上下文信息", "run: " + fullResponseRound1.toString());
                                                        // 保存上下文信息

                                                        context.add(new ChatMessage(userQuestion, true));//用户消息
                                                        context.add(new ChatMessage(fullResponseRound1.toString(), false));//机器人消息
//                                                        Log.d("上下文更新：", ); // 打印上下文信息
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "JSON格式错误: " + line);
                                        runOnUiThread(() -> {
                                            Toast.makeText(MainActivity.this, "JSON格式错误", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            }
                        }
                    }
                } else {
                    String errorBody = response.body().string();
                    runOnUiThread(() -> {
                        Log.d("请求失败", "onResponse: " + errorBody);
                        Toast.makeText(MainActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onWeatherSuccess(LocalWeatherLive weatherLive) {
        chatMessages.remove(chatMessages.size() - 1);
        mWeatherResult = weatherLive.getCity() + "今天的天气" + weatherLive.getWeather() +
                "，当前的温度是" + weatherLive.getTemperature() + "摄氏度，" + weatherLive.getWindDirection() + "风"
                + weatherLive.getWindPower() + "级，" + "湿度" + weatherLive.getHumidity() + "%";
        TTS(mWeatherResult);
        chatMessages.add(new ChatMessage("", false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        weatherIndex = 0;
        myHandler.post(weatherStreamRunnable);

    }

    private int weatherIndex = 0;
    Runnable weatherStreamRunnable = new Runnable() {
        @Override
        public void run() {
            if (weatherIndex == mWeatherResult.length() - 1) {
                return;
            }
            chatMessages.set(chatMessages.size() - 1, new ChatMessage(mWeatherResult.substring(0, weatherIndex), false));
            chatAdapter.notifyDataSetChanged();
            weatherIndex++;
            myHandler.postDelayed(weatherStreamRunnable, 500);
        }
    };

    @Override
    public void onWeatherError(String message, int rCode) {
        chatMessages.remove(chatMessages.size() - 1);
        TTS(BotConstResponse.searchWeatherError);
        chatMessages.add(new ChatMessage(BotConstResponse.searchWeatherError, false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
    }

    @Override
    public void onWeatherSuccess(LocalWeatherForecastResult localWeatherForecastResult) {
        List<LocalDayWeatherForecast> weatherForecast = localWeatherForecastResult.getForecastResult().getWeatherForecast();
        for (LocalDayWeatherForecast localDayWeatherForecast : weatherForecast) {
            Log.d(TAG, "Date: " + localDayWeatherForecast.getDate() + ":: Weather: " + localDayWeatherForecast.getDayWeather()
                    + ":: DayWeather: " + localDayWeatherForecast.getDayWeather() + ":: NightWeather: " + localDayWeatherForecast.getNightWeather());
        }
    }

    class MyHandler extends Handler {
        private WeakReference<Activity> weakReference;

        public MyHandler(Activity activity) {
            weakReference = new WeakReference<>(activity);
        }
    }


    // 历史记录适配器点击事件接口
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    //文字转语音方法
    public void TTS(String str) {

        SystemPropertiesReflection.get("deepseek_voice_speed", "");
        String deepseekVoiceSpeed = SystemPropertiesReflection.get("deepseek_voice_speed", "50");
        String deepseekVoicespeaker = SystemPropertiesReflection.get("deepseek_voice_speaker", "xiaoyan");
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

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        // mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        // boolean isSuccess = mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts2.wav");
        // Toast.makeText(MainActivity.this, "语音合成 保存音频到本地：\n" + isSuccess, Toast.LENGTH_LONG).show();
        //3.开始合成
        int code = mTts.startSpeaking(str, mSynListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //上面的语音配置对象为初始化时：
                Toast.makeText(MainActivity.this, "语音组件未安装", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.INTERNET, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
        ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    /**
     * 权限申请回调，可以作进一步处理
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    private void updateContext(String userQuestion, String modelResponse) {
        context.add(new ChatMessage(userQuestion, true)); // 用户问题
        context.add(new ChatMessage(modelResponse, false)); // 模型回答

        // 限制上下文长度（避免过长）
        if (context.size() > 10) { // 保留最近的 10 轮对话
            context.remove(0);
            context.remove(0); // 同时移除一对问答
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

    // 显示搜索结果
    private void showSearchResults(List<LocationResult> results) {
        replaceFragment(1);
        recyFragment.showNavSearchResult(results);
    }

    // 处理返回键
    @Override
    public void onBackPressed() {
        if (recyFragment != null && recyFragment.isVisible()) {
            //给fragment处理
            replaceFragment(0);
        } else {
            super.onBackPressed();
        }
    }

    public static String extractLocation(String input) {
        // 匹配 "导航到XXX"、"去XXX"、"我要去XXX" 等模式
        String pattern = "(导航到|去|我要去|带我去|帮我找|附近有|我想去|附近的|导航去|导航)(.+?)(。|$)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);
        if (m.find()) {
            Log.d("地名", "extractLocation: " + m.group(2).trim());
            return m.group(2).trim(); // 返回匹配的地名
        }
        return input; // 如果没有匹配到，返回原输入（可能已经是纯地名）
    }

    /**
     * 沉浸式模式
     */
    private void enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    public void actionByType(BaseChildModel baseChildModel) {
        String botResponse = BotConstResponse.getSuccessResponse();
        switch (baseChildModel.getType()) {
            // 附近搜索
            case SceneTypeConst.NEARBY: {
                Log.d(TAG, "Search: 附近搜索");
                isStopRequested = true;
                // 先让机器人回复固定内容
                TTS(botResponse);
                chatMessages.add(new ChatMessage(botResponse, false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                String address = extractLocation(baseChildModel.getText());
                Log.d(TAG, "Search: " + address);
                NeighborhoodSearch.search(address, 5000, new OnPoiSearchListener() {
                    @Override
                    public void onSuccess(List<LocationResult> results) {
                        runOnUiThread(() -> showSearchResults(results));
                        if (results != null && !results.isEmpty()) {
                            Log.d("附近搜索", "onSuccess: ");
                        } else {
                            replaceFragment(0);
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "未查询到相关内容", Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("搜索失败", error);
                    }
                }, this);
                break;
            }
            // 关键字导航
            case SceneTypeConst.KEYWORD: {
                Log.d(TAG, "Search: 关键字");
                isStopRequested = true;
                // 先让机器人回复固定内容
                TTS(botResponse);
                chatMessages.add(new ChatMessage(botResponse, false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                String address = extractLocation(baseChildModel.getText());
                searchIn.searchInAmap(this, address, new OnPoiSearchListener() {
                    @Override
                    public void onSuccess(List<LocationResult> results) {
                        runOnUiThread(() -> showSearchResults(results));
                        if (results != null && !results.isEmpty()) {
                            Log.d("关键字导航", "onSuccess: ");
                        } else {
                            replaceFragment(0);
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "未查询到相关内容", Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.d(TAG, "onError: " + error);
                    }
                });
                break;
            }
            case SceneTypeConst.RECENT_FILMS:
                isStopRequested = true;
                // 先让机器人回复固定内容
                TTS(botResponse);
                chatMessages.add(new ChatMessage(botResponse, false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                showMovieFragment();
                break;
            //闲聊
            case SceneTypeConst.CHITCHAT: {
                // 搜索本地知识库
                for (KnowledgeEntry entry : knowledgeBase) {
                    if (entry.getTitle().equals(baseChildModel.getText())) {
                        isStopRequested = true;
                        String content = filterSensitiveContent(entry.getContent()); // 过滤敏感词
                        updateContext(baseChildModel.getText(), content); // 更新上下文
                        chatMessages.add(new ChatMessage(entry.getContent(), false));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        //停止播放文本
                        TTS(entry.getContent());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    isStopRequested = false;
                    try {
                        callGenerateApi(baseChildModel.getText());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case SceneTypeConst.TODAY_WEATHER:
                weatherAPI.weatherSearch(SceneTypeConst.TODAY_WEATHER);
                // 先让机器人回复固定内容
                TTS(BotConstResponse.searchWeatherWaiting);
                chatMessages.add(new ChatMessage(BotConstResponse.searchWeatherWaiting, false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                break;
            case SceneTypeConst.FEATHER_WEATHER:
                weatherAPI.weatherSearch(SceneTypeConst.FEATHER_WEATHER);
                // 先让机器人回复固定内容
                TTS(BotConstResponse.searchForecastWeatherWaiting);
                chatMessages.add(new ChatMessage(BotConstResponse.searchForecastWeatherWaiting, false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                break;
        }
    }
}