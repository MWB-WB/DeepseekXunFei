package com.yl.deepseekxunfei;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.Toast;

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

import com.yl.deepseekxunfei.APICalls.KwmusiccarApi;
import com.yl.deepseekxunfei.APICalls.NeighborhoodSearch;
import com.yl.deepseekxunfei.APICalls.OnPoiSearchListenerMusccar;
import com.yl.deepseekxunfei.APICalls.WeatherAPI;
import com.yl.deepseekxunfei.adapter.ChatAdapter;
import com.yl.deepseekxunfei.crete.CreateFeature;
import com.yl.deepseekxunfei.crete.CreateGroup;
import com.yl.deepseekxunfei.crete.CreateLogotype;
import com.yl.deepseekxunfei.crete.DeleteFeature;
import com.yl.deepseekxunfei.crete.DeleteGroup;
import com.yl.deepseekxunfei.crete.QueryFeatureList;
import com.yl.deepseekxunfei.crete.SearchFeature;
import com.yl.deepseekxunfei.crete.SearchOneFeature;
import com.yl.deepseekxunfei.fragment.MainFragment;
import com.yl.deepseekxunfei.fragment.MovieDetailFragment;
import com.yl.deepseekxunfei.fragment.RecyFragment;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ChatHistory;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.model.KnowledgeEntry;
import com.yl.deepseekxunfei.model.MovieDetailModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.page.LocationMusccarResult;
import com.yl.deepseekxunfei.page.LocationResult;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.deepseekxunfei.room.entity.ChatHistoryDetailEntity;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;
import com.yl.deepseekxunfei.scene.SceneManager;
import com.yl.deepseekxunfei.utlis.BotConstResponse;
import com.yl.deepseekxunfei.utlis.ContextHolder;
import com.yl.deepseekxunfei.utlis.CreteUtlis;
import com.yl.deepseekxunfei.utlis.JsonParser;
import com.yl.deepseekxunfei.utlis.KeyboardUtils;
import com.yl.deepseekxunfei.utlis.KnowledgeBaseLoader;
import com.yl.deepseekxunfei.utlis.OptionPositionParser;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;
import com.yl.deepseekxunfei.utlis.SystemPropertiesReflection;
import com.yl.deepseekxunfei.utlis.TextLineBreaker;
import com.yl.deepseekxunfei.utlis.TimeDownUtil;
import com.yl.deepseekxunfei.utlis.VoiceManager;
import com.yl.deepseekxunfei.utlis.positioning;
import com.yl.deepseekxunfei.utlis.searchIn;
import com.yl.deepseekxunfei.view.HistoryDialog;
import com.yl.douyinapi.DouyinApi;

import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements WeatherAPI.OnWeatherListener, WeatherAPI.OnForecastWeatherListener, View.OnClickListener {

    private static final String API_URL = "http://39.108.89.176:11434/api/chat ";
    public boolean fig = true;
    private boolean selectGroupFig = true;
    private int seleteSize = 0;//判断是不是第一次进行唤醒
    public int createOne = 0;//是否第一次注册，第一次注册不进行判断
    private EditText editTextQuestion;
    private boolean deleteFig = false;//是否进入删除成功回调
    MediaRecorder mediaRecorder = new MediaRecorder();
    private SpeechSynthesizer mTts;
    private StringBuilder speakTts = new StringBuilder();
    private boolean isInRound = false;
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
    public ImageButton button;
    private RecyclerView chatRecyclerView;
    public ChatAdapter chatAdapter;
    public TextView textCrete;
    public Button deleteCrete;
    public RelativeLayout relativeLayout;

    public List<ChatMessage> chatMessages = new ArrayList<>();

    //是否停止输出
    public boolean isStopRequested = false;
    public boolean isNewChatCome = false;
    private boolean textFig;

    private List<KnowledgeEntry> knowledgeBase;
    private SceneManager sceneManager;

    private LinearLayout inputLayout, mDeepThinkLayout;
    private ImageView mDeepThinkImg;
    private TextView mDeepThinkText;
    private boolean mIsDeepThinkMode = true;
    private ImageButton TTSbutton;
    //是否隐藏停止输出按钮
    public boolean found = false;
    private List<ChatMessage> context = new ArrayList<>(); // 用于保存多轮对话的上下文
    public int i = 0;
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
    public BotConstResponse.AIType aiType = BotConstResponse.AIType.TEXT_NO_READY;
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "DeepSeek", "deepseek", "DEEPSEEK", "Deepseek", "deep seek", "Deep Seek"
    );//敏感词列表

    // 检查当前标题是否已经存在于历史记录中
    boolean isDuplicate = false;
    //初始话科大讯飞声纹识别
    private String APP_ID;
    private String APISecret;
    private String APIKey;
    private String requestUrl;

    //声纹识别所需文件保存路径
    private String creteFlies;
    //对比声纹文件
    String contrastFies;
    //全局声纹识别创建文件工具类
    private CreteUtlis creteUtlis = new CreteUtlis();
    //开始录音按钮
    private Button kaishiluyin;
    // media_ecorder_recording.xml 布局中的开始录音按钮
    private Button btnStart;
    // media_ecorder_recording.xml 布局中的停止录音按钮
    private Button btnStop;
    // media_ecorder_recording.xml 布局中的文本显示控件
    private TextView textTime;
    private CreateLogotype createLogotype = new CreateLogotype();
    private Call currentCall;
    // 创建自定义 Dialog
    Dialog customDialog;
    RadioGroup radio_teacher;
    RadioButton radioButtonChe;
    RadioButton radioButtonPy;
    RadioButton radioButtonJr;
    View layout;
    // 获取选中的按钮文本
    String selectedText;
    private List<SceneModel> sceneModelList = new ArrayList<>();
    final Map<String, String>[] SearchOneFeatureList = new Map[]{new HashMap<>()}; //1:1服务结果
    final Map<String, String>[] result = new Map[]{new HashMap<>()};//1:N服务结果
    final Map<String, String>[] group = new Map[]{new HashMap<>()};//创建特征库服务结果
    final Map<String, String>[] querySelect = new Map[]{new HashMap<>()};//查询结果
    List<String> groupIdList = new ArrayList<>();//分组标识
    List<String> groupNameList = new ArrayList<>();//声纹分组名称
    List<String> groupInfoLsit = new ArrayList<>();//分组描述信息
    List<String> featureIdList = new ArrayList<>();//特征唯一标识
    List<String> featureInfoList = new ArrayList<>();//特征描述
    int che = 0;//判断是不是第一次注册车主声纹

    Button selectCrete;//删除声纹信息
    private VoiceManager voiceManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
        setContentView(R.layout.activity_main);
        initPermission(); // 权限请求
        ContextHolder.init(this); // 保存全局 Context
