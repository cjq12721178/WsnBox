package com.weisi.tool.wsnbox.bean.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import com.weisi.tool.wsnbox.R;

/**
 * Created by CJQ on 2017/12/5.
 */

public class Settings {

    public static final String FILE_NAME = "settings";

    private final Context mContext;

    //通讯模块默认设置
    //UDP
    boolean mDefaultUdpEnable;
    String mDefaultBaseStationIp;
    int mDefaultBaseStationPort;
    long mDefaultUdpDataRequestCycle;
    //Serial Port
    boolean mDefaultSerialPortEnable;
    String mDefaultSerialPortName;
    int mDefaultSerialPortBaudRate;
    long mDefaultSerialPortDataRequestCycle;
    //BLE
    boolean mDefaultBleEnable;
    long mDefaultBleScanCycle;
    long mDefaultBleScanDuration;

    //数据处理模块默认设置
    boolean mDefaultSensorDataGatherEnable;
    long mDefaultSensorDataGatherCycle;

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
        return mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private String getString(@StringRes int preferenceKeyRes, String defaultValue) {
        return getSharedPreferences().getString(mContext.getString(preferenceKeyRes), defaultValue);
    }

    private int getInt(@StringRes int preferenceKeyRes, int defaultValue) {
        return getSharedPreferences().getInt(mContext.getString(preferenceKeyRes), defaultValue);
    }

    private long getLong(@StringRes int preferenceKeyRes, long defaultValue) {
        return getSharedPreferences().getLong(mContext.getString(preferenceKeyRes), defaultValue);
    }

    private boolean getBoolean(@StringRes int preferenceKeyRes, boolean defaultValue) {
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

    public long getSensorDataGatherTimeInterval() {
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
        return getBoolean(R.string.preferenec_key_sensor_data_gather_enable, mDefaultSensorDataGatherEnable);
    }
}
