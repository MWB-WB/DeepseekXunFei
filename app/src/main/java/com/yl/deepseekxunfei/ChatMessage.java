package com.yl.deepseekxunfei;
public class ChatMessage {
    private String message;
    private boolean isUser;
    private String thinkContent; // 新增思考内容属性


    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public ChatMessage() {
    }
    // 添加 setMessage 方法
    public void setMessage(String message) {
        this.message = message;
    }
    // 添加思考内容的get和set方法
    public String getThinkContent() {
        return thinkContent;
    }

    public void setThinkContent(String thinkContent) {
        this.thinkContent = thinkContent;
    }
}