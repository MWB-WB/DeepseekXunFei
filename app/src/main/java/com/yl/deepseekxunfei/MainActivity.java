package com.yl.deepseekxunfei;


import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.widget.ImageButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
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

import com.yl.creteEntity.crete.CreateMethod;
import com.yl.deepseekxunfei.APICalls.GeocodingApi;
import com.yl.deepseekxunfei.adapter.ChatAdapter;

import com.yl.deepseekxunfei.broadcast.Broadcasting;
import com.yl.deepseekxunfei.fragment.MainFragment;
import com.yl.deepseekxunfei.fragment.MovieDetailFragment;
import com.yl.deepseekxunfei.fragment.RecyFragment;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ChatHistory;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.model.MovieDetailModel;
import com.yl.deepseekxunfei.page.LocationResult;
import com.yl.deepseekxunfei.page.SceneryPage;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.deepseekxunfei.room.entity.ChatHistoryDetailEntity;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;
//import com.yl.deepseekxunfei.scene.JokeClass;
import com.yl.deepseekxunfei.scene.SceneManager;
import com.yl.deepseekxunfei.scene.actoin.SceneAction;
import com.yl.deepseekxunfei.utlis.BotConstResponse;
import com.yl.deepseekxunfei.utlis.ContextHolder;
import com.yl.deepseekxunfei.utlis.JsonParser;
import com.yl.deepseekxunfei.utlis.KeyboardUtils;
import com.yl.deepseekxunfei.utlis.OptionPositionParser;
import com.yl.deepseekxunfei.utlis.PromptUtlis;
import com.yl.deepseekxunfei.utlis.SystemPropertiesReflection;
import com.yl.deepseekxunfei.utlis.TextLineBreaker;
import com.yl.deepseekxunfei.utlis.TimeDownUtil;
import com.yl.deepseekxunfei.utlis.VoiceManager;
import com.yl.deepseekxunfei.utlis.positioning;
import com.yl.deepseekxunfei.view.HistoryDialog;

import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String API_URL = "http://47.106.73.32:11434/api/chat";
    private boolean isWeatherOutputStopped = false;
    private static final long DELAY_MILLIS = 5000; // 设置延迟时间，防止用户多次点击
    PromptUtlis promptUtlis = null;
    private Deque<ChatMessage> contextQueue = new ArrayDeque<>();
    private static final int MAX_CONTEXT_TOKENS = 30000; // 预留2K tokens给新问题
    private static final int MAX_HISTORY_ROUNDS = 5; // 最多5轮对话
    public boolean isNeedWakeUp = true;
    private boolean selectGroupFig = true;
    private int seleteSize = 0;//判断是不是第一次进行唤醒
    CreateMethod createMethod = new CreateMethod();
    private EditText editTextQuestion;
    private SpeechSynthesizer mTts;
    private StringBuilder speakTts = new StringBuilder();
    private static final String TAG = "MainActivity";

    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI
    String input;

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
    public List<ChatMessage> chatMessages = new ArrayList<>();

    //是否停止输出
    public boolean isStopRequested = false;
    public boolean isNewChatCome = false;
    public boolean textFig;
    private SceneManager sceneManager;

    private LinearLayout inputLayout, mDeepThinkLayout, mDeepCreteLayout;
    private ImageView mDeepThinkImg;
    private TextView mDeepThinkText;
    private SceneAction mSceneAction;
    private TextView mDeepCreteText;
    private boolean mIsDeepThinkMode = true;
    private boolean mIsDeepCreteMode = true;
    public ImageButton TTSbutton;
    //是否隐藏停止输出按钮
    public boolean found = false;
    private List<ChatMessage> context = new ArrayList<>(); // 用于保存多轮对话的上下文
    public int i = 0;
    private String currentTitle = ""; // 当前对话的标题
    private ImageButton history;//历史对话
    private ImageButton newDialogue;//新建对话
    private TextView titleTextView;//对话标题
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
    //是否开启声纹验证
    public boolean creteOkAndNo = true;
    private boolean isRecognize = false;
    //开始录音按钮
    private Button kaishiluyin;
    // media_ecorder_recording.xml 布局中的开始录音按钮
    private Button btnStart;
    // media_ecorder_recording.xml 布局中的停止录音按钮

    private Call currentCall;
    // 获取选中的按钮文本

    private VoiceManager voiceManager = null;
    private BackTextToAction backTextToAction = null;
    Broadcasting broadcasting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
        setContentView(R.layout.activity_main);
        requestStoragePermission();//请求文件存储权限 (包括读写)
        requestLocationPermission();//位置权限
        ContextHolder.init(this); // 保存全局 Context
        initView();
        initThirdApi();
        registerBroadCast();
        myHandler = new MyHandler(this);
        sceneManager = new SceneManager(this);
        mSceneAction = new SceneAction(this);
        textFig = false;
        setParam();
        chatMessages.add(new ChatMessage("我是小天，很高兴见到你！", false, "", false));
        setLastItem(chatMessages, item -> item.setOver(true));
        TTS("我是小天，很高兴见到你！");
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        //初始化提示工具类
        promptUtlis = new PromptUtlis();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
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
        stopSpeaking();
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        isStopRequested = true;
        textFig = false;
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
//                    if (creteOkAndNo) {
//                        if (createMethod.seleteCrete()) {
//                            if (!createMethod.createFig()) {
//                                startVoiceRecognize();
//                            }
//                        } else {
//                            if (seleteSize != 0) {
//                                Toast.makeText(MainActivity.this, "暂无声纹信息，请注册", Toast.LENGTH_SHORT).show();
//                                seleteSize++;
//                            }
//                        }
//                    } else {
                    startVoiceRecognize();
