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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Objects;


public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = WifiStateReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Objects.requireNonNull(action);
        switch (action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                onNetworkStateChangedAction(context, intent);
                break;
            }
            default:
                Log.w(TAG, "Should not have received this action: " + action);
        }
    }


    private void onNetworkStateChangedAction(Context context, Intent intent) {
        final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        final NetworkInfo.State state = networkInfo.getState();
        Log.d(TAG, "New network state received: " + state);
        switch (state) {
            case CONNECTED: {
                new WifiNotification(context).display();
                break;
            }
            case DISCONNECTED: {
                new WifiNotification(context).remove();
                break;
            }
            case CONNECTING:
            case DISCONNECTING:
            case SUSPENDED:
            case UNKNOWN:
        }
    }

}
