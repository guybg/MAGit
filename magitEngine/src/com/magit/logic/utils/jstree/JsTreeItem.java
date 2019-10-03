package com.magit.logic.utils.jstree;

//{ "id" : "ajson1", "parent" : "#", "text" : "Simple root node", "icon" : "jstree-file"},
public class JsTreeItem {
    private String id;
    private String parent;
    private String text;
    private String icon;
    private JsTreeAttributes li_attr;
    public JsTreeItem(String id, String parent, String text, String icon, JsTreeAttributes attr) {
        this.id = id;
        this.parent = parent;
        this.text = text;
        this.icon = icon;
        this.li_attr = attr;
    }
}
