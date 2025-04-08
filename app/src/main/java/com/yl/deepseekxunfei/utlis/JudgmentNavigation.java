package com.yl.deepseekxunfei.utlis;


import com.yl.deepseekxunfei.sceneenum.SceneType;

/**
 * 判断是否是导航关键字
 */
public class JudgmentNavigation {
    /**
     * 导航关键字判断工具类（支持附近搜索和关键字搜索细分）
     */
    /**
     * 判断是否是导航相关问题，并返回具体类型
     *
     * @param input 用户输入
     * @return SceneType枚举（NEARBY-附近搜索, KEYWORD-关键字搜索, NON_NAV-非导航）
     */
    // 新增音乐/歌曲相关排除关键词
    public final String[] MUSIC_EXCLUDE_WORDS = {
            "歌", "歌曲", "音乐", "专辑", "单曲", "歌手", "演唱",
            "好听", "推荐", "播放", "听听", "旋律", "歌词",
            "安和桥", "成都", "蓝莲花" // 可以扩展更多歌曲名
    };

    /**
     * 判断是否是附近地点搜索
     */
    public boolean isNearbySearch(String input) {
        String[][] nearbyKeywords = {
                // 附近搜索关键词
                {"附近", "周围", "周边", "旁边", "跟前", "邻近", "临近", "邻近", "临近"},
                // 距离相关词
                {"最近", "最近的", "较近", "比较近", "挺近", "蛮近"},
                // 存在性询问
                {"有没有", "是否有", "哪有", "哪里有", "哪儿有"}
        };

        String lowerInput = input.toLowerCase();
        // 检查是否包含附近搜索关键词
        for (String[] category : nearbyKeywords) {
            for (String keyword : category) {
                if (lowerInput.contains(keyword)) {
                    // 必须包含地点参考
                    if (containsLocationReference(input)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 判断是否是关键字导航搜索
     */
    public boolean isKeywordNavigation(String input) {
        String[][] keywordNavPhrases = {
                // 导航动作词
                {"导航去", "导航到", "带我去", "带我到", "领我去", "领我到"},
                // 路线询问
                {"怎么去", "如何去", "怎样去", "怎么到", "如何到", "怎样到"},
                // 交通方式+地点
                {"开车去", "坐车去", "打车去", "步行去", "骑车去", "公交去"}
        };

        String lowerInput = input.toLowerCase();

        // 检查是否包含关键字导航短语
        for (String[] category : keywordNavPhrases) {
            for (String phrase : category) {
                if (lowerInput.contains(phrase)) {
                    return true;
                }
            }
        }

        // 检查"去/到+地点"模式
        if (lowerInput.matches(".*(去|到|往|前往)\\s?[^\\s]+(怎么走|路线|怎么去|如何走).*")) {
            return true;
        }

        return false;
    }

    // 排除明显不是导航的查询（同原有方法）

    /**
     * 增强版非导航查询判断
     */
    public boolean isOtherServiceQuery(String input) {
        // 原有排除关键词
        String[] commonExcludeKeywords = {
                "天气", "新闻", "时间", "日期", "股票", "汇率", "翻译"
        };
        // 在isOtherServiceQuery()中添加商品关键词排除
        String[] PRODUCT_WORDS = {
                "手机", "电脑", "电视", "耳机", "iPhone", "小米", "华为", "购买", "买", "售价"
        };
        String[] MEDIA_WORDS = {
                "书", "小说", "电影", "电视剧", "综艺", "《", "》", "章节"
        };

        // 检查常规排除词
        for (String word : commonExcludeKeywords) {
            if (input.contains(word)) {
                return true;
            }
        }
        // 检查常规排除词
        for (String word : MEDIA_WORDS) {
            if (input.contains(word)) {
                return true;
            }
        }
        // 检查常规排除词
        for (String word : PRODUCT_WORDS) {
            if (input.contains(word)) {
                return true;
            }
        }

        // 新增音乐类排除检查
        for (String word : MUSIC_EXCLUDE_WORDS) {
            if (input.contains(word)) {
                return true;
            }
        }

        // 检查歌曲询问模式
        if (input.matches(".*(有没有|是否|可以).*好听的.*歌.*") ||
                input.matches(".*(推荐|介绍).*(歌|音乐).*")) {
            return true;
        }
        return false;
    }

    // 验证是否包含地点参考（优化版）
    public boolean containsLocationReference(String input) {
        // 基础地点词汇
        String[] locationIndicators = {
                "医院", "酒店", "学校", "超市", "商场", "机场", "车站",
                "路", "街", "道", "巷", "号", "大厦", "中心", "广场",
                "餐厅", "饭店", "银行", "公园", "景区", "地铁", "公交",
                "好玩的","好吃的","停车","停车场"
        };
        // 检查明确地点名词
        for (String indicator : locationIndicators) {
            if (input.contains(indicator)) {
                return true;
            }
        }
        // 通过句式区分推荐类查询
        if (input.matches(".*有没有比.*更(好吃|好喝|划算).*")) {
            return false;
        }
        // 检查地址模式（如"XX路XX号"）
        if (input.matches(".*[路街巷道]\\s?\\d+[号栋幢].*")) {
            return true;
        }

        // 检查地标名称（两个中文字符以上）
        if (input.matches(".*[\\u4e00-\\u9fa5]{2,}(广场|中心|大厦|大楼|商城).*")) {
            return true;
        }
        return false;
    }
}
