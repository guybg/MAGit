package com.magit.logic.utils.compare;

import com.magit.logic.system.interfaces.DeltaAction;

import java.util.SortedSet;

public class Delta {
    public static SortedSet<> getNewFiles(SortedSet<T> firstItem, SortedSet<T> secondItem, DeltaAction deltaAction) {
        return deltaAction.execute(firstItem, secondItem);
    }


}
