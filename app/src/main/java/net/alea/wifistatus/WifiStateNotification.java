package net.alea.wifistatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(PendingIntent.getActivity(context,0,activityIntent,0))
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentTitle("Wi-Fi: "+wifiInfo.getSSID());
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID, notification);
    }


    public static void removeWifiNotification(Context context) {
        Log.d(TAG, "Removing notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

}