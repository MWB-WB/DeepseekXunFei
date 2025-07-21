package com.yl.deepseekxunfei.scene.actoin;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.yl.deepseekxunfei.activity.MainActivity;
import com.yl.deepseekxunfei.broadcast.GaodeBroadcasting;
import com.yl.deepseekxunfei.fragment.RecyFragment;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.model.ComputeChildModel;
import com.yl.deepseekxunfei.model.MusicControlChildModel;
import com.yl.deepseekxunfei.model.NavControlChildModel;
import com.yl.deepseekxunfei.model.OpenAppChildMode;
import com.yl.deepseekxunfei.room.entity.AMapLocationEntity;
import com.yl.deepseekxunfei.room.ulti.JSONReader;
import com.yl.deepseekxunfei.scene.utils.GoHomeOrWorkProcessing;
import com.yl.gaodeApi.poi.ReverseGeography;
import com.yl.ylcommon.utlis.KnowledgeEntry;
import com.yl.deepseekxunfei.model.MusicChildModel;
import com.yl.deepseekxunfei.model.NavChildMode;
import com.yl.deepseekxunfei.model.WeatherChildMode;
import com.yl.deepseekxunfei.scene.NavScene;
import com.yl.ylcommon.utlis.BotConstResponse;
import com.yl.ylcommon.utlis.ContextHolder;
import com.yl.ylcommon.utlis.KnowledgeBaseLoader;
import com.yl.ylcommon.utlis.TimeDownUtil;
import com.yl.douyinapi.DouyinApi;
import com.yl.gaodeApi.poi.GeocodingApi;
import com.yl.gaodeApi.poi.NeighborhoodSearch;
import com.yl.gaodeApi.poi.OnPoiSearchListener;
import com.yl.gaodeApi.weather.WeatherAPI;
import com.yl.gaodeApi.page.LocationResult;
import com.yl.gaodeApi.poi.PositioningUtil;
import com.yl.gaodeApi.poi.GaodeKeyWordSearch;
import com.yl.gaodeApi.weather.YLLocalWeatherForecastResult;
import com.yl.gaodeApi.weather.YLLocalWeatherLive;
import com.yl.kuwo.MusicKuwo;
import com.yl.kuwo.PluginMediaModel;
import com.yl.ylcommon.ylenum.MUSIC_CONTROL;
import com.yl.ylcommon.ylenum.NAV_CONTROL;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.util.ArrayList;
import java.util.List;

public class SceneAction implements WeatherAPI.OnWeatherListener, WeatherAPI.OnForecastWeatherListener {

    private MainActivity mainActivity;
    private List<KnowledgeEntry> knowledgeBase;
    private WeatherAPI weatherAPI;
    private int currentPosition = 0;
    private List<BaseChildModel> baseChildModelList = new ArrayList<>();
    private List<BaseChildModel> navChildModelList = new ArrayList<>();
    private LocationResult wayPoint;
    private MusicKuwo musicKuwo;
    private Handler mHandler;
    public static String location;
    public static String locationScenery;
    public Context context = ContextHolder.getContext();
    public int code = 0;//设置家/公司
    public int goCode = 0;//回家/公司
    GeocodingApi geocodingApi = new GeocodingApi();

