package com.yl.deepseekxunfei.scene.actoin;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.google.gson.Gson;
import com.yl.deepseekxunfei.APICalls.GeocodingApi;
import com.yl.deepseekxunfei.APICalls.NeighborhoodSearch;
import com.yl.deepseekxunfei.APICalls.WeatherAPI;
import com.yl.deepseekxunfei.MainActivity;
import com.yl.deepseekxunfei.OnPoiSearchListener;
import com.yl.deepseekxunfei.fragment.RecyFragment;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.ChatMessage;
import com.yl.deepseekxunfei.model.ComputeChildModel;
import com.yl.deepseekxunfei.model.KnowledgeEntry;
import com.yl.deepseekxunfei.model.MusicChildModel;
import com.yl.deepseekxunfei.model.NavChildMode;
import com.yl.deepseekxunfei.model.PluginMediaModel;
import com.yl.deepseekxunfei.model.WeatherChildMode;
import com.yl.deepseekxunfei.page.LocationResult;
import com.yl.deepseekxunfei.scene.NavScene;
import com.yl.deepseekxunfei.utlis.BotConstResponse;
import com.yl.deepseekxunfei.utlis.KnowledgeBaseLoader;
import com.yl.deepseekxunfei.utlis.MusicKuwo;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;
import com.yl.deepseekxunfei.utlis.TimeDownUtil;
import com.yl.deepseekxunfei.utlis.searchIn;
import com.yl.douyinapi.DouyinApi;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public static String  location;

    public SceneAction(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        musicKuwo = new MusicKuwo(mainActivity);
        // 加载本地知识库
        knowledgeBase = KnowledgeBaseLoader.loadKnowledgeBase(mainActivity);
        weatherAPI = new WeatherAPI();
        weatherAPI.setOnWeatherListener(this);
        weatherAPI.setOnForecastWeatherListener(this);
        mHandler = new Handler(Looper.myLooper());
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
                nearbyAction(baseChildModel, botResponse);
                break;
            // 关键字导航
            case SceneTypeConst.KEYWORD:
                if (NavScene.addressLocation.toString()!=null){
                    GeocodingApi geocodingApi = new GeocodingApi();
                    geocodingApi.geocoding(NavScene.addressLocation.toString(), null, new GeocodingApi.success() {
                        @Override
                        public void SuccessAPI(String response) {
                            SceneAction.location = response;
                        }
                    });
                }
                keyWordAction(baseChildModel, botResponse);
                break;
            case SceneTypeConst.NAVIGATION_UNKNOWN_ADDRESS:
                SceneAction.location = null;
                navigationUnknownAddressAction();
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
            case SceneTypeConst.MUSIC_UNKNOW:
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

    private void musicUnknowAction() {
        mainActivity.addMessageAndTTS(new ChatMessage(BotConstResponse.musicUnknow, false, "", false)
                , BotConstResponse.musicUnknow);
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
        Log.e("TAG", "musicAction: " + musicName + ":: artist: "+ artist);
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
                mainActivity.isStopRequested = true;
                String content = mainActivity.filterSensitiveContent(entry.getContent()); // 过滤敏感词
                mainActivity.updateContext(baseChildModel.getText(), content); // 更新上下文
                mainActivity.addMessageAndTTS(new ChatMessage(entry.getContent(), false, "", false)
                        , entry.getContent());
                mainActivity.found = true;
                break;
            }
        }
        if (!mainActivity.found) {
            mainActivity.isStopRequested = false;
            try {
                mainActivity.callGenerateApi(baseChildModel.getText());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void filmAction(BaseChildModel baseChildModel, String botResponse) {
        mainActivity.isStopRequested = true;
        botResponse = botResponse + "\n您可以说查看附近的影院";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.startTimeOut();
        mainActivity.showMovieFragment();
    }

    private void waypointsAction(BaseChildModel baseChildModel) {
        String location = ((NavChildMode) baseChildModel).getLocation();
        mainActivity.isStopRequested = true;
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
        mainActivity.isStopRequested = true;
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

    private void nearbyAction(BaseChildModel baseChildModel, String botResponse) {
        String location = ((NavChildMode) baseChildModel).getLocation();
        mainActivity.isStopRequested = true;
        botResponse = botResponse + "\n您可以说第一个，最后一个";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.updateContext(baseChildModel.getText(), botResponse);
        mainActivity.startTimeOut();
        String city = ((NavChildMode) baseChildModel).getEntities().stream()
                .filter(e -> e.getType() == NavChildMode.GeoEntityType.CITY)
                .findFirst()
                .map(NavChildMode.GeoEntity::getName)
                .orElse(""); // 默认使用当前城市
        NeighborhoodSearch.getLocation(city, city, cityLocation -> {
            if (SceneAction.location!=null){
                cityLocation = SceneAction.location;
            }
            Log.d("TAG", "nearbyActionLocation: "+location);
            Log.d("TAG", "nearbyActionLocation: "+cityLocation.toString());
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

    private void keyWordAction(BaseChildModel baseChildModel, String botResponse) {
        mainActivity.isStopRequested = true;
        botResponse = botResponse + "\n您可以说第一个，最后一个";
        // 先让机器人回复固定内容
        mainActivity.addMessageAndTTS(new ChatMessage(botResponse, false, "", false)
                , botResponse);
        mainActivity.updateContext(baseChildModel.getText(), botResponse);
        mainActivity.startTimeOut();
        String city = ((NavChildMode) baseChildModel).getEntities().stream()
                .filter(e -> e.getType() == NavChildMode.GeoEntityType.CITY)
                .findFirst()
                .map(NavChildMode.GeoEntity::getName)
                .orElse(""); // 默认使用当前城市
        searchIn.searchInAmap(mainActivity, ((NavChildMode) baseChildModel).getLocation(), city, new OnPoiSearchListener() {
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
    public void onWeatherSuccess(LocalWeatherLive weatherLive) {
        mainActivity.onTodayWeather(weatherLive);
    }

    @Override
    public void onWeatherError(String message, int rCode) {
        mainActivity.onWeatherError(message, rCode);
    }

    @Override
    public void onWeatherSuccess(LocalWeatherForecastResult localWeatherForecastResult) {
        mainActivity.onForecastWeatherSuccess(localWeatherForecastResult);
    }

}
