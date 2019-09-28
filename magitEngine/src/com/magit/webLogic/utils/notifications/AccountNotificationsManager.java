package com.magit.webLogic.utils.notifications;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class AccountNotificationsManager {
    @Expose(serialize = true) private List<SingleNotification> notifications = new ArrayList<>();
    @Expose(serialize = true) private int lastUpdatedNotificationsVersion = 0;
    @Expose(serialize = true) private int seenNotifications = 0;

    public AccountNotificationsManager() {
    }
    public synchronized List<SingleNotification> getNotifications(Integer fromVersion){
        if(fromVersion < lastUpdatedNotificationsVersion){
            fromVersion = lastUpdatedNotificationsVersion;
        }
        if (fromVersion < 0 || fromVersion > notifications.size()) {
            fromVersion = 0;
        }
        seenNotifications = getNotificationsVersion();
        return notifications.subList(fromVersion,notifications.size());
    }
    public void setLastUpdatedNotificationsVersion(Integer notificationsVersion) {
        this.lastUpdatedNotificationsVersion = notificationsVersion;
        this.seenNotifications = lastUpdatedNotificationsVersion;
    }

    public Integer getLastUpdatedNotificationsVersion() {
        return lastUpdatedNotificationsVersion;
    }

    public Integer getNotificationsVersion(){
        return notifications.size();
    }

    public Integer getUnseenNotificationsAmount(){
        return getNotificationsVersion() - seenNotifications;
    }

    public void updateLastUpdatedNotificationsVersion(){
        lastUpdatedNotificationsVersion = seenNotifications;
    }

    public void addNotification(SingleNotification singleNotification){
        notifications.add(singleNotification);
    }
}
