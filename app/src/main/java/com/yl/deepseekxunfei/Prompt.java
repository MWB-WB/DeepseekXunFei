package com.yl.deepseekxunfei;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

public class Prompt {
    // 定义心情关键字列表
    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList("不错", "好", "棒", "开心", "高兴", "喜欢", "满意");
    private static final List<String> NEGATIVE_KEYWORDS = Arrays.asList("不好", "差", "糟糕", "难过", "沮丧", "讨厌", "不满");

    // 使用 HanLP 进行分词并提取关键字
    public static List<String> extractKeywords(String input) {
        List<Term> terms = HanLP.segment(input); // 使用 HanLP 分词
        return terms.stream()
                .map(term -> term.word) // 提取词
                .filter(word -> word.length() > 1) // 过滤掉单字词
                .distinct() // 去重
                .collect(Collectors.toList());
    }

    // 判断心情
    public static String judgeMood(List<String> keywords) {
        for (String keyword : keywords) {
            if (POSITIVE_KEYWORDS.contains(keyword)) {
                return "用户心情不错,请你回答,并给出相关建议";
            } else if (NEGATIVE_KEYWORDS.contains(keyword)) {
                return "用户心情不好，请你回答,并给出相关建议";
            }
        }
        return "心情中性";
    }
}
