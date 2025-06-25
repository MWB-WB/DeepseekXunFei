package com.yl.ylcommon.utlis;

import java.util.Random;

public class BotConstResponse {
    private static String[] successResponse = new String[]{"好的，以下是搜索结果", "好的，以为您搜索到如下结果", "好的，小天为您搜索到以下的内容"};
    public static String[] quitCommand = new String[]{"退出。", "再见。", "拜拜。", "你走吧。", "退下。"};
    public static String[] stopCommand = new String[]{"暂停"};
    public static String[] quitResponse = new String[]{"好的，有事再叫我", "再见", "拜拜", "我走啦"};
    public static String searchWeatherWaiting = "好的，正在为您查询今天的天气...";
    public static String musicUnknow = "您是不是没有说歌曲名称呀，小天不知道该怎么操作呢，您可以说播放稻香试试";
    public static String hotSongPlay = "好的，小天将为您播放每日推荐";
    public static String playMusic = "好的，小天将为您播放%s";
    public static String selfIntroduce = "我是智能助手小天，能为您提供天气查询、导航等服务~";
    public static String wantNavigation = "您是不是想要小天帮您导航过去？";
    public static String[] yes = new String[]{"是的。", "没错。", "嗯。", "可以。", "要的。", "要。", "需要。"};
    public static String ok = "好的";
    public static String searchMusic = "好的，正在为您查询...,请稍后";
    public static String searchForecastWeatherWaiting = "好的，正在为您查询最近的天气...";
    public static String searchForecastWeatherSuccess = "好的，为您搜索到最近的天气";
    public static String searchWeatherError = "当前网络波动较大，请稍后尝试";
    public static String searchPositionEmpty = "您还没有选择目的地，请详细说明目的地。";
    public static String searchAddressInvalidator = "您要导航的目的地有误，请重新调整目的地。";

    public static String getSuccessResponse() {
        Random random = new Random();
        int i = random.nextInt(successResponse.length);
        return successResponse[i];
    }

    public static String getQuitResponse() {
        Random random = new Random();
        int i = random.nextInt(quitResponse.length);
        return quitResponse[i];
    }

    public enum AIType {
        FREE,
        SPEAK,
        TEXT_SHUCHU,
        TEXT_READY,
        TEXT_NO_READY
    }
}
