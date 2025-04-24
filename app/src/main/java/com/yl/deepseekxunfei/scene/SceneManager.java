package com.yl.deepseekxunfei.scene;

import android.util.Log;

import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.model.WordNLPModel;
import com.yl.deepseekxunfei.sceneenum.SceneType;
import com.yl.deepseekxunfei.utlis.ChineseSegmentationUtil;
import com.yl.deepseekxunfei.utlis.OptionPositionParser;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

public class SceneManager {

    private WeatherScene weatherScene;
    private NavScene navScene;
    private MovieScene movieScene;
    private VideoScene videoScene;
    private MusicScene musicScene;
    private SceneType currentSceneType = SceneType.CHITCHAT;
    private int currentChildSceneType = SceneTypeConst.CHITCHAT;

    public SceneManager() {
        initChildScene();
    }

    private void initChildScene() {
        weatherScene = new WeatherScene();
        navScene = new NavScene();
        movieScene = new MovieScene();
        videoScene = new VideoScene();
        musicScene = new MusicScene();
    }

    //解析场景
    public SceneModel parseQuestionToScene(String text) {
        WordNLPModel wordNLPModel = ChineseSegmentationUtil.SegmentWords(text);
        Log.e("TAG", "parseQuestionToScene: " + text + " wordNLPModel: " + wordNLPModel.toString());
        SceneModel resultModel = new SceneModel();
        resultModel.setText(text);
        if (text.startsWith("导航") || text.contains("附近的")) {
            resultModel.setScene(SceneType.NAVIGATION);
        } else if (text.startsWith("播放") || text.contains("音乐") || text.startsWith("我要听")) {
            resultModel.setScene(SceneType.MUSIC);
        } else if (text.contains("当前位置") || text.contains("我在哪")) {
            resultModel.setScene(SceneType.LOCATION);
        } else if (wordNLPModel.getN().contains("天气")) {
            resultModel.setScene(SceneType.WEATHER);
        } else if (wordNLPModel.getN().contains("电影")) {
            resultModel.setScene(SceneType.MOVIE);
        } else if (wordNLPModel.getN().contains("视频")) {
            resultModel.setScene(SceneType.VIDEO);
        } else if (OptionPositionParser.parsePosition(text)){
            if (currentSceneType.equals(SceneType.NAVIGATION) || currentSceneType.equals(SceneType.MUSIC)) {
                resultModel.setScene(SceneType.SELECTION);
            } else {
                resultModel.setScene(SceneType.CHITCHAT);
            }
        } else {
            resultModel.setScene(SceneType.CHITCHAT);
        }
        currentSceneType = resultModel.getScene();
        return resultModel;
    }


    //分发场景
    public BaseChildModel distributeScene(SceneModel sceneModel) {
        BaseChildModel baseChildModel;
        switch (sceneModel.getScene()) {
            case WEATHER:
                baseChildModel = weatherScene.parseSceneToChild(sceneModel);
                currentChildSceneType = baseChildModel.getType();
                break;
            case NAVIGATION:
                baseChildModel = navScene.parseSceneToChild(sceneModel);
                currentChildSceneType = baseChildModel.getType();
                break;
            case MOVIE:
                baseChildModel = movieScene.parseSceneToChild(sceneModel);
                currentChildSceneType = baseChildModel.getType();
                break;
            case CHITCHAT:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.CHITCHAT);
                baseChildModel.setText(sceneModel.getText());
                currentChildSceneType = SceneTypeConst.CHITCHAT;
                break;
            case VIDEO:
                baseChildModel = videoScene.parseSceneToChild(sceneModel);
                currentChildSceneType = baseChildModel.getType();
                break;
            case MUSIC:
                baseChildModel = musicScene.parseSceneToChild(sceneModel);
                currentChildSceneType = baseChildModel.getType();
                break;
            case SELECTION:
                baseChildModel = new BaseChildModel();
                baseChildModel.setType(SceneTypeConst.SELECTION);
                baseChildModel.setText(sceneModel.getText());
                currentChildSceneType = SceneTypeConst.SELECTION;
                break;
            default:
                baseChildModel = new BaseChildModel();
        }
        return baseChildModel;
    }


}
