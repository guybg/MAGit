package com.magit.logic.system.interfaces;

import com.magit.logic.system.objects.FileItem;

import java.io.IOException;
import java.util.SortedSet;

public interface WalkCompareAction<T1, T2> {
    T2 delta(SortedSet<FileItem> file1, SortedSet<FileItem> file2, String currentPath) throws IOException;

    T1 actionOnDelta(T1 onWhat, T2 delta) throws IOException;

    T1 returnAction(T1 obj) throws IOException;

    T1 action1(T1 obj, T1 onWhat) throws IOException;

    T1 action2(Object... params) throws IOException;
}
