package com.magit.logic.system.interfaces;

public interface DeltaAction {
    <T> T execute(T firstItem, T secondItem);
}
