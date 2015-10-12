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

import android.annotation.TargetApi;
import android.app.Notification;
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
        Intent activityIntent = new Intent(context, MainActivity.class);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentIntent(PendingIntent.getActivity(context, 0, activityIntent, 0))
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Wi-Fi: " + wifiInfo.getSSID());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_MIN);
        }
        Notification notification;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.getNotification();
        }
        else {
            notification = builder.build();
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
