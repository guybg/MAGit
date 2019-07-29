package com.magit.logic.system.interfaces;

import com.magit.logic.system.objects.FileItem;

import java.io.IOException;
import java.util.SortedSet;

public interface WalkAction<T> {
    void onWalkAction(FileItem file, Object... params) throws IOException;

    void onAddAction(SortedSet<T> set, SortedSet<FileItem> dirFiles, FileItem fileItem, String filePath);
}
