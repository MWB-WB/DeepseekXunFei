package com.yl.deepseekxunfei.utlis;

import android.content.Context;

public class ContextHolder {
    private static Context sContext; // 静态全局变量

    // 初始化（通常在 Application 中调用）
    public static void init(Context context) {
        sContext = context.getApplicationContext(); // 使用 Application Context 避免内存泄漏
    }

    // 获取 Context
    public static Context getContext() {
        if (sContext == null) {
            throw new IllegalStateException("ContextHolder 未初始化！");
        }
        return sContext;
    }
}
