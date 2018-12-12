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
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;


public class WifiUtils {

    public enum SsidStatus {
        available, no_location, unauthorized,
    }

    private static final String TAG = WifiUtils.class.getSimpleName();
    private static final String WIFI_INTERFACE = "wlan0";

    private final WifiManager mWifiManager;
    private final Context mContext;


    public WifiUtils(Context context) {
        mWifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mContext = context;
    }


    public boolean isWifiConnectionActive() {
        final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.w(TAG, "No wifi connection, leaving");
            return false;
        }
        return true;
    }


    public String getSSID() {
        // Needs dynamic coard location permission and GPS enabled otherwise it returns
        // <unknown ssid>
        return mWifiManager.getConnectionInfo().getSSID();
    }


    public SsidStatus ssidStatus() {
        if ( ! Features.requiresLocationPermission() ) {
            return SsidStatus.available;
        }
        if ( ! hasLocationPermission() ) {
            return SsidStatus.unauthorized;
        }
        else if ( ! hasLocationEnabled() ) {
            return SsidStatus.no_location;
        }
        else {
            return SsidStatus.available;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasLocationPermission() {
        return mContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasLocationEnabled() {
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }


    public int getWifiState() {
        return mWifiManager.getWifiState();
    }


    public String[] getWifiAddresses() {
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return new String[]{};
        }
        for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
            if ( networkInterface.getName().equalsIgnoreCase(WIFI_INTERFACE)) {
                LinkedList<String> addresses = new LinkedList<>();
                Enumeration<InetAddress> inetAdresses = networkInterface.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAdresses)) {
                    if (inetAddress instanceof Inet4Address) {
                        addresses.addFirst(inetAddress.getHostAddress());
                    }
                    else {
                        addresses.add(inetAddress.getHostAddress());
                    }
                    Log.d(TAG, inetAddress.getHostAddress());
                }
                return addresses.toArray(new String[0]);
            }

        }
        return new String[]{};
    }



}
