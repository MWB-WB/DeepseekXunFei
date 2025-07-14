package com.yl.deepseekxunfei.activity;


import static com.yl.deepseekxunfei.presenter.MainPresenter.REQUEST_CODE_LOCATION;
import static com.yl.deepseekxunfei.presenter.MainPresenter.REQUEST_CODE_MAC_ADDRESS;
import static com.yl.deepseekxunfei.presenter.MainPresenter.REQUEST_CODE_RECORD_AUDIO;
import static com.yl.deepseekxunfei.presenter.MainPresenter.REQUEST_CODE_STORAGE;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.yl.basemvp.BaseActivity;
import com.yl.basemvp.SystemPropertiesReflection;
import com.yl.creteEntity.crete.CreateMethod;
import com.yl.deepseekxunfei.R;
import com.yl.deepseekxunfei.adapter.ChatAdapter;
import com.yl.deepseekxunfei.fragment.MainFragment;
import com.yl.deepseekxunfei.fragment.MovieDetailFragment;
import com.yl.deepseekxunfei.fragment.RecyFragment;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.presenter.MainPresenter;
import com.yl.deepseekxunfei.room.AppDatabase;
import com.yl.deepseekxunfei.room.AppDatabaseAbcodeRoom;
import com.yl.deepseekxunfei.room.entity.ChatHistoryDetailEntity;
import com.yl.deepseekxunfei.room.entity.ChatHistoryEntity;
import com.yl.deepseekxunfei.room.ulti.JSONReader;
import com.yl.deepseekxunfei.scene.SceneManager;
import com.yl.deepseekxunfei.scene.actoin.SceneAction;
import com.yl.ylcommon.utlis.BotConstResponse;
import com.yl.ylcommon.utlis.JsonParser;
import com.yl.ylcommon.utlis.KeyboardUtils;
import com.yl.ylcommon.utlis.OptionPositionParser;
import com.yl.ylcommon.utlis.TimeDownUtil;
import com.yl.deepseekxunfei.view.HistoryDialog;
import com.yl.gaodeApi.page.LocationResult;
import com.yl.gaodeApi.weather.YLLocalWeatherForecastResult;
import com.yl.gaodeApi.weather.YLLocalWeatherLive;
import com.yl.tianmao.MovieDetailModel;
import com.yl.ylcommon.utlis.ToastUtil;

import java.io.IOException;
import java.util.function.Consumer;

import okhttp3.internal.http2.Header;


public class MainActivity extends BaseActivity<MainPresenter> {

    private boolean isWeatherOutputStopped = false;
    public boolean isNeedWakeUp = true;
    private int seleteSize = 0;//判断是不是第一次进行唤醒
    CreateMethod createMethod = new CreateMethod();
    private EditText editTextQuestion;
    private static final String TAG = "MainActivity";
    String input;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private EditText inputEditText;
    public ImageButton button;
    private RecyclerView chatRecyclerView;
    public ChatAdapter chatAdapter;
    public boolean textFig;
    private SceneManager sceneManager;

