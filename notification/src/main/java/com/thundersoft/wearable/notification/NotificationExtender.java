package com.thundersoft.wearable.notification;

import android.app.Notification;
import android.os.Bundle;

public class NotificationExtender implements Notification.Extender {

    public static final String EXTRA_EXTENSIONS = "thundersoft.wearable.extension";
    public static final String FLAG = "flag_key";
    public static final int FLAG_NOTIFICATION_LIST = 0;
    public static final int FLAG_NOTIFICATION_OVERLAY = 1;
    public static final int FLAG_NOTIFICATION_GLOBAL_STATUS = 2;

    private int mFlag = FLAG_NOTIFICATION_LIST;

    public void setFlag(int flag) {
        this.mFlag = flag;
    }

    @Override
    public Notification.Builder extend(Notification.Builder builder) {
        Bundle wr = new Bundle();
        if (FLAG_NOTIFICATION_LIST != mFlag) {
            wr.putInt(FLAG, mFlag);
        }
        builder.getExtras().putBundle(EXTRA_EXTENSIONS, wr);
        return builder;
    }


}
