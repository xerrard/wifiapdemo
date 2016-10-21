/**
 * WifiUtil    2016-10-21
 * copyright (c) 2016 xerrard Co.ltd. All right reserved.
 */

package org.xerrard.wifiapdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * class descrption here
 *
 * @author xuqiang
 * @version 1.0.0
 * @since 2016-10-21
 */
public class WifiUtil {

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Network net[] = connectivityManager.getAllNetworks();
            int size = net.length;
            for (int i = 0; i < size; i++) {
                NetworkInfo info = connectivityManager.getNetworkInfo(net[i]);
                if (info != null && info.isConnected()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        return true;
                    }
                }
            }
        } else {
            NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifi != null && wifi.isConnected();
        }
        return false;
    }
}