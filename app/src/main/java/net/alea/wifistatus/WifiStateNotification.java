/*
Wifi Status for Android - Check Wifi status in notification bar
Copyright (C) 2015 Vincent Hiribarren

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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;


public class WifiStateNotification extends BroadcastReceiver {

    private static final String TAG = WifiStateNotification.class.getSimpleName();

    private static final int NOTIF_ID = 42;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                Log.d(TAG, "New network state received: " + state);
                switch (state) {
                    case CONNECTED:
                        displayWifiNotification(context);
                        break;
                    case DISCONNECTED:
                        removeWifiNotification(context);
                        break;
                    case CONNECTING:
                    case DISCONNECTING:
                    case SUSPENDED:
                    case UNKNOWN:
                }
                break;
            }
            default:
                Log.w(TAG, "Should not have received this action: " + action);
        }
    }


    public static void refreshWifiNotification(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int state = wifiManager.getWifiState();
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                removeWifiNotification(context);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                displayWifiNotification(context);
                break;
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
            case WifiManager.WIFI_STATE_DISABLING:
                // Nothing to do
        }
    }


    private static void displayWifiNotification(Context context) {
        Log.d(TAG, "Displaying notification");
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.w(TAG, "No wifi connection, leaving");
            return;
        }

        /* NetworkInterface should be use to also take into account IPv6 cases,
         * but requires android.permission.INTERNET and  android.permission.ACCESS_NETWORK_STATE
         * and possibly a greater minSdkVersion, so for now sticking to old method to get
         * the access point IP address. */
        int ip = wifiInfo.getIpAddress();
        String contentTitle = "Wi-Fi: " + wifiInfo.getSSID();
        String contentText = String.format("IPv4: %d.%d.%d.%d",
                (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
        Notification notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String CHANNEL_NAME = "default";
                String CHANNEL_ID = "default";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
                channel.setDescription(CHANNEL_NAME);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
                builder = new Notification.Builder(context, CHANNEL_ID)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(contentText)
                        .setContentTitle(contentTitle);
                notification = builder.build();
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder = new Notification.Builder(context);
                builder.setPriority(Notification.PRIORITY_MIN);
                builder = new Notification.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(contentText)
                        .setContentTitle(contentTitle);
                notification = builder.build();
            }
        }
        // For very old Android before 3.x
        else {
            notification = new Notification();
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.icon = R.mipmap.ic_launcher;
            try {
                // try to call "setLatestEventInfo" if available
                Method deprecatedMethod = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                deprecatedMethod.invoke(notification, context, contentTitle, contentText, pendingIntent);
            } catch (Exception e) {
                Log.w(TAG, "Method not found", e);
            }
            // Note: minimum priority does not exist before API 16
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID, notification);
    }


    public static void removeWifiNotification(Context context) {
        Log.d(TAG, "Removing notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

}
