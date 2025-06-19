package com.yl.kuwo;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.yl.ylcommon.ylenum.LOOP_MODE;

import java.util.ArrayList;
import java.util.List;

import cn.kuwo.autosdk.api.KWAPI;
import cn.kuwo.autosdk.api.OnExitListener;
import cn.kuwo.autosdk.api.OnPlayerStatusListener;
import cn.kuwo.autosdk.api.OnSearchListener;
import cn.kuwo.autosdk.api.PlayMode;
import cn.kuwo.autosdk.api.PlayState;
import cn.kuwo.autosdk.api.PlayerStatus;
import cn.kuwo.autosdk.api.SearchStatus;
import cn.kuwo.base.bean.Music;

public class MusicKuwo {
    private static final String LOG_TAG = "MusicKuwo_6_0::";
    private static String PACKAGE_NAME = "cn.kuwo.kwmusiccar";
    private static KWAPI mKWAPI = null;

    // 是否显示搜索列表
    private boolean bShowSearchResult = false;
    // 当前正在播放的节目
    private Music mPlayingModel;
    private Context mContext;
    private Handler mHandler;

    /*
     * 记录酷我的关闭状态
     *
     * 酷我在退出后调用next/prev/continuePlay不会启动播放, 所以在退出时将此标志位置true, 调用上述接口
     * 时如果酷我已退出, 先执行启动界面逻辑.
     * 此标志需要在启动后播放开始时更新, 避免每次都打开界面.
     * */
    private boolean bKuwoExited = true;

    Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            mKWAPI.exitAPP(mContext);
        }
    };

    public MusicKuwo(final Context context) {
        mContext = context;
        mHandler = new Handler();
        try {
            mKWAPI = KWAPI.createKWAPI(mContext, "auto");

            mKWAPI.registerPlayerStatusListener(mContext, new OnPlayerStatusListener() {
                @Override
                public void onPlayerStatus(final PlayerStatus playerStatus, final Music music) {
                    if (PlayerStatus.PLAYING == playerStatus) {
                        log("update status: playing");
//                        notifyPlayerStatusChange(PLAYER_STATUS.PLAYING);
//                        mPlayerStatus = PLAYER_STATUS.PLAYING;
                        mPlayingModel = music;
                        bKuwoExited = false;
                    } else if (PlayerStatus.BUFFERING == playerStatus) {
                        log("update status: buffering(ignored)");
//                        notifyPlayerStatusChange(PLAYER_STATUS.BUFFERING);
//                        mPlayerStatus = PLAYER_STATUS.BUFFERING;
                        mPlayingModel = music;
                        bKuwoExited = false;
                    } else if (PlayerStatus.PAUSE == playerStatus) {
                        log("update status: paused");
//                        notifyPlayerStatusChange(PLAYER_STATUS.PAUSED);
//                        mPlayerStatus = PLAYER_STATUS.PAUSED;
                    } else if (PlayerStatus.STOP == playerStatus) {
                        log("update status: stopped");
//                        notifyPlayerStatusChange(PLAYER_STATUS.STOPPED);
//                        mPlayerStatus = PLAYER_STATUS.STOPPED;
                    } else if (PlayerStatus.INIT == playerStatus) {
                        log("update status: idle(ignored)");
//                        notifyPlayerStatusChange(PLAYER_STATUS.IDLE);
//                        mPlayerStatus = PLAYER_STATUS.IDLE;
                        bKuwoExited = false;
                    }
                }
            });

            mKWAPI.registerExitListener(mContext, new OnExitListener() {
                @Override
                public void onExit() {
                    log("kuwo update status: exit");
                    bKuwoExited = true;
//                    mPlayerStatus = PLAYER_STATUS.IDLE;
                }
            });
        } catch (Exception e) {
            Log.e("load kuwo jar error", e.toString());
        }
    }

    private List<Music> mLastSearchResult;


    public void search(final PluginMediaModel model, final MediaSearchCallback callback) {
        String name = "";
        String artist = "";
        String album = "";

        if (!TextUtils.isEmpty(model.getTitle())) {
            name = model.getTitle();
        } else if (null != model.getKeyWords()) {
            name = model.getKeyWords();
        }

        if (null != model.getArtists()) {
            artist = model.getArtists();
        }

        if (!TextUtils.isEmpty(model.getAlbum())) {
            album = model.getAlbum();
        }

        final String keyword = (TextUtils.isEmpty(artist) ? "" : artist) + " " + name + " "
                + album;

        log("musicKw search: keyword = " + keyword);
        mKWAPI.searchOnlineMusic(keyword, new OnSearchListener() {
            @Override
            public void searchFinshed(final SearchStatus searchStatus, final boolean b, final
            List list, final boolean b1) {
                if (searchStatus == SearchStatus.SUCCESS) {
                    log("musicKw search: keyword = " + keyword + ", success");
                    mLastSearchResult = (List<Music>) list;
                    List<PluginMediaModel> resultList = new ArrayList<>();
                    for (Object obj : list) {
                        Music music = (Music) obj;
                        PluginMediaModel model = new PluginMediaModel();
                        model.setTitle(music.name);
                        model.setArtist(music.artist);
                        resultList.add(model);
                    }
                    callback.onSuccess(resultList);
                } else {
                    log("musicKw search: keyword = " + keyword + ", failed");
                    callback.onError("search error");
                }
            }
        });
    }

