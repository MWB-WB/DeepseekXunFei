package com.yl.deepseekxunfei.utlis;

import java.util.Random;

public class BotConstResponse {
    private static String[] successResponse = new String[]{"好的，以下是搜索结果", "好的，以为您搜索到如下结果", "好的，小天为您搜索到以下的内容"};
    public static String searchWeatherWaiting = "好的，正在为您查询今天的天气...";

    public static String getSuccessResponse() {
        Random random = new Random();
        int i = random.nextInt(successResponse.length);
        return successResponse[i];
    }


}
