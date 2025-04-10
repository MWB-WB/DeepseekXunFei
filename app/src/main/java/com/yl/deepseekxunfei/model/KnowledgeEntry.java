package com.yl.deepseekxunfei.model;

public class KnowledgeEntry {
    private String title;
    private String content;

    public KnowledgeEntry(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}