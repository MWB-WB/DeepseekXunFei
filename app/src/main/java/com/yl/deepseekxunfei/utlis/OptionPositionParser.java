package com.yl.deepseekxunfei.utlis;

import android.util.Log;

import java.util.regex.*;

public class OptionPositionParser {

    // 中文数字映射表
    private static final String[] CN_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    /**
     * 解析用户输入的选项位置
     *
     * @param input        用户输入文本
     * @param totalOptions 总选项数
     * @return 选项索引（从0开始），无效输入返回-1
     */
    public static int parsePosition(String input, int totalOptions) {
        if (totalOptions <= 0) return -1;

        // 预处理：移除空格并转为小写
        String text = input.replaceAll("\\s+", "").toLowerCase();

        // 匹配优先级：1.最后 2.中文序数 3.数字 4.英文关键词
        if (checkLast(text, totalOptions)) {
            return totalOptions - 1;
        }

        int num = parseChineseOrdinal(text);
        if (num == -1) {
            num = parseNumeric(text);
        }
        if (num == -1) {
            num = parseEnglishKeyword(text);
        }

        // 验证有效性
        if (num > 0 && num <= totalOptions) {
            return num - 1; // 转换为0-based索引
        }
        return -1;
    }

    /**
     * 解析用户输入的选项位置
     *
     * @param input 用户输入文本
     * @return 选项索引（从0开始），无效输入返回-1
     */
    public static boolean parsePosition(String input) {

        // 预处理：移除空格并转为小写
        String text = input.replaceAll("\\s+", "").toLowerCase();

        // 匹配优先级：1.最后 2.中文序数 3.数字 4.英文关键词
        if (checkLast(text)) {
            return true;
        }

        int num = parseChineseOrdinal(text);
//        if (num == -1) {
//            num = parseNumeric(text);
//        }
//        if (num == -1) {
//            num = parseEnglishKeyword(text);
//        }
        return num != -1;
    }

    // 检查"最后"类关键词
    private static boolean checkLast(String text, int total) {
        return text.matches(".*(最后|末尾|倒数第一|尾).*");
    }

    // 检查"最后"类关键词
    private static boolean checkLast(String text) {
        return text.matches(".*(最后|末尾|倒数第一|尾).*");
    }

    // 解析中文序数词（第X个）
    private static int parseChineseOrdinal(String text) {
        Matcher matcher = Pattern.compile("第([零一二三四五六七八九十]+)[个项条]?").matcher(text);
        if (matcher.find()) {
            String cnNum = matcher.group(1);
            return convertChineseNumber(cnNum);
        }
        return -1;
    }

    // 中文数字转阿拉伯数字（基础版）
    private static int convertChineseNumber(String cnNum) {
        for (int i = 1; i < CN_NUMBERS.length; i++) {
            if (CN_NUMBERS[i].equals(cnNum)) return i;
        }
        // 处理简单组合数字（如"十二"->12）
        if (cnNum.startsWith("十")) {
            if (cnNum.length() == 1) return 10;
            String part = cnNum.substring(1);
            return 10 + convertChineseNumber(part);
        }
        return -1;
    }

    // 解析纯数字（如"3"或"选项2"）
    private static int parseNumeric(String text) {
        Matcher matcher = Pattern.compile("(?:选项|选|个)?(\\d+)").matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    // 解析英文关键词（需处理大小写）
    private static int parseEnglishKeyword(String text) {
        String[] keywords = {
                "first", "second", "third",
                "fourth", "fifth", "sixth",
                "seventh", "eighth", "ninth", "tenth"
        };
        for (int i = 0; i < keywords.length; i++) {
            if (text.contains(keywords[i])) {
                return i + 1;
            }
        }
        return -1;
    }
}
