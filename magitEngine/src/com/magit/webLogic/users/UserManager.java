package com.magit.webLogic.users;

import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.system.MagitEngine;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UserManager {

    private final HashMap<String, UserAccount> usersMap;
    public UserManager() {
        usersMap = new HashMap<>();
    }

    public synchronized void addUser(String username, UserAccount account) throws InvalidNameException {
        new MagitEngine().updateUserName(username);
        usersMap.put(username.toLowerCase(), account);
    }

    public synchronized void removeUser(String username) {
        usersMap.remove(username);
    }

    public synchronized Map<String, UserAccount> getUsers() {
        return Collections.unmodifiableMap(usersMap);
    }
    public synchronized Set<String> getOnlineUsersAsList() {

        return Collections.unmodifiableSet(usersMap.entrySet().stream().filter(a->a.getValue().isOnline()).map(Map.Entry::getKey).collect(Collectors.toSet()));
    }

    public boolean isUserExists(String username) {
        return usersMap.containsKey(username);
    }


}