package com.magit.logic.system.interfaces;

import com.magit.logic.system.objects.FileItem;

import java.io.IOException;

public interface WalkAction<T> {
    T action(FileItem file, Object... params) throws IOException;
}
