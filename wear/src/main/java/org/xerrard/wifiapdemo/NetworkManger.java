package org.xerrard.wifiapdemo;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


/**
 * Created by xuqiang on 16-10-20.
 */
public class NetworkManger {
    Context mContext;
    WifiManager mWifiManager;
    boolean isWifiSTAEnabled = false;
    private static NetworkManger ourInstance = new NetworkManger();

    public static NetworkManger getInstance() {
        return ourInstance;
    }

    private NetworkManger() {
    }

    public void init(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    }

    public boolean createAccessPoint() {
        boolean result = false;
        String nickName = "wifiapdemo_AP";
        Log.d("WiFi", "nickName:" + nickName);
        String enc = Base64.encodeToString(nickName.getBytes(), Base64.DEFAULT);
        if (!TextUtils.isEmpty(enc)) {
            enc = enc.trim();
        }
        Log.d("WiFi", "startOpenAp");
        try {
            openAp(enc);
            result = true;
        } catch (NoSuchMethodException e) {
            result = false;
        } catch (InvocationTargetException e) {
            result = false;
        } catch (IllegalAccessException e) {
            result = false;
        }
        return result;
    }


    public boolean connectAccessPoint() {
        boolean result = false;
        String nickName = "wifiapdemo_AP";
        Log.d("WiFi", "nickName:" + nickName);
        String enc = Base64.encodeToString(nickName.getBytes(), Base64.DEFAULT);
        if (!TextUtils.isEmpty(enc)) {
            enc = enc.trim();
        }
        try {
            connectOpenWifi(enc);
            result = true;
        } catch (NoSuchMethodException e) {
            result = false;
        } catch (IllegalAccessException e) {
            result = false;
        } catch (InvocationTargetException e) {
            result = false;
        }
        return result;
    }


    private void openAp(String ssid) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        isWifiSTAEnabled = mWifiManager.isWifiEnabled();
        if (isWifiSTAEnabled == true) {
            mWifiManager.setWifiEnabled(false);
        }
        WifiConfiguration myConfig = new WifiConfiguration();
        myConfig.SSID = ssid;
        myConfig.status = WifiConfiguration.Status.ENABLED;
        Method setWifiApEnabledMethod = WifiManager.class.getMethod("setWifiApEnabled", WifiConfiguration.class,
                boolean.class);
        setWifiApEnabledMethod.invoke(mWifiManager, myConfig, true);

    }

    private void closeAp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method setWifiApEnabledMethod = WifiManager.class.getMethod("setWifiApEnabled", WifiConfiguration.class,
                boolean.class);
        setWifiApEnabledMethod.invoke(mWifiManager, null, false);
        if (isWifiSTAEnabled) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    private void connectOpenWifi(String ssid) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        if(isWifiApEnabled()){
            closeAp();
        }
        isWifiSTAEnabled = mWifiManager.isWifiEnabled();
        if(!isWifiSTAEnabled){
            mWifiManager.setWifiEnabled(true);
            isWifiSTAEnabled = true;
        }
        if (WifiUtil.isWifiConnected(mContext)) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            mWifiManager.disconnect();
            if (info != null && info.getNetworkId() != -1) {
                mWifiManager.disableNetwork(info.getNetworkId());
            }
            mWifiManager.saveConfiguration();
        }
        WifiConfiguration configuration = getWifiConfigurationBySsid(mWifiManager, ssid);
        if (configuration != null && configuration.networkId != -1) {
            mWifiManager.enableNetwork(configuration.networkId, true);
            mWifiManager.saveConfiguration();
            mWifiManager.reconnect();
        } else {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = ssid;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.networkId = mWifiManager.addNetwork(config);
            connectWifi(config, null);
        }

    }

    private void connectWifi(WifiConfiguration config, WifiP2pManager.ActionListener listener) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (config != null) {
            Method connectWifiMethod = mWifiManager.getClass().getDeclaredMethod("connect", WifiConfiguration
                    .class, WifiP2pManager.ActionListener.class);
            connectWifiMethod.setAccessible(true);
            connectWifiMethod.invoke(mWifiManager, config, listener);
        }

    }


    private WifiConfiguration getWifiConfigurationBySsid(WifiManager mWifiManager, String ssid) {
        if (TextUtils.isEmpty(ssid))
            return null;
        List<WifiConfiguration> all = mWifiManager.getConfiguredNetworks();
        if (all == null)
            return null;
        for (WifiConfiguration conf : all) {
            if (ssid.equals(conf.SSID)) {
                return conf;
            }
        }
        return null;
    }

    private boolean isWifiApEnabled() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method isWifiApEnabledMethod = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
        boolean result = (boolean) isWifiApEnabledMethod.invoke(mWifiManager);
        return result;
    }


    public void close() {
        mContext = null;
        mWifiManager = null;
    }

}
