package com.magit.logic.system.objects;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;

public class ConflictItem {
    private String location;
    private HashMap<String, FileItemInfo> item;
    private String fileName;
    public ConflictItem(String location) {
        this.item = new HashMap<>();
        this.item.put("ours", null);
        this.item.put("theirs", null);
        this.item.put("ancestor", null);
        this.location = location;
    }

    public FileItemInfo getOurs() {
        return item.get("ours");
    }

    public FileItemInfo getTheirs() {
        return item.get("theirs");
    }

    public FileItemInfo getAncestor() {
        return item.get("ancestor");
    }

    public void set(String key, FileItemInfo value) {
        item.put(key, value);
    }

    public String getLocation() {
        return location;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
