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

import android.os.Build;


public class Features {

    public static boolean requiresLocationPermission() {
        // In our case, it is of course due to the dynamic request permission for location
        // since Lollipop, but the check is on Oreo since we need this permission to get
        // the WiFi SSID, and it needs location permission since Oreo.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean requiresNotificationChannel() {
        // No notification at all if a channel is not created since Oreo.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean hasNotificationPriority() {
        // Does not exist before Jelly Bean, and deprecated since Oreo.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    public static boolean hasNotificationBuilder() {
        // There is no notification before HoneyComb
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean requiresDeprecatedNotificationBuilder() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasNotificationMultiline() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

}
