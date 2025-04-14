package com.yl.deepseekxunfei.model;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private String thinkContent; // 新增思考内容属性
    private boolean isThinkContent = false; //是否是思考内容
    private boolean isNeedShowFoldText = false;
    private boolean isOver = false;
    private boolean isSpeaking = false;


    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public ChatMessage(String message, boolean isUser, String thinkContent, boolean isThinkContent) {
        this.message = message;
        this.isUser = isUser;
        this.thinkContent = thinkContent;
        this.isThinkContent = isThinkContent;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void setSpeaking(boolean speaking) {
        isSpeaking = speaking;
    }

    public boolean isNeedShowFoldText() {
        return isNeedShowFoldText;
    }

    public void setNeedShowFoldText(boolean needShowFoldText) {
        isNeedShowFoldText = needShowFoldText;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public boolean isThinkContent() {
        return isThinkContent;
    }

    public void setThinkContent(boolean thinkContent) {
        isThinkContent = thinkContent;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public ChatMessage() {
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
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