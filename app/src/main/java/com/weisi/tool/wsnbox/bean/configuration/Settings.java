package com.weisi.tool.wsnbox.bean.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import com.weisi.tool.wsnbox.R;

import java.util.regex.Pattern;

/**
 * Created by CJQ on 2017/12/5.
 */

public class Settings {

    public static final String PREFERENCE_FILE_NAME = "settings";

    private static final long MIN_DATA_REQUEST_CYCLE = 100; /* 单位毫秒 */
    private static final long MIN_BLE_SCAN_DURATION = 10;   /* 单位秒 */

    private final Context mContext;

    //通讯模块默认设置
    //UDP
    boolean mDefaultUdpEnable;
    private String mDefaultBaseStationIp;
    private int mDefaultBaseStationPort;
    private long mDefaultUdpDataRequestCycle;   /* 单位毫秒 */
    //Serial Port
    boolean mDefaultSerialPortEnable;
    String mDefaultSerialPortName;
    int mDefaultSerialPortBaudRate;
    private long mDefaultSerialPortDataRequestCycle;    /* 单位毫秒 */
    //BLE
    boolean mDefaultBleEnable;
    private long mDefaultBleScanCycle;  /* 单位秒 */
    private long mDefaultBleScanDuration;   /* 单位秒 */

    //数据处理模块默认设置
    boolean mDefaultSensorDataGatherEnable;
    private long mDefaultSensorDataGatherCycle; /* 单位秒 */

    public Settings(Context context) {
        mContext = context;
    }

    public String getDefaultBaseStationIp() {
        return mDefaultBaseStationIp;
    }

    public int getDefaultBaseStationPort() {
        return mDefaultBaseStationPort;
    }

    public long getDefaultUdpDataRequestCycle() {
        return mDefaultUdpDataRequestCycle;
    }

    public long getDefaultSerialPortDataRequestCycle() {
        return mDefaultSerialPortDataRequestCycle;
    }

    public long getDefaultBleScanCycle() {
        return mDefaultBleScanCycle;
    }

    public long getDefaultBleScanDuration() {
        return mDefaultBleScanDuration;
    }

    public long getDefaultSensorDataGatherCycle() {
        return mDefaultSensorDataGatherCycle;
    }

    public boolean isDefaultUdpEnable() {
        return mDefaultUdpEnable;
    }

    public boolean isDefaultSerialPortEnable() {
        return mDefaultSerialPortEnable;
    }

    public String getDefaultSerialPortName() {
        return mDefaultSerialPortName;
    }

    public int getDefaultSerialPortBaudRate() {
        return mDefaultSerialPortBaudRate;
    }

    public boolean isDefaultBleEnable() {
        return mDefaultBleEnable;
    }

    public boolean isDefaultSensorDataGatherEnable() {
        return mDefaultSensorDataGatherEnable;
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }

    private String getString(@StringRes int preferenceKeyRes, String defaultValue) {
        return getSharedPreferences().getString(mContext.getString(preferenceKeyRes), defaultValue);
    }