//    public void searchHotSong(){
//        mKWAPI.searchOnlineMusic();
//    }

    public void play(final List<PluginMediaModel> list, final int index) {
        try {
            log("play index: " + index);
            // kuwo答复：最后一个参数传true就是把你传的列表替换掉当前播放的列表。解决播放歌曲不是指定索引为歌曲问题
            mKWAPI.playMusic(mContext, mLastSearchResult, index, true, false, true);
        } catch (Exception e) {
            MusicKuwo.this.log("playMusic encountered error: " + e.toString());
        }
    }

    public void open(final boolean play) {
        mKWAPI.startAPP(mContext, true);
    }

    public void exit() {
        mHandler.removeCallbacks(mExitRunnable);
        mHandler.postDelayed(mExitRunnable, 0);
    }

    public void play(final PluginMediaModel model) {

    }

    public void stop() {
       mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mKWAPI.setPlayState(mContext, PlayState.STATE_PAUSE);
            }
        }, 0);
    }

    public void pause() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mKWAPI.setPlayState(mContext, PlayState.STATE_PAUSE);
            }
        }, 0);
    }

    public void continuePlay() {
        if (bKuwoExited) {
            log("musicKw exited, open first");
            open(true);
            bKuwoExited = false;
        }

        log("op continuePlay");
        mHandler.removeCallbacks(mExitRunnable);
        mKWAPI.setPlayState(mContext, PlayState.STATE_PLAY);
    }

    public void next() {
        if (bKuwoExited) {
            log("musicKw exited, open first");
            open(true);
            bKuwoExited = false;
        }

        log("op next");
        mHandler.removeCallbacks(mExitRunnable);
        mKWAPI.setPlayState(mContext, PlayState.STATE_NEXT);
    }

    public void prev() {
        if (bKuwoExited) {
            log("musicKw exited, open first");
            open(true);
            bKuwoExited = false;
        }

        log("op prev");
        mHandler.removeCallbacks(mExitRunnable);
        mKWAPI.setPlayState(mContext, PlayState.STATE_PRE);
    }

    public void switchLoopMode(final LOOP_MODE mode) {
        switch (mode) {
            case SEQUENTIAL:
                mKWAPI.setPlayMode(mContext, PlayMode.MODE_ALL_ORDER);
                break;

            case LIST_LOOP:
                mKWAPI.setPlayMode(mContext, PlayMode.MODE_ALL_CIRCLE);
                break;

            case SINGLE_LOOP:
                mKWAPI.setPlayMode(mContext, PlayMode.MODE_SINGLE_CIRCLE);
                break;

            case SHUFFLE:
                mKWAPI.setPlayMode(mContext, PlayMode.MODE_ALL_RANDOM);
                break;
        }
    }

    public PluginMediaModel getPlayingModel() {
        if (null == mPlayingModel) {
            return null;
        }

        PluginMediaModel model = new PluginMediaModel();
        model.setTitle(mPlayingModel.name);
        model.setArtist(mPlayingModel.artist);
        model.setAlbum(mPlayingModel.album);

        return model;
    }

    // logger
    private void log(String msg) {
        Log.d("PluginMediaTool", LOG_TAG + msg);
    }

    public interface MediaSearchCallback {
        void onSuccess(List<PluginMediaModel> resultList);

        void onError(String text);
    }

}
