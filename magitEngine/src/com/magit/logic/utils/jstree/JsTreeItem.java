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

    public JsTreeItem(String text, String icon,JsTreeAttributes attr) {
        this.text = text;
        this.icon = icon;
        this.li_attr = attr;
    }


    public String getId() {
        return id;
    }

    public String getParent() {
        return parent;
    }

    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }

    public JsTreeAttributes getLi_attr() {
        return li_attr;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setLi_attr(JsTreeAttributes li_attr) {
        this.li_attr = li_attr;
    }
}