//        voiceManager = new VoiceManager();
//        voiceManager.init(MainActivity.this);
        initView();
        initThirdApi();
        registerBroadCast();
        myHandler = new MyHandler(this);
        sceneManager = new SceneManager();
        textFig = false;
        setParam();
        chatMessages.add(new ChatMessage("我是小天，很高兴见到你！", false, "", false));
        chatMessages.get(chatMessages.size() - 1).setOver(true);
        TTS("我是小天，很高兴见到你！");
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatMessages.size() > 0) {
            chatAdapter.notifyDataSetChanged();
        }
        String text = "";
        try {
            text = SystemPropertiesReflection.get("persist.sys.yl.text", "");
            Log.e(TAG, "onResume: " + text);
            SystemPropertiesReflection.set("persist.sys.yl.text", "");
        } catch (Exception e) {
            text = "";
            SystemPropertiesReflection.set("persist.sys.yl.text", "");
        }
        if (!TextUtils.isEmpty(text)) {
            commitText(text);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTts.stopSpeaking();
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        isStopRequested = true;
        textFig = false;
        isInRound = false;
        setCurrentChatOver();
        aiType = BotConstResponse.AIType.FREE;
        button.setImageResource(R.drawable.jzfason);
        if (!chatMessages.isEmpty()) {
            chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
            chatAdapter.notifyItemChanged(chatMessages.size() - 1);
        }
    }

    private void registerBroadCast() {
        seleteSize++;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "registerBroadCast();: " + intent);
                if ("com.yl.voice.wakeup".equals(intent.getAction())) {
                    if (seleteCrete()) {
                        if (!createFig()) {
                            startVoiceRecognize();
                        }
                    } else {
                        if (seleteSize != 0) {
                            Toast.makeText(MainActivity.this, "暂无声纹信息，请注册", Toast.LENGTH_SHORT).show();
                            seleteSize++;
                        }
                    }
                } else if ("com.yl.voice.test.start".equals(intent.getAction())) {
                    String result = intent.getStringExtra("result");
                    commitText(result);
                } else if ("com.yl.voice.test.stop".equals(intent.getAction())) {
                    if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                        if (button != null) {
                            button.performClick();
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.yl.voice.wakeup");
        filter.addAction("com.yl.voice.test.start");
        filter.addAction("com.yl.voice.test.stop");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }
    }

    private void initView() {
        customDialog = new Dialog(MainActivity.this);
        //标题置顶
        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.bringToFront();
        mDeepThinkLayout = findViewById(R.id.deep_think_layout);
        mDeepThinkLayout.setOnClickListener(this);
        mDeepThinkImg = findViewById(R.id.deep_think_img);
        mDeepThinkText = findViewById(R.id.deep_think_text);
        editTextQuestion = findViewById(R.id.inputEditText);//输入框
        button = findViewById(R.id.send_button);//发送按钮
        TTSbutton = findViewById(R.id.wdxzs);
        kaishiluyin = findViewById(R.id.kaishiluyin);
        // 获取输入区域的布局
        inputLayout = findViewById(R.id.submitLayout); // 输入区域布局的 id 为 layoutInput
        button.setImageResource(R.drawable.jzfason);
        // 获取容器

        //获取录音弹出的布局
        // 动态加载 ConstraintLayout
//        LayoutInflater inflater = LayoutInflater.from(this);
//        ConstraintLayout dynamicLayout = (ConstraintLayout) inflater.inflate(
//                R.layout.media_ecorder_recording, // 你的 ConstraintLayout XML 文件
//                null, // 直接附加到父布局
//                true
//        );

        button.setOnClickListener(v -> {
            Log.e(TAG, "button onclick: " + aiType);
            if (aiType == BotConstResponse.AIType.TEXT_NO_READY) {
                Toast.makeText(MainActivity.this, "请输入一个问题", Toast.LENGTH_SHORT).show();
            } else if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
                try {
                    if (chatMessages.isEmpty()) {
                        replaceFragment(0);
                        sendMessage();
                    } else {
                        if (!chatMessages.get(chatMessages.size() - 1).isOver()) {
                            Toast.makeText(MainActivity.this, "请先等待上一个问题回复完成在进行提问（提交按钮）", Toast.LENGTH_SHORT).show();
                        } else {
                            replaceFragment(0);
                            sendMessage();
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                mTts.stopSpeaking();
                if (currentCall != null) {
                    currentCall.cancel();
                    currentCall = null;
                }
                isStopRequested = true;
                textFig = false;
                setCurrentChatOver();
                if (voiceManager!=null){
                    voiceManager.mTts.stopSpeaking();
                    voiceManager.release();
                }
                aiType = BotConstResponse.AIType.FREE;
                button.setImageResource(R.drawable.jzfason);
                chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
                chatAdapter.notifyItemChanged(chatMessages.size() - 1);
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
            startVoiceRecognize();
        });
        //输入框
        inputEditText = findViewById(R.id.inputEditText);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        // 在初始化 RecyclerView 时禁用动画
        chatRecyclerView.setItemAnimator(null);
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
        history.setOnClickListener(this);
        newDialogue.setOnClickListener(this);
        mainFragment = new MainFragment();
        recyFragment = new RecyFragment();
        movieDetailFragment = new MovieDetailFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, mainFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, recyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, movieDetailFragment).commit();
        replaceFragment(0);
        kaishiluyin.setOnClickListener(v -> {
            customDialog.setContentView(R.layout.media_ecorder_recording);
            //初始化单选按钮
            radio_teacher = customDialog.findViewById(R.id.radio_group_identity);
            radioButtonChe = customDialog.findViewById(R.id.radio_teacher);
            radioButtonPy = customDialog.findViewById(R.id.radio_friend);
            radioButtonJr = customDialog.findViewById(R.id.radio_student);
            // 1. 先创建录音文件
            creteFlies = creteUtlis.createAudioFilePath(MainActivity.this);
            Log.e(TAG, "initView: " + creteFlies);
            creteUtlis.startRecord(new WeakReference<>(this), creteFlies);
            contrastFies = creteUtlis.createAudioFilePath(MainActivity.this);
            Log.e(TAG, "initView: " + contrastFies);
            creteUtlis.startRecord(new WeakReference<>(this), contrastFies);
            // 获取 media_ecorder_recording.xml 布局中的按钮和文本控件
            btnStart = customDialog.findViewById(R.id.btn_start);
            // 找到关闭按钮
            Button closeDialogButton = customDialog.findViewById(R.id.btn_stop);
            selectCrete = customDialog.findViewById(R.id.query_selete);
            relativeLayout = customDialog.findViewById(R.id.pathRelative);
            // 显示自定义 Dialog
            customDialog.show();
            // 设置关闭按钮点击事件
            closeDialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 关闭弹窗
                    customDialog.dismiss();
                    if (isRecording) {
                        try {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            isRecording = false;
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            //开始录音
            btnStart.setOnClickListener(v1 -> {
                mIatDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        try {
                            createVoiceprint(recognizerResult, b);//调用解析，解析中调用创建声纹库，对比等操作
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }
                });
                mIatDialog.show();
                //获取字体所在控件
                TextView txt = (TextView) mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
                txt.setText("您可以朗读一段文本，为保证声纹识别的准确度，请保证录音时长在5到10秒之间");
                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //设置为点击无反应，避免跳转到讯飞平台
                    }
                });
            });
            //查询声纹信息
            selectCrete.setOnClickListener(select -> {
                seleteCrete();
            });
        });
    }

    //查询声纹特征的方法
    public boolean seleteCrete() {

        // 检查groupId是否存在
        Log.d(TAG, "initView: " + createLogotype.getGroupId());
        if (createLogotype.getGroupId() != null && !createLogotype.getGroupId().isEmpty()) {
            // 开始查询前清空并显示容器
            relativeLayout.removeAllViews();
            relativeLayout.setVisibility(View.VISIBLE);

            // 用于存储所有结果的列表
            final List<JSONObject> allResults = new ArrayList<>();


            // 记录groupId总数和已完成的查询数
            final int totalGroups = createLogotype.getGroupId().size();
            final AtomicInteger completedGroups = new AtomicInteger(0);

            // 遍历所有groupId进行查询
            for (int j = 0; j < totalGroups; j++) {
                final String groupId = createLogotype.getGroupId().get(j);
                QueryFeatureList.doQueryFeatureList(requestUrl, APP_ID, APISecret, APIKey, createLogotype, groupId,
                        new QueryFeatureList.NetCall() {
                            @Override
                            public void OnSuccess(String success) {
                                try {
                                    JSONArray jsonArray = new JSONArray(success);
                                    Log.d(TAG, "查询groupId " + groupId + " 结果: " + jsonArray.toString());

                                    // 将当前groupId的结果添加到总结果列表
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject item = jsonArray.getJSONObject(i);
                                        item.put("groupId", groupId); // 保存groupId到结果中
                                        allResults.add(item);
                                    }

                                    // 检查是否所有groupId都查询完成
                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        // 所有查询完成后，统一更新UI
                                        runOnUiThread(() -> updateUIWithResults(allResults));
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "解析JSON失败", e);
                                }
                                Log.d(TAG, "seleteCrete: " + allResults);
                            }

                            @Override
                            public void OnError() {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "查询groupId " + groupId + " 失败", Toast.LENGTH_SHORT).show();
                                    // 即使部分查询失败，也继续处理已有的结果
                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        updateUIWithResults(allResults);
                                    }
                                });
                            }
                        });
            }
            return true;
        } else {
            if (!deleteFig) {
                Toast.makeText(MainActivity.this, "暂无声纹信息，请注册", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    // 删除声纹特征的方法
    private void deleteFeature(String groupId, String featureId, int viewId) {
        Log.d(TAG, "删除声纹: GroupID=" + groupId + ", FeatureID=" + featureId);
        // 显示加载中
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在删除...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // 执行删除请求
        DeleteFeature.doDeleteFeature(requestUrl, APP_ID, APISecret, APIKey, createLogotype, groupId, featureId, new DeleteFeature.NetCallDeleteCrete() {
            @Override
            public void OnSuccess(String success) {
                DeleteGroup.doDeleteGroup(requestUrl, APP_ID, APISecret, APIKey, groupId, new DeleteGroup.NetDeleteGroup() {
                    @Override
                    public void OnSuccessGroup(String success) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            deleteFig = true;
                            //根据删除的id获取到对应的下标，在根据下标删除对应的描述信息和声纹唯一标识
                            createLogotype.getGroupInfo().remove(createLogotype.getGroupId().indexOf(groupId));
                            createLogotype.getGroupName().remove(createLogotype.getGroupId().indexOf(groupId));
                            createLogotype.getFeatureInfo().remove(createLogotype.getGroupId().indexOf(groupId));
                            createLogotype.getFeatureId().remove(featureId);
                            createLogotype.getGroupId().remove(groupId);
                            Log.d(TAG, "OnSuccessGroup: " + createLogotype);
                            boolean fig = seleteCrete();
                            if (!fig) {
                                // 关闭弹窗
                                customDialog.dismiss();
                                if (isRecording) {
                                    try {
                                        mediaRecorder.stop();
                                        mediaRecorder.release();
                                        isRecording = false;
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void OnErrorGroup() {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "删除特征库失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void OnError() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "删除声纹特征失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 统一更新UI的方法
    private void updateUIWithResults(List<JSONObject> results) {
        // 清空之前的视图
        relativeLayout.removeAllViews();

        // 记录上一个视图的ID
        int lastViewId = 0;

        // 遍历所有结果，创建视图
        for (JSONObject item : results) {
            try {
                String featureInfo = item.getString("featureInfo");
                String featureId = item.getString("featureId");
                String groupId = item.getString("groupId"); // 获取保存的groupId

                // 创建一个线性布局作为条目容器
                LinearLayout itemLayout = new LinearLayout(MainActivity.this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 0, 0, 10);

                // 生成唯一的viewId
                int itemViewId = View.generateViewId();
                itemLayout.setId(itemViewId);

                // 设置布局参数
                RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

                // 如果不是第一个视图，则放在上一个视图的下方
                if (lastViewId != 0) {
                    itemParams.addRule(RelativeLayout.BELOW, lastViewId);
                }

                itemLayout.setLayoutParams(itemParams);

                // 创建文本视图
                TextView textView = new TextView(MainActivity.this);
                textView.setText(featureInfo);
                textView.setTextSize(18);
                textView.setPadding(16, 16, 16, 16);
                textView.setBackgroundResource(android.R.color.holo_blue_light);

                // 设置文本视图的布局参数，使其占据大部分空间
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0, // 宽度为0，由weight决定
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f // 权重为1，占据剩余空间
                );
                textView.setLayoutParams(textParams);

                // 创建删除按钮
                Button deleteButton = new Button(MainActivity.this);
                deleteButton.setText("删除");
                deleteButton.setBackgroundResource(android.R.color.holo_red_light);
                deleteButton.setTextColor(Color.WHITE);

                // 设置删除按钮的布局参数
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                buttonParams.setMargins(10, 0, 0, 0);
                deleteButton.setLayoutParams(buttonParams);

                // 为删除按钮设置点击事件，绑定groupId和featureId
                deleteButton.setOnClickListener(v2 -> {
                    try {
                        // 显示确认对话框
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("确认删除")
                                .setMessage("确定要删除此声纹吗？\nGroup ID: " + groupId + "\nFeature ID: " + featureId)
                                .setPositiveButton("确认", (dialog, which) -> {
                                    // 执行删除操作
                                    deleteFeature(groupId, featureId, itemViewId);
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    } catch (Exception e) {
                        Log.e(TAG, "删除按钮点击事件异常", e);
                    }
                });
                // 将文本视图和删除按钮添加到条目布局
                itemLayout.addView(textView);
                itemLayout.addView(deleteButton);

                // 将条目布局添加到主布局
                relativeLayout.addView(itemLayout);

                // 更新最后一个视图的ID
                lastViewId = itemViewId;

            } catch (JSONException e) {
                Log.e(TAG, "处理结果失败", e);
            }
        }
    }

    private void startVoiceRecognize() {
        setParam();
        // 1. 先创建录音文件
        contrastFies = creteUtlis.createAudioFilePath(MainActivity.this);
        Log.e(TAG, "initView: " + contrastFies);
        creteUtlis.startRecord(new WeakReference<>(this), contrastFies);
        isDuplicate = true;
        isStopRequested = false;
        if (aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
            isNewChatCome = true;
        }
        editTextQuestion.setText("");
        //停止播放文本
        mTts.stopSpeaking();
        if (null == mIat) {
            Log.d(TAG, "创建对象失败，请确认libmsc.so放置正确，且有调用createUtility进行初始化");
            return;
        }
        mIatResults.clear();
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
    }

    public void showDetailFragment(MovieDetailModel model) {
        replaceFragment(2);
        movieDetailFragment.setData(model);
    }

    public void showMovieFragment() {
        replaceFragment(1);
        recyFragment.getNowPlayingMovies();
    }

    public void showNearbyCinemaFragment() {
        replaceFragment(1);
        recyFragment.getNearbyCinema();
    }

    public void commitText(String text) {
        if (chatMessages.isEmpty()) {
            chatMessages.add(new ChatMessage(text, true, "", false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            SceneModel sceneModel = sceneManager.parseQuestionToScene(text);
            sceneModelList.add(sceneModel);
            BaseChildModel baseChildModel = sceneManager.distributeScene(sceneModel);
            actionByType(baseChildModel);
        } else {
            if (!chatMessages.get(chatMessages.size() - 1).isOver() || aiType == BotConstResponse.AIType.SPEAK) {
                Toast.makeText(MainActivity.this, "请先等待上一个问题回复完成在进行提问", Toast.LENGTH_SHORT).show();
            } else {
                chatMessages.add(new ChatMessage(text, true, "", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                SceneModel sceneModel = sceneManager.parseQuestionToScene(text);
                sceneModelList.add(sceneModel);
                BaseChildModel baseChildModel = sceneManager.distributeScene(sceneModel);
                actionByType(baseChildModel);
            }
        }
    }

    private void initThirdApi() {
        //初始化动画效果
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
        //初始话声纹识别必须参数
        APP_ID = "27b3a946";
        APISecret = "MGNhOTM2Yjg3MmVhMTFjYzhhODQzMTYw";
        APIKey = "06224092793087296b1f47c96e0133bc";
        requestUrl = "http://api.xf-yun.com/v1/private/s782b4996";
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
        HistoryDialog dialog = new HistoryDialog(this, AppDatabase.getInstance(this).getChatHistoryEntities());
        dialog.show();
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
            chatMessages.add(new ChatMessage(input, true, "", false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            inputEditText.setText("");
            SceneModel sceneModel = sceneManager.parseQuestionToScene(input);
            sceneModelList.add(sceneModel);
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

    private String getTitle(List<ChatHistoryDetailEntity> chatHistoryDetailEntities) {
        for (ChatHistoryDetailEntity chatHistoryDetailEntity : chatHistoryDetailEntities) {
            if (chatHistoryDetailEntity.isUser) {
                return chatHistoryDetailEntity.message;
            }
        }
        return "还没有开始提问哦。";
    }

    /**
     * 释放连接
     */
    @Override
    protected void onDestroy() {
        //根据现有的聊天记录存储到数据库中
        List<ChatHistoryDetailEntity> list = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            list.add(new ChatHistoryDetailEntity(chatMessage.isUser(), chatMessage.getThinkContent(), chatMessage.getMessage()));
        }
        ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity(list, getTitle(list));
        AppDatabase.getInstance(this).insert(chatHistoryEntity);
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if (mIatDialog != null && mIatDialog.isShowing()) {
            mIatDialog.dismiss(); // 关闭对话框
        }
        isRecording = false;
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }

        super.onDestroy();
    }

    /**
     * 参数设置
     */
    public void setParam() {
        String deepseekVoiceSpeed = SystemPropertiesReflection.get("persist.sys.deepseek_voice_speed", "50");
        String deepseekVoicespeaker = SystemPropertiesReflection.get("persist.sys.deepseek_voice_speaker", "x4_lingfeizhe_emo");
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
        mTts.setParameter(SpeechConstant.PITCH, "55");//设置音高
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
    private void printResult(RecognizerResult results, boolean isLast) {


        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String finalText = resultBuffer.toString().trim();
        if (finalText.isEmpty()) {
            Toast.makeText(this, "您还没开始说话", Toast.LENGTH_SHORT).show();
            return;
        }
        // 只有最后一段才做最终判断
        if (!isLast) {
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
            printResult(results, isLast);//结果数据解析
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
            Log.d(TAG, "error" + error);
            Log.d(TAG, "isInRound" + isInRound);
            if (error == null) {
                button.setImageResource(R.drawable.jzfason);
                aiType = BotConstResponse.AIType.FREE;
                chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
                chatAdapter.notifyItemChanged(chatMessages.size() - 1);
            }
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

        }

        //开始播放
        public void onSpeakBegin() {
            Log.d(TAG, "开始播放");
            button.setImageResource(R.drawable.tingzhi);
            aiType = BotConstResponse.AIType.SPEAK;
            chatMessages.get(chatMessages.size() - 1).setSpeaking(true);
            chatAdapter.notifyItemChanged(chatMessages.size() - 1);
        }

        //暂停播放
        public void onSpeakPaused() {

        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            Log.d("播放进度", "" + percent);
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
        //重置标识
        isStopRequested = false;
        isNewChatCome = false;
        speakTts.delete(0, speakTts.length());
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
//        userMessage.put("content", "请用最简洁的语言直接回答问题：\n" + userQuestion); // userQuestion 已经过转义处理
        userMessage.put("content", userQuestion); // userQuestion 已经过转义处理
        messages.put(userMessage);

        requestBody.put("messages", messages);
//        if (!mIsDeepThinkMode) {
//            requestBody.put("system", "请直接返回答案，用中文回答，回复在100字内。");
//        }
        requestBody.put("stream", true);

        JSONObject options = new JSONObject();
        options.put("temperature", 0.6);
        options.put("mirostat_tau", 1.0);
        options.put("num_predict", -1);
        options.put("repeat_penalty", 1.5);
//        options.put("mirostat_eta", 1);//影响算法响应生成文本的反馈的速度。较低的学习率将导致较慢的调整，而较高的学习率将使算法的响应速度更快。（默认值：0.1）
//        options.put("mirostat_tau", 4.0);//控制输出的连贯性和多样性之间的平衡。较低的值将导致文本更集中、更连贯。（默认值：5.0）
        requestBody.put("options", options);
        // 将 JSONObject 转换为字符串
        String jsonBodyRound1 = requestBody.toString();

        RequestBody requestBodyRound1 = RequestBody.create(jsonBodyRound1, MediaType.parse("application/json; charset=utf-8"));
        Request requestRound1 = new Request.Builder().url(API_URL).post(requestBodyRound1).build();
        Log.d(TAG, "请求: " + API_URL);
        // 异步执行请求
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)//连接超时
                .readTimeout(3, TimeUnit.SECONDS)//读取超时
                .writeTimeout(3, TimeUnit.SECONDS)//写入超时
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
                Log.d(TAG, "错误: " + e.getMessage());
                //如果不是主动关闭的话需要进行网络波动的播报
                if (!e.getMessage().equals("Socket closed")) {
                    isStopRequested = true;
                    isNewChatCome = true;
                    textFig = false;
                    button.setImageResource(R.drawable.jzfason);
                    aiType = BotConstResponse.AIType.FREE;
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage("网络波动较大，请稍后再试");
                    chatMessage.setOver(true);
                    chatMessages.add(chatMessage);
                    TTS("网络波动较大，请稍后再试");
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            BufferedSource source = responseBody.source();
                            aiType = BotConstResponse.AIType.TEXT_SHUCHU;
                            isInRound = true;
                            voiceManager = new VoiceManager();
                            voiceManager.init(MainActivity.this);
                            voiceManager.startProcessing();
                            while (!source.exhausted()) {
                                Log.e(TAG, "botMessageIndexRound1: " + botMessageIndexRound1);
                                if (isStopRequested || isNewChatCome) {
                                    isNewChatCome = false;
                                    textFig = false;
                                    if (botMessageIndexRound1 != -1) {
                                        chatMessages.get(botMessageIndexRound1).setOver(true);
                                    } else {
                                        ChatMessage chatMessage = new ChatMessage();
                                        chatMessage.setMessage("不好意思，请您重新提问");
                                        chatMessage.setOver(true);
                                        chatMessages.add(chatMessage);
                                        TTS("不好意思，请您重新提问");
                                    }
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
                                                runOnUiThread(() -> {
                                                    String huida = "";
                                                    if (chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(thinkText.toString()));
                                                        // 更新机器人消息记录的内容
                                                        String reust = huida.replace("\n", "").replace("\n\n", "");
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(reust);
                                                    } else {
                                                        huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(fullResponseRound1.toString()));
                                                        //缩进
                                                        if (huida.length() <= 0) {
                                                            huida = "对不起，这个问题我暂时不能回答哦";
                                                            // 更新机器人消息记录的内容
                                                            String result = huida.replace("\n", "").replace("\n\n", "");
                                                            chatMessages.get(botMessageIndexRound1).setMessage(result);
                                                            Log.d("huida", "huida: " + huida);
                                                            TTS(huida);
                                                        } else {
                                                            // 更新机器人消息记录的内容
                                                            String result = huida.replace("\n", "").replace("\n\n", "");
                                                            chatMessages.get(botMessageIndexRound1).setMessage(result);
                                                            Log.d("huida", "huida: " + huida);
                                                        }
                                                    }
                                                    // 如果完成，停止读取
                                                    if (done) {
                                                        Log.d(TAG, "onResponse: 回答" + huida);
                                                        isInRound = false;
                                                        isStopRequested = true;
                                                        isNewChatCome = false;
                                                        textFig = false;
                                                        chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(true);
                                                        chatMessages.get(botMessageIndexRound1).setOver(true);
//                                                        button.setImageResource(R.drawable.jzfason);
                                                        Log.d("保存上下文信息", "run: " + fullResponseRound1.toString());
                                                        // 保存上下文信息
                                                        context.add(new ChatMessage(userQuestion, true));//用户消息
                                                        context.add(new ChatMessage(fullResponseRound1.toString(), false));//机器人消息
//                                                        Log.d("上下文更新：", ); // 打印上下文信息
                                                    } else {
                                                        chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(false);
                                                    }
                                                    chatAdapter.notifyItemChanged(botMessageIndexRound1);
                                                    chatRecyclerView.scrollBy(0, chatRecyclerView.getLayoutManager().getHeight());
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
        setCurrentChatOver();
        chatMessages.remove(chatMessages.size() - 1);
        mWeatherResult = weatherLive.getCity() + "今天的天气" + weatherLive.getWeather() +
                "，当前的温度是" + weatherLive.getTemperature() + "摄氏度，" + weatherLive.getWindDirection() + "风"
                + weatherLive.getWindPower() + "级，" + "湿度" + weatherLive.getHumidity() + "%";
        ChatMessage chatMessage = new ChatMessage("", false);
        chatMessage.setOver(true);
        chatMessages.add(chatMessage); // 添加到聊天界面
        chatAdapter.notifyDataSetChanged();
        TTS(mWeatherResult);
        weatherIndex = 0;
        myHandler.post(weatherStreamRunnable);
    }

    private int weatherIndex = 0;
    Runnable weatherStreamRunnable = new Runnable() {
        @Override
        public void run() {
            if (weatherIndex > mWeatherResult.length()) {
                return;
            }
            chatMessages.set(chatMessages.size() - 1, new ChatMessage(mWeatherResult.substring(0, weatherIndex), false));
            chatAdapter.notifyDataSetChanged();
            weatherIndex++;
            myHandler.postDelayed(weatherStreamRunnable, 200);
        }
    };

    @Override
    public void onWeatherError(String message, int rCode) {
        setCurrentChatOver();
        chatMessages.remove(chatMessages.size() - 1);
        chatMessages.add(new ChatMessage(BotConstResponse.searchWeatherError, false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        TTS(BotConstResponse.searchWeatherError);
    }

    @Override
    public void onWeatherSuccess(LocalWeatherForecastResult localWeatherForecastResult) {
        setCurrentChatOver();
        Log.d(TAG, "chatMessages.remove(chatMessages.size() - 1);: " + (chatMessages.size() - 1));
        if (chatMessages.size() - 1 != 1) {
            chatMessages.remove(chatMessages.size() - 1);
        }

        StringBuilder result = new StringBuilder(BotConstResponse.getSuccessResponse());
        List<LocalDayWeatherForecast> weatherForecast = localWeatherForecastResult.getForecastResult().getWeatherForecast();
        for (LocalDayWeatherForecast localDayWeatherForecast : weatherForecast) {
            Log.d(TAG, "Date: " + localDayWeatherForecast.getDate() + ":: Weather: " + localDayWeatherForecast.getDayWeather()
                    + ":: DayWeather: " + localDayWeatherForecast.getDayWeather() + ":: NightWeather: " + localDayWeatherForecast.getNightWeather());
            int dayTemp = Integer.parseInt(localDayWeatherForecast.getDayTemp());
            int nightTemp = Integer.parseInt(localDayWeatherForecast.getNightTemp());
            result.append("\n").append("日期：").append(localDayWeatherForecast.getDate()).append("\t温度：")
                    .append(Math.min(dayTemp, nightTemp)).append("°/").append(Math.max(dayTemp, nightTemp)).append("°");
        }
        chatMessages.add(new ChatMessage(result.toString(), false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        TTS(result.toString());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.deep_think_layout) {
            mIsDeepThinkMode = !mIsDeepThinkMode;
            if (!mIsDeepThinkMode) {
                mDeepThinkLayout.setBackgroundResource(R.drawable.text_bg);
                mDeepThinkText.setTextColor(Color.parseColor("#000000"));
                mDeepThinkImg.setImageResource(R.drawable.think_unselected);
            } else {
                mDeepThinkLayout.setBackgroundResource(R.drawable.text_selected_bg);
                mDeepThinkText.setTextColor(Color.parseColor("#00BFFF"));
                mDeepThinkImg.setImageResource(R.drawable.think_select);
            }
        } else if (v.getId() == R.id.historyButton) {
            mTts.stopSpeaking();
            AppDatabase.getInstance(this).query();
            // 显示历史记录对话框
            myHandler.postDelayed(this::showHistoryDialog, 500);
        } else if (v.getId() == R.id.xjianduihua) {
            mTts.stopSpeaking();
            isStopRequested = true;
            isNewChatCome = true;
            isInRound = false;
            aiType = BotConstResponse.AIType.FREE;
            TimeDownUtil.clearTimeDown();
            if (currentCall != null) {
                currentCall.cancel();
                currentCall = null;
            }
            button.setImageResource(R.drawable.jzfason);
            List<ChatHistoryDetailEntity> list = new ArrayList<>();
            for (ChatMessage chatMessage : chatMessages) {
                list.add(new ChatHistoryDetailEntity(chatMessage.isUser(), chatMessage.getThinkContent(), chatMessage.getMessage()));
            }
            ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity(list, getTitle(list));
            AppDatabase.getInstance(this).insert(chatHistoryEntity);
            chatMessages.clear();
            chatAdapter.notifyDataSetChanged();
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
        Log.e(TAG, "123131312: " + str.trim());
        int code = mTts.startSpeaking(str.trim(), mSynListener);
        Log.e(TAG, "TTS code: " + code);
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
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
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
                chatMessages.add(new ChatMessage(botResponse, false, "", false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                TTS(botResponse);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                String address = extractLocation(baseChildModel.getText());
                Log.d(TAG, "Search: " + address);
                startTime(chatMessages.size() - 1);
                NeighborhoodSearch.search(address, 5000, new OnPoiSearchListener() {
                    @Override
                    public void onSuccess(List<LocationResult> results) {
                        setCurrentChatOver();
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
                        setCurrentChatOver();
                        Log.d("搜索失败", error);
                    }
                }, this);
                isInRound = false;
                break;
            }
            // 关键字导航
            case SceneTypeConst.KEYWORD: {
                Log.d(TAG, "Search: 关键字");
                isStopRequested = true;
                // 先让机器人回复固定内容
                chatMessages.add(new ChatMessage(botResponse, false, "", false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                TTS(botResponse);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                String address = extractLocation(baseChildModel.getText());
                startTime(chatMessages.size() - 1);
                searchIn.searchInAmap(this, address, new OnPoiSearchListener() {
                    @Override
                    public void onSuccess(List<LocationResult> results) {
                        setCurrentChatOver();
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
                        setCurrentChatOver();
                        Log.d(TAG, "onError: " + error);
                    }
                });
                isInRound = false;
                break;
            }
            case SceneTypeConst.RECENT_FILMS:
                isStopRequested = true;
                // 先让机器人回复固定内容
                chatMessages.add(new ChatMessage(botResponse, false, "", false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                TTS(botResponse);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                startTime(chatMessages.size() - 1);
                showMovieFragment();
                isInRound = false;
                break;
            //闲聊
            case SceneTypeConst.CHITCHAT: {
                // 搜索本地知识库
                for (KnowledgeEntry entry : knowledgeBase) {
                    if (entry.getTitle().equals(baseChildModel.getText())) {
                        setCurrentChatOver();
                        isStopRequested = true;
                        String content = filterSensitiveContent(entry.getContent()); // 过滤敏感词
                        updateContext(baseChildModel.getText(), content); // 更新上下文
                        chatMessages.add(new ChatMessage(entry.getContent(), false, "", false));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        //停止播放文本
                        TTS(entry.getContent());
                        found = true;
                        isInRound = false;
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
                chatMessages.add(new ChatMessage(BotConstResponse.searchWeatherWaiting, false, "", false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                TTS(BotConstResponse.searchWeatherWaiting);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                isInRound = false;
                startTime(chatMessages.size() - 1);
                break;
            case SceneTypeConst.FEATHER_WEATHER:
                weatherAPI.weatherSearch(SceneTypeConst.FEATHER_WEATHER);
                // 先让机器人回复固定内容
                chatMessages.add(new ChatMessage(BotConstResponse.searchForecastWeatherWaiting, false, "", false)); // 添加到聊天界面
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                TTS(BotConstResponse.searchForecastWeatherWaiting);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                isInRound = false;
                startTime(chatMessages.size() - 1);
                break;
            case SceneTypeConst.SELECTION:
                chatMessages.add(new ChatMessage(BotConstResponse.ok, false, "", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                TTS(BotConstResponse.ok);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                if (recyFragment != null && recyFragment.isVisible()) {
                    int position = OptionPositionParser.parsePosition(baseChildModel.getText(), recyFragment.getItemCount());
                    Log.e(TAG, "actionByType position: " + position);
                    if (position == -1) {

                    } else {
                        recyFragment.performClickItem(position);
                    }
                }
                break;
            case SceneTypeConst.VIDEO:
                DouyinApi.requestAuth(this);
                isInRound = false;
                break;
        }
    }

    //超时逻辑处理
    private void startTime(int position) {
        TimeDownUtil.startTimeDown(new TimeDownUtil.CountTimeListener() {
            @Override
            public void onTimeFinish() {
                if (!chatMessages.get(position).isOver()) {
                    chatMessages.get(position).setOver(true);
                    TTS(BotConstResponse.searchWeatherError);
                    chatMessages.get(position).setMessage(BotConstResponse.searchWeatherError);
                    chatAdapter.notifyItemChanged(position);
                }
            }
        });
    }

    public void setCurrentChatOver() {
        if (chatMessages.size() > 0) {
            chatMessages.get(chatMessages.size() - 1).setOver(true);
        }
    }

    //逻辑还存在一点问题
    public void create() {
        boolean createEnrollFig = true;
//        if (createOne == 1) {
//            createEnrollFig = createFig();
//        }
        Log.d(TAG, "注册声纹: " + createEnrollFig);
        if (createEnrollFig) {
            //匹配成功返回false失败返回true
            // 打印或使用选中的按钮文本
            if (radioButtonChe.isChecked()) {
                selectedText = radioButtonChe.getText().toString().trim();
            } else if (radioButtonPy.isChecked()) {
                selectedText = radioButtonPy.getText().toString().trim();
            } else if (radioButtonJr.isChecked()) {
                selectedText = radioButtonJr.getText().toString().trim();
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "请先选择身份！！", Toast.LENGTH_SHORT).show());
                return;
            }
            String name = selectedText;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    groupIdList.add("u1" + translateToEnglish(name) + i);
                    groupNameList.add(name + i);
                    groupInfoLsit.add(name + "hello" + i);
                    createLogotype.setGroupId(groupIdList);// 分组标识
                    createLogotype.setGroupName(groupNameList);//声纹分组名称
                    createLogotype.setGroupInfo(groupInfoLsit);//分组描述信息
                    if (!name.equals("车主")) {
                        createOne = 1;
                        featureIdList.add(translateToEnglish(name) + "number" + i);
                        featureInfoList.add(name + "Num" + i);
                        createLogotype.setFeatureId(featureIdList);//特征唯一标识
                        createLogotype.setFeatureInfo(featureInfoList); //特征描述
                        group[0] = CreateGroup.doCreateGroup(requestUrl, APP_ID, APISecret, APIKey, createLogotype);//创建声纹特征库
                        if (group[0] != null) {
                            if (group[0].get("code").equals("0") && group[0].get("message").equals("success")) {
                                // 关闭弹窗
                                customDialog.dismiss();
                                if (isRecording) {
                                    try {
                                        mediaRecorder.stop();
                                        mediaRecorder.release();
                                        isRecording = false;
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "注册声纹成功", Toast.LENGTH_SHORT).show());
                            } else {

                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "注册声纹失败，请重新注册", Toast.LENGTH_SHORT).show());
                            }
                        }
                    } else {
                        createOne = 1;
                        featureIdList.add(translateToEnglish(name) + "number" + i);
                        featureInfoList.add(name + i);
                        createLogotype.setFeatureId(featureIdList);//特征唯一标识
                        createLogotype.setFeatureInfo(featureInfoList); //特征描述
                        if (name.equals("车主") && che == 0) {
                            group[0] = CreateGroup.doCreateGroup(requestUrl, APP_ID, APISecret, APIKey, createLogotype);//创建声纹特征库
                            if (group[0] != null) {
                                if (group[0].get("code").equals("0") && group[0].get("message").equals("success")) {
                                    // 关闭弹窗
                                    customDialog.dismiss();
                                    if (isRecording) {
                                        try {
                                            mediaRecorder.stop();
                                            mediaRecorder.release();
                                            isRecording = false;
                                        } catch (IllegalStateException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "注册声纹成功", Toast.LENGTH_SHORT).show());
                                    che++;
                                    i++;
                                } else {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "注册声纹失败，请重新注册", Toast.LENGTH_SHORT).show());
                                }
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "只能注册一个车主声纹", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                    CreateFeature.doCreateFeature(requestUrl, APP_ID, APISecret, APIKey, creteFlies, createLogotype);// 添加声纹特征
                    i++;
                    creteUtlis.stopRecording();
                }
            });
            thread.start();
        } else {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "已有您的声纹信息，请不要重复注册", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * 数据解析
     *
     * @param results
     */
    private void createVoiceprint(RecognizerResult results, boolean isLast) throws JSONException {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        Log.d(TAG, "判断是不是最后一段: " + isLast);
        // 只有最后一段才做最终判断
        if (isLast) {
            create();
        }
    }

    public static String translateToEnglish(String input) {
        // 定义一个简单的映射关系
        String[] chineseWords = {"车主", "家人", "朋友"};
        String[] englishWords = {"CarOwner", "FamilyMember", "Friend"};

        // 替换中文字符串为英文
        for (int j = 0; j < chineseWords.length; j++) {
            input = input.replace(chineseWords[j], englishWords[j]);
        }
        return input;
    }

    //判断是否已注册声纹
    public boolean createFig() {
        if (createLogotype.getGroupId() != null) {
            if (createLogotype.getGroupId().size() > 1) {
                for (int j = 0; j < createLogotype.getGroupId().size(); j++) {
                    result[0] = SearchFeature.doSearchFeature(requestUrl, APP_ID, APISecret, APIKey, contrastFies, createLogotype, createLogotype.getGroupId().get(j));//1:N比对
                    if (result[0] != null) {
                        if (Double.parseDouble(Objects.requireNonNull(result[0].get("score"))) >= 0.35) {
                            fig = false;
                            break;
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "没有匹配的声纹信息，请注册1：N", Toast.LENGTH_SHORT).show());
                        fig = true;
                    }
                }
            } else if (createLogotype.getGroupId().size() == 1) {
                for (int j = 0; j < createLogotype.getGroupId().size(); j++) {
                    for (int k = 0; k < createLogotype.getFeatureId().size(); k++) {
                        SearchOneFeatureList[0] = SearchOneFeature.doSearchOneFeature(requestUrl, APP_ID, APISecret, APIKey, contrastFies, createLogotype, createLogotype.getGroupId().get(j), createLogotype.getFeatureId().get(k));//1:1
                        if (SearchOneFeatureList[0] != null && SearchOneFeatureList[0].get("score") != null) {
                            if (Double.parseDouble(Objects.requireNonNull(SearchOneFeatureList[0].get("score"))) >= 0.35) {
                                fig = false;
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "没有匹配的声纹信息，请注册1:1", Toast.LENGTH_SHORT).show());
                            fig = true;
                        }
                    }
                }
            }
        } else {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "请先注册声纹信息", Toast.LENGTH_SHORT).show());
            fig = true;
        }
        return fig;
    }
}