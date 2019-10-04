package com.magit.logic.utils.jstree;

public class JsTreeAttributes {
    String content;
    String path;

    public JsTreeAttributes(String content,String path) {
        this.content = content;
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }
}
