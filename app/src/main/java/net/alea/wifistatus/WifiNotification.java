/*
Wifi Status for Android - Check Wifi status in notification bar
Copyright (C) 2018 Vincent Hiribarren

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.alea.wifistatus;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class WifiNotification {

    private static final String TAG = WifiNotification.class.getSimpleName();
    private static final int NOTIF_ID = 42;

    private final Context mContext;
    private final WifiUtils mWifiUtils;


    public WifiNotification(Context context) {
        mWifiUtils = new WifiUtils(context);
        mContext = context;
    }


    public void refresh() {
        final int state = mWifiUtils.getWifiState();
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                remove();
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                display();
                break;
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
            case WifiManager.WIFI_STATE_DISABLING:
                // Nothing to do
        }
    }


    public void remove() {
        Log.d(TAG, "Removing notification");
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }



    public void display() {
        if (!mWifiUtils.isWifiConnectionActive()) {
            return;
        }
        Log.d(TAG, "Displaying notification");

        /* NetworkInterface should be use to also take into account IPv6 cases,
         * but requires android.permission.INTERNET and  android.permission.ACCESS_NETWORK_STATE
         * and possibly a greater minSdkVersion, so for now sticking to old method to get
         * the access point IP address. */

        String contentTitle = "SSID: ";
        switch (mWifiUtils.ssidStatus()) {
            case unauthorized:
                contentTitle += "<unauthorized>";
                break;
            case no_location:
                contentTitle += "<location off>";
                break;
            default:
                contentTitle += mWifiUtils.getSSID();
        }
        String contentText = TextUtils.join("\n", mWifiUtils.getWifiAddresses());
        Intent activityIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, activityIntent, 0);
        Notification notification = null;

        if (Features.hasNotificationBuilder()) {
            notification = generateNewStyleNotification(contentTitle, contentText, pendingIntent);
        }
        // For very old Android before 3.x
        else {
            notification = generateOldStyleNotification(contentTitle, contentText, pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID, notification);
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Notification generateNewStyleNotification(String contentTitle, String contentText, PendingIntent pendingIntent) {
        Notification notification = null;
        Notification.Builder builder;
        if (Features.requiresNotificationChannel()) {
            String CHANNEL_NAME = "default";
            String CHANNEL_ID = "default";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            channel.setDescription(CHANNEL_NAME);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(mContext, CHANNEL_ID);
        }
        else {
            builder = new Notification.Builder(mContext);
        }
        builder.setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(contentText)
                .setContentTitle(contentTitle);
        if (Features.hasNotificationPriority()) {
            builder.setPriority(Notification.PRIORITY_MIN);
        }
        if (Features.requiresDeprecatedNotificationBuilder()) {
            notification = builder.getNotification();
        }
        else {
            notification = builder.build();
        }
        return notification;
    }


    private Notification generateOldStyleNotification(String contentTitle, String contentText, PendingIntent pendingIntent) {
        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.icon = R.mipmap.ic_launcher;
        try {
            // try to call "setLatestEventInfo" if available
            Method deprecatedMethod = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            deprecatedMethod.invoke(notification, mContext, contentTitle, contentText, pendingIntent);
        } catch (Exception e) {
            Log.w(TAG, "Method not found", e);
        }
        return notification;
    }

}