//                    }
                } else if ("com.yl.voice.test.start".equals(intent.getAction())) {
                    String result = intent.getStringExtra("result");
                    commitText(result);
                } else if ("com.yl.voice.test.stop".equals(intent.getAction())) {
                    if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                        if (button != null) {
                            button.performClick();
                        }
                    }
                } else if ("com.yl.voice.commit.text".equals(intent.getAction())) {
                    if (!isRecognize) {
                        String text = intent.getStringExtra("text");
                        if (voiceManager != null) {
                            voiceManager.mTts.stopSpeaking();
                            voiceManager.release();
                        }
                        stopSpeaking();
                        if (!chatMessages.isEmpty()) {
                            setLastItem(chatMessages, item -> item.setOver(true));
                            aiType = BotConstResponse.AIType.FREE;
                        }
                        mIat.stopListening();
                        commitText(text);
                        isNeedWakeUp = false;
                    }
                }
                if (intent.getAction().equals("AUTONAVI_STANDARD_BROADCAST_SEND")) {
                    int keyType = intent.getIntExtra("KEY_TYPE", -1);
                    if (keyType == 10059) {
                        int category = intent.getIntExtra("CATEGORY", -1);
                        int responseCode = intent.getIntExtra("EXTRA_RESPONSE_CODE", -1);
                        String result = responseCode == 0
                                ? (category == 1 ? "家" : "公司") + "设置成功"
                                : "设置失败";
                        Log.d("AmapAuto", result);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.yl.voice.wakeup");
        IntentFilter filters = new IntentFilter("AUTONAVI_STANDARD_BROADCAST_SEND");
        filter.addAction("com.yl.voice.test.start");
        filter.addAction("com.yl.voice.test.stop");
        filter.addAction("com.yl.voice.commit.text");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filters, Context.RECEIVER_EXPORTED);
        }
    }

    public void stopTTSAndRequest() {
        stopSpeaking();
        if (voiceManager != null) {
            voiceManager.mTts.stopSpeaking();
            voiceManager.release();
        }
        if (currentCall != null) {
            currentCall.cancel();
        }
        if (!chatMessages.isEmpty()) {
            setLastItem(chatMessages, item -> item.setOver(true));
        }
        aiType = BotConstResponse.AIType.FREE;
    }

    private void initView() {
//        createMethod.init(MainActivity.this);
        //标题置顶
        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.bringToFront();
        mDeepThinkLayout = findViewById(R.id.deep_think_layout);
        mDeepCreteLayout = findViewById(R.id.deep_crete_layout);
        mDeepThinkLayout.setOnClickListener(this);
        mDeepCreteLayout.setOnClickListener(this);
        mDeepThinkImg = findViewById(R.id.deep_think_img);
        mDeepThinkText = findViewById(R.id.deep_think_text);
        mDeepCreteText = findViewById(R.id.deep_crete_text);
        editTextQuestion = findViewById(R.id.inputEditText);//输入框
        button = findViewById(R.id.send_button);//发送按钮
        button.setOnClickListener(this);
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
            if (voiceManager != null) {
                voiceManager.mTts.stopSpeaking();
                voiceManager.release();
            }
            Log.e(TAG, "button onclick: " + aiType);
            if (aiType == BotConstResponse.AIType.TEXT_NO_READY) {
                Log.d(TAG, "initView: 请输入一个问题");
                promptUtlis.promptInput(MainActivity.this);
            } else if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
                promptUtlis.promptInput(MainActivity.this);
                try {
                    if (chatMessages.isEmpty()) {
                        replaceFragment(0);
                        sendMessage();
                    } else {
                        Log.d(TAG, "initView: " + aiType);
                        if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
                            replaceFragment(0);
                            sendMessage();
                        } else {
                            promptUtlis.promptReply(MainActivity.this);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                // 停止天气输出
                isWeatherOutputStopped = true;
                myHandler.removeCallbacks(weatherStreamRunnable);
                mTts.stopSpeaking();
                if (currentCall != null) {
                    currentCall.cancel();
                    currentCall = null;
                }
                isStopRequested = true;
                textFig = false;
                setCurrentChatOver();
                if (voiceManager != null) {
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
            requestRecordAudioPermission();
            // 检查录音权限
            boolean hasRecordPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (hasRecordPermission){
                setLastItem(chatMessages, item -> item.setOver(true));
                mTts.stopSpeaking();
                if (voiceManager != null) {
                    voiceManager.mTts.stopSpeaking();
                    voiceManager.release();
                }
                if (currentCall != null) {
                    currentCall.cancel();
                }
                startVoiceRecognize();
            }
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
        broadcasting = new Broadcasting();
        movieDetailFragment = new MovieDetailFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, mainFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, recyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, movieDetailFragment).commit();
        replaceFragment(0);
        kaishiluyin.setOnClickListener(v -> {
            createMethod.kaishi();
        });
    }

    private void startVoiceRecognize() {
        setParam();
        isDuplicate = true;
        isStopRequested = false;
        if (aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
            isNewChatCome = true;
        }
        editTextQuestion.setText("");
        //停止播放文本
        stopSpeaking();
        if (null == mIat) {
            return;
        }
        mIatResults.clear();
        //带UI界面
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
        isRecognize = true;
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
            List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(text);
            mSceneAction.actionByType(baseChildModelList.get(0));
        } else {
            if (!chatMessages.get(chatMessages.size() - 1).isOver() || aiType == BotConstResponse.AIType.SPEAK) {
                promptUtlis.promptReply(MainActivity.this);
            } else {
                mTts.stopSpeaking();
                if (voiceManager != null) {
                    voiceManager.release();
                }
                chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
                chatAdapter.notifyItemChanged(chatMessages.size() - 1);
                chatMessages.add(new ChatMessage(text, true, "", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(text);
                mSceneAction.actionByType(baseChildModelList.get(0));
            }
        }
    }

    private void initThirdApi() {
        //初始化动画效果
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
        //初始话声纹识别必须参数

        // 加载本地知识库
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
    }


    private void showHistoryDialog() {
        HistoryDialog dialog = new HistoryDialog(this, AppDatabase.getInstance(this).getChatHistoryEntities(), MainActivity.this);
        dialog.setOnDialogDataBack(new HistoryDialog.onDialogDataBack() {
            @Override
            public void dataBack(ChatHistoryEntity chatHistoryEntity) {
                chatMessages.clear();
                context.clear();
                List<ChatHistoryDetailEntity> chatHistoryDetailEntities = chatHistoryEntity.getChatHistoryDetailEntities();
                for (ChatHistoryDetailEntity chatHistoryDetailEntity : chatHistoryDetailEntities) {
                    chatMessages.add(new ChatMessage(chatHistoryDetailEntity.message, chatHistoryDetailEntity.isUser, chatHistoryDetailEntity.thinkMessage, false));
                    context.add(new ChatMessage(chatHistoryDetailEntity.message, chatHistoryDetailEntity.isUser));
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        });
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
        input = inputEditText.getText().toString().trim();
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
            if (backTextToAction != null) {
                backTextToAction.backUserText(input);
                backTextToAction = null;
            } else {
                List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(input);
                if (baseChildModelList.size() > 1) {
                    mSceneAction.startActionByList(baseChildModelList);
                } else {
                    mSceneAction.actionByType(baseChildModelList.get(0));
                }
            }
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

    public void replaceFragment(int id) {
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
        createMethod.cloneMIatDisalogs();
        createMethod.tenSecondsOfAudio.stopRecording();
        super.onDestroy();
    }

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
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        if (backTextToAction != null) {
            backTextToAction.backUserText(finalText);
            backTextToAction = null;
        } else {
            if (!isPause) {
                List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(finalText);
                if (baseChildModelList.size() > 1) {
                    mSceneAction.startActionByList(baseChildModelList);
                } else {
                    mSceneAction.actionByType(baseChildModelList.get(0));
                }
            }
        }
    }

    private boolean isPause = false;

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    public void setBackTextToAction(BackTextToAction backTextToAction) {
        this.backTextToAction = backTextToAction;
    }

    //用来返回用户说的话或者输入的文本
    public interface BackTextToAction {
        void backUserText(String text);
    }

    /**
     * 听写UI监听器
     */
    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results, isLast);//结果数据解析
            isRecognize = false;
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
            if (error == null) {
                button.setImageResource(R.drawable.jzfason);
                aiType = BotConstResponse.AIType.FREE;
                chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
                chatAdapter.notifyItemChanged(chatMessages.size() - 1);
                if (isNeedWakeUp) {
//                    if (BotConstResponse.searchWeatherWaiting)
                    // 检查录音权限
                    boolean hasRecordPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                    if (hasRecordPermission){
                        TTSbutton.performClick();
                    }
                }
                mSceneAction.startActionByPosition();
                isNeedWakeUp = true;
            }
            Log.d(TAG, "onCompleted: aiType " + aiType);
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

        }

        //开始播放
        public void onSpeakBegin() {
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

    public void callGenerateApi(String userQuestion) throws JSONException {
        textFig = true;
        button.setImageResource(R.drawable.tingzhi);
        //重置标识
        isStopRequested = false;
        isNewChatCome = false;
        speakTts.delete(0, speakTts.length());
        // 使用 JSONObject 构建 JSON 请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text");
        JSONArray messages = new JSONArray();
        int currentTokens = 0;
        // 1. 添加历史上下文（从旧到新）
        for (ChatMessage msg : contextQueue) {
            int msgTokens = estimateTokens(msg.getMessage());
            if (currentTokens + msgTokens > MAX_CONTEXT_TOKENS) break;
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("role", msg.isUser() ? "user" : "assistant");
            jsonMsg.put("content", msg.getMessage());
            messages.put(jsonMsg);
            currentTokens += msgTokens;
        }
        // 添加当前用户问题
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
//        userMessage.put("content", "请用最简洁的语言直接回答问题：\n" + userQuestion); // userQuestion 已经过转义处理
        userMessage.put("content", userQuestion); // userQuestion 已经过转义处理ffc
        messages.put(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        JSONObject options = new JSONObject();
        options.put("temperature", 0.9);
        options.put("mirostat_tau", 1.0);
        options.put("num_predict", -1);
        options.put("repeat_penalty", 1.0);//重复惩罚
        options.put("mirostat_eta", 1);//影响算法响应生成文本的反馈的速度。较低的学习率将导致较慢的调整，而较高的学习率将使算法的响应速度更快。（默认值：0.1）
        options.put("mirostat_tau", 1);//控制输出的连贯性和多样性之间的平衡。较低的值将导致文本更集中、更连贯。（默认值：5.0）
        requestBody.put("options", options);
        // 将 JSONObject 转换为字符串
        String jsonBodyRound1 = requestBody.toString();

        RequestBody requestBodyRound1 = RequestBody.create(jsonBodyRound1, MediaType.parse("application/json; charset=utf-8"));
        Request requestRound1 = new Request.Builder().url(API_URL).post(requestBodyRound1).build();
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
                    textFig = false;
                    button.setImageResource(R.drawable.jzfason);
                    aiType = BotConstResponse.AIType.FREE;
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage("网络波动较大，请稍后再试");
                    chatMessage.setOver(true);
                    chatMessages.add(chatMessage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
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
                            aiType = BotConstResponse.AIType.TEXT_SHUCHU;
                            voiceManager = new VoiceManager();
                            voiceManager.init(MainActivity.this);
                            voiceManager.startProcessing();
                            while (!source.exhausted()) {
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
//                                                    TTSFish(partialResponse);
                                                    voiceManager.appendText(partialResponse);
                                                }
                                                // 更新 UI
                                                runOnUiThread(() -> {
                                                    String huida = "";
                                                    if (chatMessages.get(botMessageIndexRound1).isThinkContent()) {
                                                        huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(thinkText.toString())).trim();
                                                        // 更新机器人消息记录的内容
//                                                        String reust = huida.replace("\n", "").replace("\n\n", "")
                                                        chatMessages.get(botMessageIndexRound1).setThinkContent(huida);
                                                        Log.d(TAG, "onResponse: " + huida);
                                                    } else {
                                                        huida = filterSensitiveContent(TextLineBreaker.breakTextByPunctuation(fullResponseRound1.toString())).trim();
                                                        //缩进
                                                        if (huida.contains(startTag)) {
                                                            huida = huida.replace(startTag, "");
                                                        }
                                                        if (huida.length() <= 0) {
                                                            huida = "对不起，这个问题我暂时不能回答";
                                                            // 更新机器人消息记录的内容
//                                                            String result = huida.replace("\n", "").replace("\n\n", "").trim();
                                                            Log.d(TAG, "onResponse: " + huida);
                                                            chatMessages.get(botMessageIndexRound1).setMessage(huida);
                                                            TTS(huida);
                                                        } else {
                                                            // 更新机器人消息记录的内容
                                                            String result = huida.replace("\n", "").replace("\n\n", "").trim();
                                                            chatMessages.get(botMessageIndexRound1).setMessage(huida);
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
                                                        textFig = false;
                                                        chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(true);
                                                        chatMessages.get(botMessageIndexRound1).setOver(true);
//                                                        button.setImageResource(R.drawable.jzfason);
                                                        // 保存上下文信息
                                                        context.add(new ChatMessage(userQuestion, true));//用户消息
                                                        context.add(new ChatMessage(fullResponseRound1.toString(), false));//机器人消息
                                                    } else {
                                                        chatMessages.get(botMessageIndexRound1).setNeedShowFoldText(false);
                                                    }
                                                    chatAdapter.notifyItemChanged(botMessageIndexRound1);
                                                    chatRecyclerView.scrollBy(0, chatRecyclerView.getLayoutManager().getHeight());
                                                });
                                            }
                                        }
                                    } else {
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
                        Toast.makeText(MainActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    public void onTodayWeather(LocalWeatherLive weatherLive) {
        setCurrentChatOver();
        TimeDownUtil.clearTimeDown();
        isWeatherOutputStopped = false;
        mWeatherResult = weatherLive.getCity() + "今天的天气" + weatherLive.getWeather() +
                "，当前的温度是" + weatherLive.getTemperature() + "摄氏度，" + weatherLive.getWindDirection() + "风"
                + weatherLive.getWindPower() + "级，" + "湿度" + weatherLive.getHumidity() + "%";
        ChatMessage chatMessage = new ChatMessage("", false);
        chatMessage.setOver(true);
        chatAdapter.notifyDataSetChanged();
        // 只有在未被停止时才执行TTS和后续输出
        if (!isWeatherOutputStopped) {
            TTS(mWeatherResult);
            weatherIndex = 0;
            myHandler.post(weatherStreamRunnable);
        }
    }

    private int weatherIndex = 0;
    Runnable weatherStreamRunnable = new Runnable() {
        @Override
        public void run() {
            if (isWeatherOutputStopped || weatherIndex > mWeatherResult.length()) {
                chatMessages.get(chatMessages.size() - 1).setOver(true);
                return;
            }
            chatMessages.set(chatMessages.size() - 1, new ChatMessage(mWeatherResult.substring(0, weatherIndex), false));
            chatAdapter.notifyDataSetChanged();
            weatherIndex++;
            myHandler.postDelayed(weatherStreamRunnable, 200);
        }
    };

    public void onWeatherError(String message, int rCode) {
        setCurrentChatOver();
        TimeDownUtil.clearTimeDown();
        chatMessages.remove(chatMessages.size() - 1);
        chatMessages.add(new ChatMessage(BotConstResponse.searchWeatherError, false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        TTS(BotConstResponse.searchWeatherError);
    }


    public void onForecastWeatherSuccess(LocalWeatherForecastResult localWeatherForecastResult) {
        setCurrentChatOver();
        TimeDownUtil.clearTimeDown();
        Log.d(TAG, "chatMessages.remove(chatMessages.size() - 1);: " + (chatMessages.size() - 1));
        if (chatMessages.size() - 1 != 1) {
            chatMessages.remove(chatMessages.size() - 1);
        }

        StringBuilder result = new StringBuilder(BotConstResponse.searchForecastWeatherSuccess);
        List<LocalDayWeatherForecast> weatherForecast = localWeatherForecastResult.getForecastResult().getWeatherForecast();
        for (LocalDayWeatherForecast localDayWeatherForecast : weatherForecast) {
            int dayTemp = Integer.parseInt(localDayWeatherForecast.getDayTemp());
            int nightTemp = Integer.parseInt(localDayWeatherForecast.getNightTemp());
            result.append("\n").append("日期：").append(localDayWeatherForecast.getDate()).append("\t温度：")
                    .append(Math.min(dayTemp, nightTemp)).append("°/").append(Math.max(dayTemp, nightTemp)).append("°");
        }
        chatMessages.add(new ChatMessage(result.toString(), false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
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
            stopSpeaking();
            isNeedWakeUp = true;
            AppDatabase.getInstance(this).query();
            // 显示历史记录对话框
            myHandler.postDelayed(this::showHistoryDialog, 500);
        } else if (v.getId() == R.id.xjianduihua) {
            contextQueue.clear();
            context.clear();
            if (voiceManager != null) {
                voiceManager.mTts.stopSpeaking();
            }
            stopSpeaking();
            setCurrentChatOver();
            isStopRequested = true;
            isNewChatCome = true;
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
        } else if (v.getId() == R.id.send_button) {
            isNeedWakeUp = true;
            if (aiType == BotConstResponse.AIType.TEXT_NO_READY) {
                promptUtlis.promptInput(MainActivity.this);
            } else if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
                Log.d(TAG, "请输入一个问题: ");
                try {
                    if (chatMessages.isEmpty()) {
                        replaceFragment(0);
                        sendMessage();
                    } else {
                        if (!chatMessages.get(chatMessages.size() - 1).isOver()) {
                            promptUtlis.promptReply(MainActivity.this);
                        } else {
                            replaceFragment(0);
                            sendMessage();
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                stopSpeaking();
                if (currentCall != null) {
                    currentCall.cancel();
                    currentCall = null;
                }
                isStopRequested = true;
                textFig = false;
                setCurrentChatOver();
                if (voiceManager != null) {
                    voiceManager.mTts.stopSpeaking();
                    voiceManager.release();
                }
                aiType = BotConstResponse.AIType.FREE;
                button.setImageResource(R.drawable.jzfason);
                chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
                chatAdapter.notifyItemChanged(chatMessages.size() - 1);
            }
        } else if (v.getId() == R.id.deep_crete_layout) {
            mIsDeepCreteMode = !mIsDeepCreteMode;
            if (!mIsDeepCreteMode) {
                mDeepCreteLayout.setBackgroundResource(R.drawable.text_bg);
                mDeepCreteText.setTextColor(Color.parseColor("#000000"));
                Log.d(TAG, "onClick: 关闭声纹验证");
                creteOkAndNo = false;
            } else {
                mDeepCreteLayout.setBackgroundResource(R.drawable.text_selected_bg);
                mDeepCreteText.setTextColor(Color.parseColor("#00BFFF"));
                Log.d(TAG, "onClick: 开启声纹验证");
                creteOkAndNo = true;
            }
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
        mTts.stopSpeaking();
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
    private static final int REQUEST_CODE_RECORD_AUDIO = 100;
    private static final int REQUEST_CODE_STORAGE = 101;
    private static final int REQUEST_CODE_LOCATION = 102;

    /**
     * 请求录音权限
     */
    private void requestRecordAudioPermission() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_RECORD_AUDIO);
        }
    }

    /**
     * 请求文件存储权限 (包括读写)
     */
    private void requestStoragePermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_CODE_STORAGE);
        }
    }

    /**
     * 请求定位权限 (包括精确和粗略定位)
     */
    public void requestLocationPermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_CODE_LOCATION);
        }
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 录音权限已授予，执行相关操作
                    Toast.makeText(this, "录音权限已授予", Toast.LENGTH_SHORT).show();
                } else {
                    // 录音权限被拒绝，提示用户或执行其他操作
                    Toast.makeText(this, "录音权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_STORAGE:
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    // 存储权限已授予
                    Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
                } else {
                    // 存储权限被拒绝
                    Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_LOCATION:
                boolean locationGranted = false;
                for (int i = 0; i < permissions.length; i++) {
                    if ((permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) &&
                            grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        locationGranted = true;
                        break;
                    }
                }
                if (locationGranted) {
                    // 定位权限已授予
                    Toast.makeText(this, "定位权限已授予", Toast.LENGTH_SHORT).show();
                } else {
                    // 定位权限被拒绝
                    Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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

    public void updateContext(String userQuestion, String modelResponse) {
        context.add(new ChatMessage(userQuestion, true)); // 用户问题
        context.add(new ChatMessage(modelResponse, false)); // 模型回答
        // 限制上下文长度（避免过长）
        if (context.size() > 4) { // 保留最近的 10 轮对话
            context.remove(0);
            context.remove(0); // 同时移除一对问答
        }
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

    // 显示搜索结果
    public void showSearchResults(List<LocationResult> results) {
        replaceFragment(1);
        recyFragment.showNavSearchResult(results);
    }

    // 显示搜索结果
    public void showWaypointsResults(List<LocationResult> results, RecyFragment.OnWayPointClick onWayPointClick) {
        replaceFragment(1);
        recyFragment.showWaypointsResult(results, onWayPointClick);
    }

    // 显示搜索结果
    public void showStartWaypointsResults(List<LocationResult> results, LocationResult wayPoint) {
        replaceFragment(1);
        recyFragment.showStartWaypointsResult(results, wayPoint);
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

    public void addMessageAndTTS(ChatMessage chatMessage, String text) {
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemChanged(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        TTS(text);
    }

    public void selectionAction(String text) {
        if (recyFragment != null && recyFragment.isVisible()) {
            int position = OptionPositionParser.parsePosition(text, recyFragment.getItemCount());
            if (position == -1) {

            } else {
                recyFragment.performClickItem(position);
                replaceFragment(0);
            }
        }
    }

    public void startTimeOut() {
        startTime(chatMessages.size() - 1);
    }

    //超时逻辑处理
    private void startTime(int position) {
        Log.d(TAG, "startTime12456888: 超时");
        Log.d(TAG, "超时: " + position);
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

    // 简易估算token长度（实际应调用HuggingFace tokenizer）
    private int estimateTokens(String text) {
        return text.length() / 4; // 中文≈1token/2字，英文≈1token/4字符
    }

    public void setCurrentChatOver() {
        if (chatMessages.size() > 0) {
            setLastItem(chatMessages, item -> item.setOver(true));
        }
    }


    private void stopSpeaking() {
        if (mTts != null) {
            mTts.stopSpeaking();
        }
        if (!chatMessages.isEmpty()) {
            chatMessages.get(chatMessages.size() - 1).setSpeaking(false);
            chatAdapter.notifyItemChanged(chatMessages.size() - 1);
        }
    }

    /**
     * 播放音频文件
     */
    private void playAudioFile(File audioFile) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void setLastItem(List<T> list, Consumer<T> action) {
        if (list != null && !list.isEmpty()) {
            action.accept(list.get(list.size() - 1));
        }
    }
}