package com.magit.webLogic.users;

import java.util.*;

public class UserManager {

    private final HashMap<String, UserAccount> usersMap;

    public UserManager() {
        usersMap = new HashMap<>();
    }

    public synchronized void addUser(String username, UserAccount account) {
        usersMap.put(username, account);
    }

    public synchronized void removeUser(String username) {
        usersMap.remove(username);
    }

    public synchronized Map<String, UserAccount> getUsers() {
        return Collections.unmodifiableMap(usersMap);
    }

    public boolean isUserExists(String username) {
        return usersMap.containsKey(username);
    }

}