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

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Needed to displays text as HTML with clickable links
        final TextView authorText = (TextView)findViewById(R.id.author_label);
        authorText.setText(Html.fromHtml(getResources().getString(R.string.app_author)));
        authorText.setMovementMethod(LinkMovementMethod.getInstance());

        final ToggleButton toggleButton = (ToggleButton)findViewById(R.id.enableSwitch);
        final ComponentName receiver = new ComponentName(this, WifiStateNotification.class);
        final PackageManager pm = this.getPackageManager();

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "Change of button state: " + isChecked);
                if (isChecked) {
                    pm.setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                    WifiStateNotification.refreshWifiNotification(MainActivity.this);
                }
                else {
                    pm.setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    WifiStateNotification.removeWifiNotification(MainActivity.this);
                }
            }
        });

        int componentState = pm.getComponentEnabledSetting(receiver);
        switch (componentState) {
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                WifiStateNotification.refreshWifiNotification(this);
                toggleButton.setChecked(true);
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                toggleButton.setChecked(false);
                break;
            default:
        }
    }

}
