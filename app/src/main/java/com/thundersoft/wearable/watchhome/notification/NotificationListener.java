package com.thundersoft.wearable.watchhome.notification;

import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.thundersoft.wearable.watchhome.notification.bean.Channel;
import com.thundersoft.wearable.watchhome.notification.presenter.ConfigManager;
import com.thundersoft.wearable.watchhome.notification.presenter.NotificationRecordManager;

import java.lang.reflect.Method;
import java.util.List;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";

    @Override
    public void onCreate() {
        super.onCreate();
        registerNotificationListener();
        //update config
        updateConfig();
    }

    private void updateConfig() {
        List<PackageInfo> infoList = getPackageManager()
                .getInstalledPackages(PackageManager.GET_ACTIVITIES);
        ConfigManager.getInstance().updateConfig(infoList);
    }

    /**
     * register notification listener.
     */
    private void registerNotificationListener() {
        Log.d(TAG, "registerNotificationListener");
        try {
            ComponentName componentName = new ComponentName(this, NotificationListener.class);
            String methodName = "registerAsSystemService";
            Method method = this.getClass().getMethod(methodName, Context.class, ComponentName.class, int.class);
            method.invoke(this, this, componentName, -1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationPosted id=" + sbn.getId()
                + " packageName=" + sbn.getPackageName() + " tag=" + sbn.getKey());
        if (Integer.MAX_VALUE == sbn.getId()) {
            return;
        }
        NotificationRecordManager.getInstance().recordNotifications(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved id=" + sbn.getId()
                + " packageName=" + sbn.getPackageName() + " tag=" + sbn.getTag());
        NotificationRecordManager.getInstance().deleteNotifications(sbn);
    }

    @Override
    public void onNotificationChannelModified(String pkg, UserHandle user,
                                              NotificationChannel channel, int modificationType) {
        super.onNotificationChannelModified(pkg, user, channel, modificationType);
        Log.d(TAG, "onNotificationChannelModified");
        //update config
        ConfigManager.getInstance().updateChannel(new Channel(pkg, channel));
    }

    @Override
    public void onNotificationRankingUpdate(RankingMap rankingMap) {
        super.onNotificationRankingUpdate(rankingMap);
        Log.d(TAG, "onNotificationRankingUpdate");
        NotificationRecordManager.getInstance().rankingUpdate(rankingMap);
    }

}