    private int getInt(@StringRes int preferenceKeyRes, int defaultValue) {
        try {
            return Integer.parseInt(getString(preferenceKeyRes, ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        //return getSharedPreferences().getInt(mContext.getString(preferenceKeyRes), defaultValue);
    }

    private long getLong(@StringRes int preferenceKeyRes, long defaultValue) {
        try {
            return Long.parseLong(getString(preferenceKeyRes, ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        //return getSharedPreferences().getLong(mContext.getString(preferenceKeyRes), defaultValue);
    }

    private boolean getBoolean(@StringRes int preferenceKeyRes, boolean defaultValue) {
//        try {
//            return Boolean.parseBoolean(getString(preferenceKeyRes, ""));
//        } catch (NumberFormatException e) {
//            return defaultValue;
//        }
        return getSharedPreferences().getBoolean(mContext.getString(preferenceKeyRes), defaultValue);
    }

    public String getBaseStationIp() {
        return getString(R.string.preference_key_base_station_ip, mDefaultBaseStationIp);
    }

    public int getBaseStationPort() {
        return getInt(R.string.preference_key_base_station_port, mDefaultBaseStationPort);
    }

    public long getUdpDataRequestCycle() {
        return getLong(R.string.preference_key_udp_data_request_cycle, mDefaultUdpDataRequestCycle);
    }

    public long getSerialPortDataRequestCycle() {
        return getLong(R.string.preference_key_serial_port_data_request_cycle, mDefaultSerialPortDataRequestCycle);
    }

    public long getBleScanCycle() {
        return getLong(R.string.preference_key_ble_scan_cycle, mDefaultBleScanCycle);
    }

    public long getBleScanDuration() {
        return getLong(R.string.preference_key_ble_scan_duration, mDefaultBleScanDuration);
    }

    public long getSensorDataGatherCycle() {
        return getLong(R.string.preference_key_sensor_data_gather_cycle, mDefaultSensorDataGatherCycle);
    }

    public boolean isUdpEnable() {
        return getBoolean(R.string.preference_key_udp_enable, mDefaultUdpEnable);
    }

    public boolean isSerialPortEnable() {
        return getBoolean(R.string.preference_key_serial_port_enable, mDefaultSerialPortEnable);
    }

    public String getSerialPortName() {
        return getString(R.string.preference_key_serial_port_name, mDefaultSerialPortName);
    }

    public int getSerialPortBaudRate() {
        return getInt(R.string.preference_key_serial_port_baud_rate, mDefaultSerialPortBaudRate);
    }

    public boolean isBleEnable() {
        return getBoolean(R.string.preference_key_ble_enable, mDefaultBleEnable);
    }

    public boolean isSensorDataGatherEnable() {
        return getBoolean(R.string.preference_key_sensor_data_gather_enable, mDefaultSensorDataGatherEnable);
    }

    void setDefaultBaseStationIp(String ip) {
        checkIp(ip);
        mDefaultBaseStationIp = ip;
    }

    public void checkIp(String ip) {
        if (!Pattern.matches("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d?)(.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]?\\d?)){3}$",
                ip)) {
            throw new IllegalArgumentException("ip format error");
        }
    }

    void setDefaultBaseStationPort(int port) {
        checkPort(port);
        mDefaultBaseStationPort = port;
    }

    public void checkPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port out of bounds");
        }
    }

    void setDefaultUdpDataRequestCycle(long cycle) {
        checkDataRequestCycle(cycle);
        mDefaultUdpDataRequestCycle = cycle;
    }

    public void checkDataRequestCycle(long cycle) {
        if (cycle < MIN_DATA_REQUEST_CYCLE) {
            throw new IllegalArgumentException("data request cycle may greater 100 milliseconds");
        }
    }

    void setDefaultSerialPortDataRequestCycle(long cycle) {
        checkDataRequestCycle(cycle);
        mDefaultSerialPortDataRequestCycle = cycle;
    }

    void setDefaultBleScanCycle(long cycle) {
        checkBleScanCycle(cycle);
        mDefaultBleScanCycle = cycle;
    }

    public void checkBleScanCycle(long cycle) {
        if (cycle < 0) {
            throw new IllegalArgumentException("ble scan cycle may not be less than 0");
        }
    }

    void setDefaultBleScanDuration(long duration) {
        checkBleScanDuration(duration);
        mDefaultBleScanDuration = duration;
    }

    public void checkBleScanDuration(long duration) {
        if (duration < MIN_BLE_SCAN_DURATION) {
            throw new IllegalArgumentException("ble min scan duration is 10 seconds");
        }
    }

    void setDefaultSensorDataGatherCycle(long cycle) {
        checkSensorDataGatherCycle(cycle);
        mDefaultSensorDataGatherCycle = cycle;
    }

    public void checkSensorDataGatherCycle(long cycle) {
        if (cycle < 0) {
            throw new IllegalArgumentException("sensor data gather cycle may not be less than 0");
        }
    }

    //修复SwitchPreference自带初始值的问题
    void clearRedundantRecordInFirstRun() {
        getSharedPreferences().edit().clear().commit();
    }
}