    public SceneAction(MainActivity mainActivity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("SceneAction must be created on main thread!");
        }
        this.mainActivity = mainActivity;
        musicKuwo = new MusicKuwo(mainActivity);
        // 加载本地知识库
        knowledgeBase = KnowledgeBaseLoader.loadKnowledgeBase(mainActivity);
        weatherAPI = new WeatherAPI(mainActivity);
        weatherAPI.setOnWeatherListener(this);
        weatherAPI.setOnForecastWeatherListener(this);
        mHandler = new Handler(Looper.myLooper());
    }

    public SceneAction() {

    }

    public void startActionByList(List<BaseChildModel> baseChildModelList) {
        if ((baseChildModelList.get(0).getType() == SceneTypeConst.NEARBY || baseChildModelList.get(0).getType() == SceneTypeConst.KEYWORD)
                && (baseChildModelList.get(1).getType() == SceneTypeConst.NEARBY || baseChildModelList.get(1).getType() == SceneTypeConst.KEYWORD)) {
            navChildModelList = baseChildModelList;
            waypointsAction(navChildModelList.get(0));
        } else {
            mainActivity.isNeedWakeUp = false;
            this.baseChildModelList.clear();
            this.baseChildModelList = baseChildModelList;
            currentPosition = 0;
            actionByType(this.baseChildModelList.get(0));
        }
    }

    public void startActionByPosition() {
        currentPosition++;
        if (baseChildModelList.size() - 1 >= currentPosition) {
            if (currentPosition == baseChildModelList.size() - 1) {
                mainActivity.isNeedWakeUp = true;
            }
            actionByType(baseChildModelList.get(currentPosition));
        }
    }

    public void actionByType(BaseChildModel baseChildModel) {
        mainActivity.replaceFragment(0);
        String botResponse = BotConstResponse.getSuccessResponse();
        switch (baseChildModel.getType()) {
            // 附近搜索
            case SceneTypeConst.NEARBY:
                mainActivity.requestLocationPermission();
                // 检查粗略定位权限
                boolean hasCoarseLocationNearby = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;
                if (hasCoarseLocationNearby) {
                    nearbyAction(baseChildModel, botResponse);
                } else {
                    mainActivity.addMessageAndTTS(new ChatMessage("请打开位置访问权限", false, "", false)
                            , "请打开位置访问权限");
//                    mainActivity.startTimeOut();
                }
                break;
            // 关键字导航
            case SceneTypeConst.KEYWORD:
                if (!GoHomeOrWorkProcessing.recognizeIntent(baseChildModel.getText()).equals("work") && !GoHomeOrWorkProcessing.recognizeIntent(baseChildModel.getText()).equals("home")) {
                    mainActivity.requestLocationPermission();
                    // 检查粗略定位权限
                    boolean hasCoarseLocationsKeyword = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;
                    if (hasCoarseLocationsKeyword) {
                        if (NavScene.addressLocation.toString() != null) {
                            geocodingApi.geocoding(NavScene.addressLocation.toString(), null, new GeocodingApi.success() {
                                @Override
                                public void SuccessAPI(String response) {
                                    SceneAction.location = response;
                                }
                            });
                        }
                        keyWordAction(baseChildModel, botResponse);
                    } else {
                        mainActivity.addMessageAndTTS(new ChatMessage("请打开位置访问权限", false, "", false)
                                , "请打开位置访问权限");
                    }
                } else {
                    if (baseChildModel.getText().contains("公司")) {
                        goCode = 1;
                    } else {
                        goCode = 0;
                    }
                    Log.d("TAG", "goCode: " + goCode);
                    GaodeBroadcasting.goHomeToWord(goCode, context);
                    // 先让机器人回复固定内容
                    mainActivity.addMessageAndTTS(new ChatMessage("好的", false, "", false)
                            , "好的");
                    GaodeBroadcasting.top(mainActivity);
                }
                break;
            case SceneTypeConst.NAVIGATION_UNKNOWN_ADDRESS:
                SceneAction.location = null;
                navigationUnknownAddressAction();
                break;
            case SceneTypeConst.NAVIGATION_ADDRESS_INVALIDATOR:
                SceneAction.location = null;
                //非法地址
                navigationAddressInvalidator();
                break;
            case SceneTypeConst.RECENT_FILMS:
                SceneAction.location = null;
                filmAction(baseChildModel, botResponse);
                break;
            //闲聊
            case SceneTypeConst.CHITCHAT:
                SceneAction.location = null;
                chitchatAction(baseChildModel);
                break;
            case SceneTypeConst.TODAY_WEATHER:
                SceneAction.location = null;
                todayWeatherAction(baseChildModel);
                break;
            case SceneTypeConst.FEATHER_WEATHER:
                SceneAction.location = null;
                featherWeatherAction(baseChildModel);
                break;
            case SceneTypeConst.SELECTION:
                SceneAction.location = null;
                selectionAction(baseChildModel);
                break;
            case SceneTypeConst.QUIT:
                SceneAction.location = null;
                quitAction(baseChildModel);
                break;
            case SceneTypeConst.STOP:
                SceneAction.location = null;
                stopAction();
                break;
            case SceneTypeConst.MUSIC_SEARCH:
                SceneAction.location = null;
                musicSearchAction(baseChildModel);
                break;
            case SceneTypeConst.MUSIC_START_AND_PLAY:
                SceneAction.location = null;
                musicStartAndPlayAction();
                break;
            case SceneTypeConst.HOT_SONGS:
            case SceneTypeConst.TODAY_RECOMMEND:
                SceneAction.location = null;
                hotSongsAction();
                break;
            case SceneTypeConst.MUSIC_UNKNOWN:
                SceneAction.location = null;
                musicUnknowAction();
                break;
            case SceneTypeConst.VIDEO:
                SceneAction.location = null;
                videoAction(baseChildModel);
                break;
            case SceneTypeConst.COMPUTE:
                SceneAction.location = null;
                computeAction(baseChildModel);
                break;
            case SceneTypeConst.SELFINTRODUCE:
                SceneAction.location = null;
                selfIntroduceAction();
                break;
            case SceneTypeConst.HOMECOMPANY:
                SceneAction.location = null;
                if (baseChildModel.getText().contains("公司")) {
                    code = 1;
                } else {
                    code = 0;
                }
                //1是否设置，2，设置的名称，3，纬度，4，经度，5，地址名称（），6，context对象
                Log.d("TAG", "actionByType: " + code);//0家1公司
                mainActivity.addMessageAndTTS(new ChatMessage("好的，正在为您打开高德地图", false, "", false)
                        , "好的，正在为您打开高德地图");

                GaodeBroadcasting.BroadcastingActivate(code, context);//设置家或者公司
                GaodeBroadcasting.top(mainActivity);
                break;
            case SceneTypeConst.GOHOMETOWORK:
                if (baseChildModel.getText().contains("公司")) {
                    goCode = 1;
                } else {
                    goCode = 0;
                }
                Log.d("TAG", "goCode: " + goCode);
                GaodeBroadcasting.goHomeToWord(goCode, context);
                // 先让机器人回复固定内容
                mainActivity.addMessageAndTTS(new ChatMessage("好的", false, "", false)
                        , "好的");
                GaodeBroadcasting.top(mainActivity);
                break;
            case SceneTypeConst.LOCATIONCONST:
                aMapLocationSceneAction();
                break;
            case SceneTypeConst.OPEN_APP:
                OpenAppAction(((OpenAppChildMode) baseChildModel).getAppPkgName());
                break;
            case SceneTypeConst.UNKNOWN_OPEN_APP:
                UnKnownOpenAppAction();
                break;
            case SceneTypeConst.CONTROL_MUSIC:
                MusicControlAction(baseChildModel);
                break;
            case SceneTypeConst.CONTROL_NAV:
                NavControlAction(baseChildModel);
                break;
//            case SceneTypeConst.JOKECLASS:
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                       List<String> list =  JokeUitl.init();
//                        // 先让机器人回复固定内容
//                        Random random = new Random();
//                        int listId = random.nextInt(10);
//                        if (list!=null){
//                            mainActivity.runOnUiThread(()->{
//                                mainActivity.addMessageAndTTS(new ChatMessage(list.get(listId), false, "", false),
//                                        list.get(listId));
//                            });
//                        }
//                    }
//                }).start();
//                break;
        }
    }

    private void NavControlAction(BaseChildModel baseChildModel) {
        NAV_CONTROL navControl = ((NavControlChildModel) baseChildModel).getNavControl();
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.ok, false, "", false),
                BotConstResponse.ok);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (navControl) {
                    case EXIT_NAV:
                        sendGaodeBroadcast(10010);
                        break;
                    case CONTINUE_NAV:

                        break;
                }
            }
        }, 1500);
    }

    private void sendGaodeBroadcast(int keyType){
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", keyType);
        mainActivity.sendBroadcast(intent);
    }

    private void MusicControlAction(BaseChildModel baseChildModel) {
        MUSIC_CONTROL control = ((MusicControlChildModel) baseChildModel).getControl();
        mainActivity.isNeedWakeUp = false;
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.ok, false, "", false),
                BotConstResponse.ok);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (control) {
                    case PLAY:
                    case PAUSE:
                        inputKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                        break;
                    case NEXT:
                        inputKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
                        break;
                    case PREV:
                        inputKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        break;
                }
            }
        }, 1500);

    }

    public static void inputKeyEvent(int key) {
        Log.e("TAGTEST", "inputKeyEvent: " + key);
        try {
            String keyCommand = "input keyevent = " + key;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(keyCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UnKnownOpenAppAction() {
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.unknownOpenApp, false, "", false),
                BotConstResponse.unknownOpenApp);
    }

    private void OpenAppAction(String appPkgName) {
        try {
            // 先让机器人回复固定内容
            mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.ok, false, "", false),
                    BotConstResponse.ok);
            mainActivity.isNeedWakeUp = false;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intentForPackage = mainActivity.getPackageManager().getLaunchIntentForPackage(appPkgName);
                    intentForPackage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mainActivity.startActivity(intentForPackage);
                }
            }, 1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigationAddressInvalidator() {
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.searchAddressInvalidator, false, "", false),
                BotConstResponse.searchAddressInvalidator);
    }

    private void musicStartAndPlayAction() {
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.ok, false, "", false), BotConstResponse.hotSongPlay);
        mHandler.postDelayed(() -> {
            musicKuwo.open(true);
            musicKuwo.continuePlay();
        }, 2000);
    }

    private void stopAction() {
        mainActivity.stopTTSAndRequest();
    }

    private void navigationUnknownAddressAction() {
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.searchPositionEmpty, false, "", false),
                BotConstResponse.searchPositionEmpty);
    }

    private void selfIntroduceAction() {
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.selfIntroduce, false, "", false),
                BotConstResponse.selfIntroduce);
    }

    private void computeAction(BaseChildModel baseChildModel) {
        StringBuilder response = new StringBuilder(((ComputeChildModel) baseChildModel).getResultText());
        response.append("等于").append(((ComputeChildModel) baseChildModel).getResult());
        mainActivity.addMessageAndTTS(new ChatMessage(response.toString(), false, "", false),
                response.toString());
    }

    public void musicUnknowAction() {
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.musicUnknow, false, "", false)
                , BotConstResponse.musicUnknow);
    }

    public void aMapLocationSceneAction() {
        final String[] name = {null};
        PositioningUtil posit = new PositioningUtil();
        try {
            posit.initLocation(context);
        } catch (Exception e) {
            Log.d("报错", "searchInAmap: " + e);
            throw new RuntimeException(e);
        } finally {
            posit.release();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Location", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("cityCode", "");
        String city = sharedPreferences.getString("city", "");
        float lat = sharedPreferences.getFloat("latitude", 0);
        float lot = sharedPreferences.getFloat("longitude", 0);
        String adcode = sharedPreferences.getString("adcode", "");
        Log.d("我的当前位置", "纬度::" + lat + "\t经度" + lot + "\tcity" + city + "\t区县cityCode编码" + cityCode);
        String lot_lat = lot + "," + lat;
        ReverseGeography reverseGeography = new ReverseGeography();
        reverseGeography.reverseGeographyApi(lot_lat, new ReverseGeography.successApi() {
            @Override
            public void success(String formattedAddress) {
                // 先让机器人回复固定内容
                Log.d("TAG", "success: " + formattedAddress.isEmpty());
                if (formattedAddress != null && !formattedAddress.trim().isEmpty() && !formattedAddress.equals("[]")) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        mainActivity.addMessageAndTTS(new ChatMessage("您当前所在位置是：" + formattedAddress, false, "", false),
                                formattedAddress);
                    });
                } else {
                    List<AMapLocationEntity> list = JSONReader.select(context, adcode);
                    Log.d("TAG", "查询成功：: " + list.toString());
                    for (AMapLocationEntity amapLocationEntity : list) {
                        name[0] = amapLocationEntity.getName();
                    }
                    if (name[0] != null) {
                        Log.d("TAG", "cityCode编码查询: " + cityCode);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            mainActivity.addMessageAndTTS(new ChatMessage("未获取到您的具体位置，您当前所在：" + city + name[0], false, "", false),
                                    city + cityCode);
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            mainActivity.addMessageAndTTS(new ChatMessage("定位失败，请检查定位权限是否授予", false, "", false),
                                    city + cityCode);
                        });
                    }
                }
            }
        });
    }

    private void hotSongsAction() {
        PluginMediaModel pluginMediaModel = new PluginMediaModel();
        pluginMediaModel.setKeyWords("热门歌曲");
        mainActivity.isNeedWakeUp = false;
        musicKuwo.search(pluginMediaModel, new MusicKuwo.MediaSearchCallback() {
            @Override
            public void onSuccess(List<PluginMediaModel> resultList) {
                mainActivity.runOnUiThread(() -> {
                    String response = BotConstResponse.playMusic.replace("%s", resultList.get(0).getArtists() + "的" + resultList.get(0).getTitle());
                    mainActivity.addMessageAndTTS(new ChatMessage(response, false, "", false), response);
                });
                mHandler.postDelayed(() -> {
                    musicKuwo.play(resultList, 0);
                }, 3000);
            }

            @Override
            public void onError(String text) {

            }
        });
    }

    private void videoAction(BaseChildModel baseChildModel) {
        DouyinApi.requestAuth(mainActivity);
    }

    private void musicSearchAction(BaseChildModel baseChildModel) {
        String musicName = ((MusicChildModel) baseChildModel).getMusicName();
        String artist = ((MusicChildModel) baseChildModel).getArtist();
        Log.e("TAG", "musicAction: " + musicName + ":: artist: " + artist);
        PluginMediaModel pluginMediaModel = new PluginMediaModel();
        pluginMediaModel.setKeyWords(musicName);
        pluginMediaModel.setArtist(artist);
        musicKuwo.search(pluginMediaModel, new MusicKuwo.MediaSearchCallback() {
            @Override
            public void onSuccess(List<PluginMediaModel> resultList) {
                mainActivity.runOnUiThread(() -> {
                    String response = BotConstResponse.playMusic.replace("%s", resultList.get(0).getArtists() + "的" + resultList.get(0).getTitle());
                    mainActivity.addMessageAndTTS(new ChatMessage(response, false, "", false), response);
                });
                mHandler.postDelayed(() -> {
                    musicKuwo.play(resultList, 0);
                }, 3000);
            }

            @Override
            public void onError(String text) {

            }
        });
    }

    private void quitAction(BaseChildModel baseChildModel) {
        String quitResponse = BotConstResponse.getQuitResponse();
        mainActivity.isNeedWakeUp = false;
        mainActivity.addMessageAndTTS(new ChatMessage(quitResponse, false, "", false), quitResponse);
    }

    private void selectionAction(BaseChildModel baseChildModel) {
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.ok, false, "", false), BotConstResponse.ok);
        mainActivity.selectionAction(baseChildModel.getText());
    }

    private void featherWeatherAction(BaseChildModel baseChildModel) {
        weatherAPI.weatherSearch(SceneTypeConst.FEATHER_WEATHER, ((WeatherChildMode) baseChildModel).getCity());
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.searchForecastWeatherWaiting, false, "", false)
                , BotConstResponse.searchForecastWeatherWaiting);
        mainActivity.startTimeOut();
    }

    private void todayWeatherAction(BaseChildModel baseChildModel) {
        weatherAPI.weatherSearch(SceneTypeConst.TODAY_WEATHER, ((WeatherChildMode) baseChildModel).getCity());
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.searchWeatherWaiting, false, "", false)
                , BotConstResponse.searchWeatherWaiting);
        mainActivity.startTimeOut();
    }

    private void chitchatAction(BaseChildModel baseChildModel) {
        // 搜索本地知识库
        for (KnowledgeEntry entry : knowledgeBase) {
            if (entry.getTitle().equals(baseChildModel.getText())) {
                mainActivity.setCurrentChatOver();
                mainActivity.setStopRequest(true);
                String content = mainActivity.filterSensitiveContent(entry.getContent()); // 过滤敏感词
                mainActivity.updateContext(baseChildModel.getText(), content,false); // 更新上下文
                Log.d("TAG", "nearbyAction: " + baseChildModel.getText());
                Log.d("TAG", "nearbyAction: " + content);
                mainActivity.addMessageAndTTS(new ChatMessage(entry.getContent(), false, "", false)
                        , entry.getContent());
                mainActivity.found = true;
                break;
            }
        }
        if (!mainActivity.found) {
            mainActivity.setStopRequest(false);
            mainActivity.callGenerateApi(baseChildModel.getText());
        }
    }

    private void filmAction(BaseChildModel baseChildModel, String botResponse) {
        mainActivity.setStopRequest(true);
        botResponse = botResponse + "，您可以说查看附近的影院";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.startTimeOut();
        mainActivity.showMovieFragment();
    }

    private void waypointsAction(BaseChildModel baseChildModel) {
        String location = ((NavChildMode) baseChildModel).getLocation();
        mainActivity.setStopRequest(true);
        String botResponse = "请先选择途径点，您可以说第一个";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.startTimeOut();
        String city = ((NavChildMode) baseChildModel).getEntities().stream()
                .filter(e -> e.getType() == NavChildMode.GeoEntityType.CITY)
                .findFirst()
                .map(NavChildMode.GeoEntity::getName)
                .orElse(""); // 默认使用当前城市
        Log.e("TAG123", "waypointsAction: " + city);
        NeighborhoodSearch.getLocation(city, city, cityLocation -> {
            NeighborhoodSearch.search(location, cityLocation, 5000, new OnPoiSearchListener() {
                @Override
                public void onSuccess(List<LocationResult> results) {
                    mainActivity.setCurrentChatOver();
                    TimeDownUtil.clearTimeDown();
                    mainActivity.runOnUiThread(() -> mainActivity.showWaypointsResults(results, new RecyFragment.OnWayPointClick() {
                        @Override
                        public void onClick(LocationResult result) {
                            wayPoint = result;
                            startWaypointsAction(navChildModelList.get(1));
                        }
                    }));
                }

                @Override
                public void onError(String error) {
                    mainActivity.setCurrentChatOver();
                    Log.d("搜索失败", error);
                }
            }, mainActivity);
        });
    }

    private void startWaypointsAction(BaseChildModel baseChildModel) {
        String location = ((NavChildMode) baseChildModel).getLocation();
        mainActivity.setStopRequest(true);
        String botResponse = "接下来请选择目的地";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.startTimeOut();
        String city = ((NavChildMode) baseChildModel).getEntities().stream()
                .filter(e -> e.getType() == NavChildMode.GeoEntityType.CITY)
                .findFirst()
                .map(NavChildMode.GeoEntity::getName)
                .orElse(""); // 默认使用当前城市
        NeighborhoodSearch.getLocation(city, city, cityLocation -> {
            NeighborhoodSearch.search(location, cityLocation, 5000, new OnPoiSearchListener() {
                @Override
                public void onSuccess(List<LocationResult> results) {
                    mainActivity.setCurrentChatOver();
                    TimeDownUtil.clearTimeDown();
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.showStartWaypointsResults(results, wayPoint);
                    });
                }

                @Override
                public void onError(String error) {
                    mainActivity.setCurrentChatOver();
                    Log.d("搜索失败", error);
                }
            }, mainActivity);
        });
    }

    /**
     * 附近搜索
     *
     * @param baseChildModel 问题
     * @param botResponse    固定答案
     */
    private void nearbyAction(BaseChildModel baseChildModel, String botResponse) {
        String location = ((NavChildMode) baseChildModel).getLocation();
        mainActivity.setStopRequest(true);
        botResponse = botResponse + ",您可以说第一个，最后一个";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        //获取当前所在位置
        mainActivity.updateContext(baseChildModel.getText(), botResponse,false);
        Log.d("TAG", "nearbyAction: "+baseChildModel.getText());
        mainActivity.startTimeOut();
        String city = ((NavChildMode) baseChildModel).getEntities().stream()
                .filter(e -> e.getType() == NavChildMode.GeoEntityType.CITY)
                .findFirst()
                .map(NavChildMode.GeoEntity::getName)
                .orElse(""); // 默认使用当前城市
        String finalBotResponse = botResponse;
        NeighborhoodSearch.getLocation(city, city, cityLocation -> {
            if (SceneAction.location != null) {
                cityLocation = SceneAction.location;
            }
            Log.d("TAG", "nearbyActionLocation: " + location);
            Log.d("TAG", "nearbyActionLocation: " + cityLocation.toString());
            NeighborhoodSearch.search(location, cityLocation, 5000, new OnPoiSearchListener() {
                @Override
                public void onSuccess(List<LocationResult> results) {
                    mainActivity.setCurrentChatOver();
                    TimeDownUtil.clearTimeDown();
                    mainActivity.runOnUiThread(() -> mainActivity.showSearchResults(results));
                    if (results != null && !results.isEmpty()) {
                        Log.d("附近搜索", "onSuccess: ");
                    } else {
                        mainActivity.replaceFragment(0);
                        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "未查询到相关内容", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onError(String error) {
                    mainActivity.setCurrentChatOver();
                    Log.d("搜索失败", error);
                }
            }, mainActivity);
        });
    }

    /**
     * 关键字搜索
     *
     * @param baseChildModel 问题
     * @param botResponse    固定答案
     */
    private void keyWordAction(BaseChildModel baseChildModel, String botResponse) {
        mainActivity.setStopRequest(true);
        botResponse = botResponse + "\n您可以说第一个，最后一个";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.updateContext(baseChildModel.getText(), "",false);
        Log.d("TAG", "nearbyAction: "+baseChildModel.getText());
        Log.d("TAG", "nearbyAction: "+botResponse);
        mainActivity.startTimeOut();
        String city = ((NavChildMode) baseChildModel).getEntities().stream()
                .filter(e -> e.getType() == NavChildMode.GeoEntityType.CITY)
                .findFirst()
                .map(NavChildMode.GeoEntity::getName)
                .orElse(""); // 默认使用当前城市
        GaodeKeyWordSearch.searchInAmap(mainActivity, ((NavChildMode) baseChildModel).getLocation(), city, new OnPoiSearchListener() {
            @Override
            public void onSuccess(List<LocationResult> results) {
                mainActivity.setCurrentChatOver();
                TimeDownUtil.clearTimeDown();
                mainActivity.runOnUiThread(() -> mainActivity.showSearchResults(results));
                if (results != null && !results.isEmpty()) {
                    Log.d("关键字导航", "onSuccess: ");
                } else {
                    mainActivity.replaceFragment(0);
                    mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "未查询到相关内容", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                mainActivity.setCurrentChatOver();
            }
        });
    }

    @Override
    public void onWeatherSuccess(List<YLLocalWeatherForecastResult> localWeatherForecastResult) {
        mainActivity.onForecastWeatherSuccess(localWeatherForecastResult);
    }

    @Override
    public void onWeatherSuccess(YLLocalWeatherLive weatherLive) {
        mainActivity.onTodayWeather(weatherLive);
    }

    @Override
    public void onWeatherError(String message, int rCode) {
        mainActivity.onWeatherError(message, rCode);
    }
}