    private LinearLayout mDeepThinkLayout, mDeepCreteLayout;
    private ImageView mDeepThinkImg;
    private TextView mDeepThinkText;
    private SceneAction mSceneAction;
    private TextView mDeepCreteText;
    private boolean mIsDeepThinkMode = true;
    private boolean mIsDeepCreteMode = true;
    public ImageButton TTSbutton;
    //是否隐藏停止输出按钮
    public boolean found = false;
    public int i = 0;
    private String currentTitle = ""; // 当前对话的标题
    private ImageButton history;//历史对话
    private ImageButton newDialogue;//新建对话
    private TextView titleTextView;//对话标题
    public MainFragment mainFragment;
    private RecyFragment recyFragment;
    private MovieDetailFragment movieDetailFragment;
    private MyHandler myHandler;
    private String mWeatherResult;//天气消息
    public BotConstResponse.AIType aiType = BotConstResponse.AIType.TEXT_NO_READY;
    // 检查当前标题是否已经存在于历史记录中
    boolean isDuplicate = false;
    //是否开启声纹验证
    public boolean creteOkAndNo = true;
    private boolean isRecognize = false;
    //开始录音按钮
    private Button kaishiluyin;
    private BackTextToAction backTextToAction = null;
    private AppDatabaseAbcodeRoom db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.getChatMessages().add(new ChatMessage("我是小天，很高兴见到你！", false, "", false));
        setLastItem(mPresenter.getChatMessages(), item -> item.setOver(true));
        mPresenter.TTS("我是小天，很高兴见到你！");
        chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
        JSONReader.insertJsonFileData(this, "result.json");//高德城市编码表中的数据添加到数据库
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attach(this);
    }

    @Override
    protected void initData() {
        mPresenter.initThirdApi();
        registerBroadCast();
        myHandler = new MyHandler(this);
        sceneManager = new SceneManager(this);
        mSceneAction = new SceneAction(this);
        textFig = false;
        mPresenter.setParam();
//        mPresenter.MACAddressMain();
        //        requestLocationPermission();//位置权限
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
//        createMethod.init(MainActivity.this);
        //标题置顶
        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.bringToFront();
        mDeepThinkLayout = findViewById(R.id.deep_think_layout);
        mDeepCreteLayout = findViewById(R.id.deep_crete_layout);
        mDeepThinkLayout.setOnClickListener(mPresenter);
        mDeepCreteLayout.setOnClickListener(mPresenter);
        mDeepThinkImg = findViewById(R.id.deep_think_img);
        mDeepThinkText = findViewById(R.id.deep_think_text);
        mDeepCreteText = findViewById(R.id.deep_crete_text);
        editTextQuestion = findViewById(R.id.inputEditText);//输入框
        button = findViewById(R.id.send_button);//发送按钮
        button.setOnClickListener(mPresenter);
        TTSbutton = findViewById(R.id.wdxzs);
        kaishiluyin = findViewById(R.id.kaishiluyin);
        button.setImageResource(R.drawable.jzfason);
        button.setOnClickListener(v -> {
            mPresenter.voiceManagerStop();
            Log.e(TAG, "button onclick: " + aiType);
            if (aiType == BotConstResponse.AIType.TEXT_NO_READY) {
                ToastUtil.show(this, "请输入一个问题");
            } else if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
                if (TextUtils.isEmpty(editTextQuestion.getText())) {
                    ToastUtil.show(this, "请输入一个问题");
                    return;
                }
                try {
                    if (mPresenter.getChatMessagesSize() > 0) {
                        replaceFragment(0);
                        sendMessage();
                    } else {
                        Log.d(TAG, "initView: " + aiType);
                        if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
                            replaceFragment(0);
                            sendMessage();
                        } else {
                            ToastUtil.show(this, "");
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
                // 停止天气输出
                isWeatherOutputStopped = true;
                myHandler.removeCallbacks(weatherStreamRunnable);
                mPresenter.stopSpeaking();
                mPresenter.voiceManagerStop();
                mPresenter.stopCurrentCall();
                mPresenter.setStopRequested(true);
                textFig = false;
                setCurrentChatOver();
                aiType = BotConstResponse.AIType.FREE;
                button.setImageResource(R.drawable.jzfason);
                mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(false);
                chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
            }
        });
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
            mPresenter.requestRecordAudioPermission();
            // 检查录音权限
            boolean hasRecordPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (hasRecordPermission) {
                setLastItem(mPresenter.getChatMessages(), item -> item.setOver(true));
                mPresenter.stopSpeaking();
                mPresenter.voiceManagerStop();
                mPresenter.stopCurrentCall();
                startVoiceRecognize();
            }
        });
        //输入框
        inputEditText = findViewById(R.id.inputEditText);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatAdapter = new ChatAdapter(mPresenter.getChatMessages());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        // 在初始化 RecyclerView 时禁用动画
        chatRecyclerView.setItemAnimator(null);
        // 隐藏主面板并添加历史记录
        inputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                try {
                    sendMessage();
                } catch (Exception e) {
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
        history.setOnClickListener(mPresenter);
        newDialogue.setOnClickListener(mPresenter);
        mainFragment = new MainFragment();
        recyFragment = new RecyFragment();
        movieDetailFragment = new MovieDetailFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, mainFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, recyFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_layout, movieDetailFragment).commit();
        replaceFragment(0);
        kaishiluyin.setOnClickListener(v -> {
            createMethod.kaishi();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
        if (mPresenter.getChatMessagesSize() > 0) {
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
        mPresenter.stopCurrentCall();
        mPresenter.setStopRequested(true);
        textFig = false;
        setCurrentChatOver();
        aiType = BotConstResponse.AIType.FREE;
        button.setImageResource(R.drawable.jzfason);
        if (mPresenter.getChatMessagesSize() > 0) {
            mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(false);
            chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
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
                        mPresenter.voiceManagerStop();
                        stopSpeaking();
                        if (mPresenter.getChatMessagesSize() > 0) {
                            setLastItem(mPresenter.getChatMessages(), item -> item.setOver(true));
                            aiType = BotConstResponse.AIType.FREE;
                        }
                        mPresenter.stopListening();
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
        mPresenter.voiceManagerStop();
        mPresenter.stopCurrentCall();
        if (mPresenter.getChatMessagesSize() > 0) {
            setLastItem(mPresenter.getChatMessages(), item -> item.setOver(true));
        }
        aiType = BotConstResponse.AIType.FREE;
    }

    private void startVoiceRecognize() {
        mPresenter.setParam();
        isDuplicate = true;
        mPresenter.setStopRequested(false);
        if (aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
            mPresenter.setNewChatCome(true);
        }
        editTextQuestion.setText("");
        //停止播放文本
        stopSpeaking();
        mIatResults.clear();
        isRecognize = true;
        mPresenter.startVoiceRecognize();
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
        if (mPresenter.getChatMessagesSize() <= 0) {
            mPresenter.getChatMessages().add(new ChatMessage(text, true, "", false));
            chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
            if (!isNetWorkConnect()) {
                addMessageAndTTS(new ChatMessage(BotConstResponse.searchWeatherError, false, "", false), BotConstResponse.searchWeatherError);
            } else {
                List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(text);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSceneAction.actionByType(baseChildModelList.get(0));
                    }
                });
            }
        } else {
            if (!mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).isOver() || aiType == BotConstResponse.AIType.SPEAK) {
                ToastUtil.show(this, "请先等待上一个问题回复完成在进行提问");
            } else {
                mPresenter.stopSpeaking();
                mPresenter.voiceManagerStop();
                mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(false);
                chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
                mPresenter.getChatMessages().add(new ChatMessage(text, true, "", false));
                chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
                chatRecyclerView.scrollToPosition(mPresenter.getChatMessagesSizeIndex());
                if (!isNetWorkConnect()) {
                    addMessageAndTTS(new ChatMessage(BotConstResponse.searchWeatherError, false, "", false), BotConstResponse.searchWeatherError);
                } else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(text);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSceneAction.actionByType(baseChildModelList.get(0));
                                }
                            });
                        }
                    }).start();
                }
            }
        }
    }


    public void showHistoryDialog() {
        HistoryDialog dialog = new HistoryDialog(this, AppDatabase.getInstance(this).getChatHistoryEntities(), MainActivity.this);
        dialog.setOnDialogDataBack(new HistoryDialog.onDialogDataBack() {
            @Override
            public void dataBack(ChatHistoryEntity chatHistoryEntity) {
                mPresenter.getChatMessages().clear();
                mPresenter.contextQueue.clear();
                List<ChatHistoryDetailEntity> chatHistoryDetailEntities = chatHistoryEntity.getChatHistoryDetailEntities();
                for (ChatHistoryDetailEntity chatHistoryDetailEntity : chatHistoryDetailEntities) {
                    mPresenter.getChatMessages().add(new ChatMessage(chatHistoryDetailEntity.message, chatHistoryDetailEntity.isUser, chatHistoryDetailEntity.thinkMessage, false));
                    mPresenter.contextQueue.add(new ChatMessage(chatHistoryDetailEntity.message, chatHistoryDetailEntity.isUser));
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(mPresenter.getChatMessagesSizeIndex());
            }
        });
        dialog.show();
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
    private void sendMessage() {
        found = false;
        input = inputEditText.getText().toString().trim();
        if (!input.isEmpty()) {
            // 如果是第一次提问，将问题设置为对话标题
            if (currentTitle.isEmpty()) {
                currentTitle = input;
                titleTextView.setText(currentTitle); // 更新标题
            }
            // 添加问题到对话列表
            mPresenter.getChatMessages().add(new ChatMessage(input, true, "", false));
            chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
            chatRecyclerView.scrollToPosition(mPresenter.getChatMessagesSizeIndex());
            inputEditText.setText("");
            if (backTextToAction != null) {
                backTextToAction.backUserText(input);
                backTextToAction = null;
            } else {
                if (!isNetWorkConnect()) {
                    addMessageAndTTS(new ChatMessage(BotConstResponse.searchWeatherError, false, "", false), BotConstResponse.searchWeatherError);
                } else {
                    List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(input);
                    if (baseChildModelList.size() > 1) {
                        mSceneAction.startActionByList(baseChildModelList);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSceneAction.actionByType(baseChildModelList.get(0));
                            }
                        });
                    }
                }
            }
        }
    }

    private boolean isNetWorkConnect() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected();
    }

    public void replaceFragment(int id) {

        hideFragment();
        if (id == 0) {
            if (mainFragment == null) {
                //设置fragment
                Log.d(TAG, "replaceFragment:1 右侧");
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
        for (ChatMessage chatMessage : mPresenter.getChatMessages()) {
            list.add(new ChatHistoryDetailEntity(chatMessage.isUser(), chatMessage.getThinkContent(), chatMessage.getMessage()));
        }
        ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity(list, getTitle(list));
        AppDatabase.getInstance(this).insert(chatHistoryEntity);
        createMethod.cloneMIatDisalogs();
        if (createMethod.tenSecondsOfAudio != null) {
            createMethod.tenSecondsOfAudio.stopRecording();
        }
        mPresenter.detach();
        super.onDestroy();
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
        mPresenter.getChatMessages().add(new ChatMessage(finalText, true)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
        chatRecyclerView.scrollToPosition(mPresenter.getChatMessagesSizeIndex());
        if (backTextToAction != null) {
            backTextToAction.backUserText(finalText);
            backTextToAction = null;
        } else {
            if (!isPause) {
                List<BaseChildModel> baseChildModelList = sceneManager.parseToScene(finalText);
                if (baseChildModelList.size() > 1) {
                    mSceneAction.startActionByList(baseChildModelList);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSceneAction.actionByType(baseChildModelList.get(0));
                        }
                    });
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

    public void newChat() {
        mPresenter.clearContextQueue();
        mPresenter.voiceManagerStop();
        mPresenter.stopCurrentCall();
        // 新增：重置当前对话状态
        mPresenter.isStopRequested = true;
        mPresenter.isNewChatCome = true;
        stopSpeaking();
        setCurrentChatOver();
        mPresenter.setNewChatCome(true);
        mPresenter.setStopRequested(true);
        aiType = BotConstResponse.AIType.FREE;
        TimeDownUtil.clearTimeDown();
        button.setImageResource(R.drawable.jzfason);
        List<ChatHistoryDetailEntity> list = new ArrayList<>();
        for (ChatMessage chatMessage : mPresenter.getChatMessages()) {
            list.add(new ChatHistoryDetailEntity(chatMessage.isUser(), chatMessage.getThinkContent(), chatMessage.getMessage()));
        }
        ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity(list, getTitle(list));
        AppDatabase.getInstance(this).insert(chatHistoryEntity);
        mPresenter.getChatMessages().clear();
        chatAdapter.notifyDataSetChanged();
    }

    public void sendBtnClick() {
        isNeedWakeUp = true;
        if (aiType == BotConstResponse.AIType.TEXT_NO_READY) {
            ToastUtil.show(this, "请输入一个问题");
        } else if (aiType == BotConstResponse.AIType.TEXT_READY || aiType == BotConstResponse.AIType.FREE) {
            Log.d(TAG, "请输入一个问题: ");
            try {
                if (mPresenter.getChatMessagesSize() > 0) {
                    replaceFragment(0);
                    sendMessage();
                } else {
                    if (!mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).isOver()) {
                        ToastUtil.show(this, "请先等待上一个问题回复完成在进行提问");
                    } else {
                        replaceFragment(0);
                        sendMessage();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (aiType == BotConstResponse.AIType.SPEAK || aiType == BotConstResponse.AIType.TEXT_SHUCHU) {
            stopSpeaking();
            mPresenter.stopCurrentCall();
            setCurrentChatOver();
            mPresenter.setStopRequested(true);
            textFig = false;
            mPresenter.voiceManagerStop();
            aiType = BotConstResponse.AIType.FREE;
            button.setImageResource(R.drawable.jzfason);
            mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(false);
            chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
        }
    }

    public void swOpenClose() {
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

    public void recognizeResult(RecognizerResult results, boolean isLast) {
        printResult(results, isLast);//结果数据解析
        isRecognize = false;
    }

    public void recognizeOnError(SpeechError error) {
        showMsg(error.getPlainDescription(true));
    }

    public void onSpeakCompleted() {
        button.setImageResource(R.drawable.jzfason);
        aiType = BotConstResponse.AIType.FREE;
        mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(false);
        chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
        if (isNeedWakeUp) {
//                    if (BotConstResponse.searchWeatherWaiting)
            // 检查录音权限
            boolean hasRecordPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (hasRecordPermission) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TTSbutton.performClick();
                    }
                }, 1000);

            }
        }
        mSceneAction.startActionByPosition();
        isNeedWakeUp = true;
        Log.d(TAG, "onCompleted: aiType " + aiType);
    }

    public void onSpeakBegin() {
        button.setImageResource(R.drawable.tingzhi);
        aiType = BotConstResponse.AIType.SPEAK;
        mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(true);
        chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
    }

    public void scrollByPosition(int i) {
        chatRecyclerView.scrollToPosition(i);
    }

    public void setThinkContent(int index, String text) {
        mPresenter.getChatMessages().get(index).setThinkContent(text);
    }

    public void notifyDataChanged(int index) {
        chatAdapter.notifyItemChanged(index);
        chatRecyclerView.scrollBy(0, chatRecyclerView.getLayoutManager().getHeight());
    }

    public void onTimeFinish(int position) {
        if (!mPresenter.getChatMessages().get(position).isOver()) {
            mPresenter.getChatMessages().get(position).setOver(true);
            mPresenter.TTS(BotConstResponse.searchWeatherError);
            mPresenter.getChatMessages().get(position).setMessage(BotConstResponse.searchWeatherError);
            chatAdapter.notifyItemChanged(position);
        }
    }

    public String filterSensitiveContent(String content) {
        return mPresenter.filterSensitiveContent(content);
    }

    public void requestLocationPermission() {
        mPresenter.requestLocationPermission();
    }

    //用来返回用户说的话或者输入的文本
    public interface BackTextToAction {
        void backUserText(String text);
    }

    public void callGenerateApi(String userQuestion) {
        textFig = true;
        button.setImageResource(R.drawable.tingzhi);
        //重置标识
        mPresenter.setStopRequested(false);
        mPresenter.setNewChatCome(false);
        mPresenter.callGenerateApi(userQuestion);
    }

    public void onTodayWeather(YLLocalWeatherLive weatherLive) {
        synchronized (this) {  // 添加同步块
            setCurrentChatOver();
            TimeDownUtil.clearTimeDown();
            isWeatherOutputStopped = false;
            mWeatherResult = weatherLive.getCity() + "今天的天气" + weatherLive.getWeather() +
                    "，当前的温度是" + weatherLive.getTemperature() + "摄氏度，" + weatherLive.getWindDirection() + "风"
                    + weatherLive.getWindPower() + "级，" + "湿度" + weatherLive.getHumidity() + "%";

            // 确保添加新消息是线程安全的
            runOnUiThread(() -> {
                ChatMessage chatMessage = new ChatMessage("", false);
                chatMessage.setOver(true);
                chatAdapter.notifyDataSetChanged();

                if (!isWeatherOutputStopped) {
                    mPresenter.TTS(mWeatherResult);
                    weatherIndex = 0;
                    myHandler.removeCallbacks(weatherStreamRunnable); // 清除旧回调
                    myHandler.post(weatherStreamRunnable);
                }
            });
        }
    }

    private int weatherIndex = 0;
    Runnable weatherStreamRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (MainActivity.this) {  // 添加同步
                if (isWeatherOutputStopped || weatherIndex > mWeatherResult.length()) {
                    if (mPresenter.getChatMessagesSize() > 0) {
                        mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setOver(true);
                    }
                    return;
                }

                runOnUiThread(() -> {
                    if (mPresenter.getChatMessagesSize() > 0) {
                        String currentText = mWeatherResult.substring(0, Math.min(weatherIndex, mWeatherResult.length()));
                        ChatMessage lastMsg = mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex());
                        lastMsg.setMessage(currentText);
                        lastMsg.setOver(false);
                        chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
                    }
                });

                weatherIndex++;
                myHandler.postDelayed(this, 200);
            }
        }
    };

    public void onWeatherError(String message, int rCode) {
        setCurrentChatOver();
        TimeDownUtil.clearTimeDown();
        mPresenter.getChatMessages().remove(mPresenter.getChatMessagesSizeIndex());
        mPresenter.getChatMessages().add(new ChatMessage(BotConstResponse.searchWeatherError, false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
        mPresenter.TTS(BotConstResponse.searchWeatherError);
    }


    public void onForecastWeatherSuccess(List<YLLocalWeatherForecastResult> localWeatherForecastResult) {
        setCurrentChatOver();
        TimeDownUtil.clearTimeDown();
        Log.d(TAG, "chatMessages.remove(mPresenter.getChatMessagesSize());: " + (mPresenter.getChatMessagesSize()));
        if (mPresenter.getChatMessagesSize() != 1) {
            mPresenter.getChatMessages().remove(mPresenter.getChatMessagesSizeIndex());
        }

        StringBuilder result = new StringBuilder(BotConstResponse.searchForecastWeatherSuccess);
        for (YLLocalWeatherForecastResult localDayWeatherForecast : localWeatherForecastResult) {
            int dayTemp = Integer.parseInt(localDayWeatherForecast.getDayTemp());
            int nightTemp = Integer.parseInt(localDayWeatherForecast.getNightTemp());
            result.append("\n").append("日期：").append(localDayWeatherForecast.getDate()).append("\t温度：")
                    .append(Math.min(dayTemp, nightTemp)).append("°/").append(Math.max(dayTemp, nightTemp)).append("°");
        }
        mPresenter.getChatMessages().add(new ChatMessage(result.toString(), false)); // 添加到聊天界面
        chatAdapter.notifyItemInserted(mPresenter.getChatMessagesSizeIndex());
        chatRecyclerView.scrollToPosition(mPresenter.getChatMessagesSizeIndex());
        mPresenter.TTS(result.toString());
    }

    public void changeDeepThinkMode() {
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
    }

    class MyHandler extends Handler {
        private WeakReference<Activity> weakReference;

        public MyHandler(Activity activity) {
            weakReference = new WeakReference<>(activity);
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
                    showMsg("录音权限已授予");
                } else {
                    // 录音权限被拒绝，提示用户或执行其他操作
                    showMsg("录音权限被拒绝");
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
                    showMsg("存储权限已授予");
                } else {
                    // 存储权限被拒绝
                    showMsg("存储权限被拒绝");
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
                    showMsg("定位权限已授予");
                } else {
                    // 定位权限被拒绝
                    showMsg("定位权限被拒绝");
                }

                break;
            case REQUEST_CODE_MAC_ADDRESS:
                mPresenter.handleMacAddressPermissionResult(permissions, grantResults);
                break;
        }
    }

    public void updateContext(String userQuestion, String modelResponse) {
        runOnUiThread(() -> {
            // 使用已绑定的mPresenter，避免新建实例
            mPresenter.contextQueue.add(new ChatMessage(userQuestion, true));
            mPresenter.contextQueue.add(new ChatMessage(modelResponse, false));
        });
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

    public void addMessageAndTTS(ChatMessage chatMessage, String text) {
        mPresenter.getChatMessages().add(chatMessage);
        chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
        chatRecyclerView.scrollToPosition(mPresenter.getChatMessagesSizeIndex());
        mPresenter.TTS(text);
    }

    public void addMessageByBot(String text) {
        mPresenter.getChatMessages().add(new ChatMessage(text, false, "", false));
    }

    public void setTextByIndex(int index, String text) {
        mPresenter.getChatMessages().get(index).setMessage(text);
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
        mPresenter.startTime(mPresenter.getChatMessagesSizeIndex());
    }

    public void setCurrentChatOver() {
        if (mPresenter.getChatMessagesSize() > 0) {
            setLastItem(mPresenter.getChatMessages(), item -> item.setOver(true));
        }
    }


    public void stopSpeaking() {
        mPresenter.stopSpeaking();
        if (mPresenter.getChatMessagesSize() > 0) {
            mPresenter.getChatMessages().get(mPresenter.getChatMessagesSizeIndex()).setSpeaking(false);
            chatAdapter.notifyItemChanged(mPresenter.getChatMessagesSizeIndex());
        }
    }

    public void setStopRequest(boolean isStop) {
        mPresenter.setStopRequested(isStop);
    }

    public List<ChatMessage> getChatMessages() {
        return mPresenter.getChatMessages();
    }

    public int getChatMessagesSizeIndex() {
        return mPresenter.getChatMessagesSizeIndex();
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
