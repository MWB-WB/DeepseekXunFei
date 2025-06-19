package com.yl.deepseekxunfei.scene;

import android.util.Log;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.yl.deepseekxunfei.model.BaseChildModel;
import com.yl.deepseekxunfei.model.MusicChildModel;
import com.yl.deepseekxunfei.model.SceneModel;
import com.yl.ylcommon.ylsceneconst.SceneTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MusicScene extends BaseChildScene {

    static {
        // 加载自定义词典（首次初始化时执行）
        CustomDictionary.add("热门歌曲");
        CustomDictionary.add("今日推荐");
        CustomDictionary.add("新歌速递");
        CustomDictionary.add("飙升榜");
    }

    private String[] musicV = new String[]{"播放", "播放一首", "我要听", "来一首", "放", "我想听"};
    private String[] startAndPlayCommand = new String[]{"来首歌", "来一首歌", "放一首歌", "放首歌", "我想听歌", "播放歌曲", "播放音乐"
            , "来首音乐", "来一首音乐", "放一首音乐", "放首音乐", "我想听音乐"};

    // 结果封装类
    public class MusicData {
        public List<String> musicNames = new ArrayList<>();
        public String artist;
        public boolean hasHotSongs = false;
        public boolean hasTodayRecommend = false;
        public boolean isStartAndPlay = false;
    }

    public MusicData extract(String text) {
        MusicData result = new MusicData();

        // 第二步：HanLP分词处理
        List<Term> terms = HanLP.segment(text);
        for (Term term : terms) {
            String word = term.word;
            Log.e("TAG123", "word: " + term.word + ":: nature: " + term.nature);
            // 识别关键词标签
            switch (word) {
                case "热门歌曲":
                case "热歌榜":
                    result.hasHotSongs = true;
                    break;
                case "今日推荐":
                case "每日推荐":
                    result.hasTodayRecommend = true;
                    break;
            }

            // 识别非书名号音乐名称（需要扩展规则）
            if (isMusicName(word, term.nature.toString())) {
                if (word.length() >= 2) {
                    result.musicNames.add(word);
                }
            }
            if (term.nature.startsWith("nr")) {
                if (word.length() >= 2) {
                    result.artist = word;
                }
            }
        }
        if (Arrays.asList(startAndPlayCommand).contains(text)) {
            result.isStartAndPlay = true;
        }
        if (result.musicNames.size() == 0) {
            Optional<String> first = Arrays.stream(musicV).filter(t -> {
                return text.contains(t);
            }).findFirst();
            if (!first.isEmpty()) {
                String musicName = text.split(first.get())[1];
                if (!musicName.equals("。"))
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
        if (musicData.hasHotSongs) {
            musicChildModel.setKeyWord("热门推荐");
            musicChildModel.setType(SceneTypeConst.HOT_SONGS);
        } else if (musicData.hasTodayRecommend) {
            musicChildModel.setKeyWord("今日推荐");
            musicChildModel.setType(SceneTypeConst.TODAY_RECOMMEND);
        } else if (musicData.isStartAndPlay) {
            musicChildModel.setType(SceneTypeConst.MUSIC_START_AND_PLAY);
        } else if (musicData.musicNames.size() > 0) {
            musicChildModel.setMusicName(musicData.musicNames.get(0));
            musicChildModel.setArtist(musicData.artist);
            musicChildModel.setKeyWord(musicData.musicNames.get(0));
            musicChildModel.setType(SceneTypeConst.MUSIC_SEARCH);
        } else {
            musicChildModel.setType(SceneTypeConst.MUSIC_UNKNOW);
        }
        return musicChildModel;
    }
}
