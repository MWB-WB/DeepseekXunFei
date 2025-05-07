package com.yl.deepseekxunfei.scene;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.MusicChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.deepseekxunfei.utlis.SceneTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicScene extends BaseChildScene {

    static {
        // 加载自定义词典（首次初始化时执行）
        CustomDictionary.add("热门歌曲");
        CustomDictionary.add("今日推荐");
        CustomDictionary.add("新歌速递");
        CustomDictionary.add("飙升榜");
    }

    private String[] musicV = new String[]{"播放", "播放一首", "我要听", "来一首", "放", "我想听"};

    // 结果封装类
    public class MusicData {
        public List<String> musicNames = new ArrayList<>();
        public boolean hasHotSongs = false;
        public boolean hasTodayRecommend = false;
    }

    public MusicData extract(String text) {
        MusicData result = new MusicData();

        // 第二步：HanLP分词处理
        List<Term> terms = HanLP.segment(text);
        for (Term term : terms) {
            String word = term.word;

            // 识别关键词标签
            switch (word) {
                case "热门歌曲":
                    result.hasHotSongs = true;
                    break;
                case "今日推荐":
                    result.hasTodayRecommend = true;
                    break;
            }

            // 识别非书名号音乐名称（需要扩展规则）
            if (isMusicName(word, term.nature.toString())) {
                result.musicNames.add(word);
            }
        }
        if (result.musicNames.size() == 0) {
            Optional<String> first = Arrays.stream(musicV).filter(t -> {
                return text.contains(t);
            }).findFirst();
            if (!first.isEmpty()) {
                String musicName = text.split(first.get())[1];
                result.musicNames.add(musicName);
            }
        }
        return result;
    }

    // 判断是否为音乐名称（需要扩展规则）
    private boolean isMusicName(String word, String nature) {
        // 规则1：特定词性判断
        if (nature.startsWith("nz")) { // 其他专名
            return true;
        }
        return "n".equals(nature);
    }


    @Override
    public BaseChildModel parseSceneToChild(SceneModel sceneModel) {
        String text = sceneModel.getText();
        MusicData musicData = extract(text);
        MusicChildModel musicChildModel = new MusicChildModel();
        musicChildModel.setText(text);
        musicChildModel.setMusicName(musicData.musicNames.get(0));
        if (musicData.hasHotSongs) {
            musicChildModel.setKeyWord("热门推荐");
        } else if (musicData.hasTodayRecommend) {
            musicChildModel.setKeyWord("今日推荐");
        }
        musicChildModel.setType(SceneTypeConst.MUSIC);
        return musicChildModel;
    }
}
