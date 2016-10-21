package org.xerrard.wifiapdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


/**
 * Created by xuqiang on 16-10-20.
 */
public class WifiHelper {
    Context mContext;
    WifiManager mWifiManager;
    boolean isWifiSTAEnabled = false;
    private static WifiHelper ourInstance = new WifiHelper();
    private MyReceiver mWifiReceiver = new MyReceiver();
    private IfWifiEventHandler mIfWifiEventHandler;

    public static WifiHelper getInstance() {
        return ourInstance;
    }

    private WifiHelper() {
    }

    public interface IfWifiEventHandler {
        void updateWifiStatus(String str);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
                    || intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                if (WifiUtil.isWifiConnected(context)) {
                    try {
                        mIfWifiEventHandler.updateWifiStatus(getBroadcastAddressString());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void init(Context context, IfWifiEventHandler ifWifiEventHandler) {
        mContext = context;
        mIfWifiEventHandler = ifWifiEventHandler;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        mContext = context;
        mContext.registerReceiver(mWifiReceiver, intentFilter);
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
        } catch (ClassNotFoundException e) {
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
            InvocationTargetException, ClassNotFoundException {
        if (isWifiApEnabled()) {
            closeAp();
        }
        isWifiSTAEnabled = mWifiManager.isWifiEnabled();
        if (!isWifiSTAEnabled) {
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

    private void connectWifi(WifiConfiguration config, ActionListener listener) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (config != null) {
            Object actionListener = null;
            Class<?> clz = Class.forName("android.net.wifi.WifiManager$ActionListener");

            actionListener = java.lang.reflect.Proxy.newProxyInstance(clz.getClassLoader(), new java.lang
                    .Class[]{clz}, listener == null ? (new ActionListener()) : listener);

            if (actionListener != null) {
                Method connectWifiMethod = mWifiManager.getClass().getDeclaredMethod("connect", WifiConfiguration
                        .class, clz);
                connectWifiMethod.setAccessible(true);
                connectWifiMethod.invoke(mWifiManager, config, listener);
            }

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

    /**
     * 获取广播地址,经过证明，得到的是AP的地址
     *
     * @return
     * @throws UnknownHostException
     */
    public InetAddress getBroadcastAddress() throws UnknownHostException {
        DhcpInfo dhcp = mWifiManager.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public String getBroadcastAddressString() throws UnknownHostException {
        InetAddress address = getBroadcastAddress();
        String str = address.getHostAddress();
        return str;
    }


    public void close() {
        mContext = null;
        mWifiManager = null;
    }


    /**
     * Passed with {@link ActionListener#onFailureCB}.
     * Indicates that the operation failed due to an internal error.
     *
     * @hide
     */
    public static final int ERROR = 0;

    /**
     * Passed with {@link ActionListener#onFailureCB}.
     * Indicates that the operation is already in progress
     *
     * @hide
     */
    public static final int IN_PROGRESS = 1;

    /**
     * Passed with {@link ActionListener#onFailureCB}.
     * Indicates that the operation failed because the framework is busy and
     * unable to service the request
     *
     * @hide
     */
    public static final int BUSY = 2;

    public static class ActionListener implements java.lang.reflect.InvocationHandler {

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws java.lang.Throwable {
            String method_name = method.getName();
            Class<?>[] classes = method.getParameterTypes();

            if (method_name.equals("onSuccess")) {
                onSuccessCB();
            } else if (method_name.equals("onFailure")) {
                if (classes.length == 1) {
                    if (classes[0] == int.class) {
                        onFailureCB(Integer.valueOf(args[0].toString()));
                    }
                }
            }
            return null;
        }

        public void onSuccessCB() {

        }

        /**
         * The operation failed
         *
         * @param reason The reason for failure could be one of
         *               {@link #ERROR}, {@link #IN_PROGRESS} or {@link #BUSY}
         */
        public void onFailureCB(int reason) {

        }

    }


}
