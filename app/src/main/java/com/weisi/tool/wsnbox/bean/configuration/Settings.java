package com.weisi.tool.wsnbox.bean.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.weisi.tool.wsnbox.R;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

/**
 * Created by CJQ on 2017/12/5.
 */

public class Settings {

    //public static final String PREFERENCE_FILE_NAME = "settings";

    private static final long MIN_DATA_REQUEST_CYCLE = 100; /* 单位毫秒 */
    private static final long MIN_BLE_SCAN_DURATION = 10;   /* 单位秒 */

    private final Context mContext;

    //通讯模块默认设置
    //UDP
    boolean mDefaultUdpEnable;
    private String mDefaultBaseStationIp;
    private int mDefaultBaseStationPort;
    private long mDefaultUdpDataRequestCycle;   /* 单位毫秒 */

    //TCP
    boolean mDefaultTcpEnable;
    private String mDefaultRemoteServerIp;
    private int mDefaultRemoteServerPort;
    private long mDefaultTcpDataRequestCycle;

    //Serial Port
    boolean mDefaultSerialPortEnable;
    String mDefaultSerialPortName;
    int mDefaultSerialPortBaudRate;
    private long mDefaultSerialPortDataRequestCycle;    /* 单位毫秒 */
    //BLE
    boolean mDefaultBleEnable;
    private long mDefaultBleScanCycle;  /* 单位秒 */
    private long mDefaultBleScanDuration;   /* 单位秒 */

    //USB
    boolean mDefaultUsbEnable;
    long mDefaultUsbVendorProductId;   /* 0-31位为ProductId， 32-63位为VendorId */
    int mDefaultUsbBaudRate;
    int mDefaultUsbDataBits;
    int mDefaultUsbStopBits;
    int mDefaultUsbParity;
    private long mDefaultUsbDataRequestCycle;   /* 单位毫秒 */
    String mDefaultUsbProtocol;

    //数据处理模块默认设置
    boolean mDefaultSensorDataGatherEnable;
    private long mDefaultSensorDataGatherCycle; /* 单位秒 */

    public Settings(Context context) {
        mContext = context.getApplicationContext();
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

    public boolean isDefaultTcpEnable() {
        return mDefaultTcpEnable;
    }

    public String getDefaultRemoteServerIp() {
        return mDefaultRemoteServerIp;
    }

    public int getDefaultRemoteServerPort() {
        return mDefaultRemoteServerPort;
    }

    public long getDefaultTcpDataRequestCycle() {
        return mDefaultTcpDataRequestCycle;
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

    public boolean isDefaultUsbEnable() {
        return mDefaultUsbEnable;
    }

    public long getDefaultUsbVendorProductId() {
        return mDefaultUsbVendorProductId;
    }

    public int getDefaultUsbVendorId() {
        return getUsbVendorIdByUsbVendorProductId(getDefaultUsbVendorProductId());
    }

    public int getUsbVendorIdByUsbVendorProductId(long vendorProductId) {
        return (int) (vendorProductId >> 32);
    }

    public int getDefaultUsbProductId() {
        return getUsbProductIdByUsbVendorProductId(getDefaultUsbVendorProductId());
    }

    public int getUsbProductIdByUsbVendorProductId(long vendorProductId) {
        return (int) (vendorProductId & 0xffffffff);
    }

    public int getDefaultUsbBaudRate() {
        return mDefaultUsbBaudRate;
    }

    public int getDefaultUsbDataBits() {
        return mDefaultUsbDataBits;
    }

    public int getDefaultUsbStopBits() {
        return mDefaultUsbStopBits;
    }

    public int getDefaultUsbParity() {
        return mDefaultUsbParity;
    }

    public long getDefaultUsbDataRequestCycle() {
        return mDefaultUsbDataRequestCycle;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
        //return mContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
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
        return getString(R.string.preference_key_base_station_ip, getDefaultBaseStationIp());
    }

    public int getBaseStationPort() {
        return getInt(R.string.preference_key_base_station_port, getDefaultBaseStationPort());
    }

    public long getUdpDataRequestCycle() {
        return getLong(R.string.preference_key_udp_data_request_cycle, getDefaultUdpDataRequestCycle());
    }

    public String getRemoteServerIp() {
        return getString(R.string.preference_key_remote_server_ip, getDefaultRemoteServerIp());
    }

    public int getRemoteServerPort() {
        return getInt(R.string.preference_key_remote_server_port, getDefaultRemoteServerPort());
    }

    public long getTcpDataRequestCycle() {
        return getLong(R.string.preference_key_tcp_data_request_cycle, getDefaultTcpDataRequestCycle());
    }

    public long getSerialPortDataRequestCycle() {
        return getLong(R.string.preference_key_serial_port_data_request_cycle, getDefaultSerialPortDataRequestCycle());
    }

    public long getBleScanCycle() {
        return getLong(R.string.preference_key_ble_scan_cycle, getDefaultBleScanCycle());
    }

    public long getBleScanDuration() {
        return getLong(R.string.preference_key_ble_scan_duration, getDefaultBleScanDuration());
    }

    public long getSensorDataGatherCycle() {
        return getLong(R.string.preference_key_sensor_data_gather_cycle, getDefaultSensorDataGatherCycle());
    }

    public boolean isUdpEnable() {
        return getBoolean(R.string.preference_key_udp_enable, isDefaultUdpEnable());
    }

    public boolean isTcpEnable() {
        return getBoolean(R.string.preference_key_tcp_enable, isDefaultTcpEnable());
    }

    public boolean isSerialPortEnable() {
        return getBoolean(R.string.preference_key_serial_port_enable, isDefaultSerialPortEnable());
    }

    public String getSerialPortName() {
        return getString(R.string.preference_key_serial_port_name, getDefaultSerialPortName());
    }

    public int getSerialPortBaudRate() {
        return getInt(R.string.preference_key_serial_port_baud_rate, getDefaultSerialPortBaudRate());
    }

    public boolean isBleEnable() {
        return getBoolean(R.string.preference_key_ble_enable, isDefaultBleEnable());
    }

    public boolean isSensorDataGatherEnable() {
        return getBoolean(R.string.preference_key_sensor_data_gather_enable, isDefaultSensorDataGatherEnable());
    }

    void setDefaultBaseStationIp(String ip) {
        checkIp(ip);
        mDefaultBaseStationIp = ip;
    }

    void setDefaultRemoteServerIp(String ip) {
        checkIp(ip);
        mDefaultRemoteServerIp = ip;
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

    void setDefaultRemoteServerPort(int port) {
        checkPort(port);
        mDefaultRemoteServerPort = port;
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

    void setDefaultTcpDataRequestCycle(long cycle) {
        checkDataRequestCycle(cycle);
        mDefaultTcpDataRequestCycle = cycle;
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

    public boolean isUsbEnable() {
        return getBoolean(R.string.preference_key_usb_enable, isDefaultUsbEnable());
    }

    public long getUsbVendorProductId() {
        return getLong(R.string.preference_key_usb_vendor_product_id, getDefaultUsbVendorProductId());
    }

    public int getUsbVendorId() {
        return getUsbVendorIdByUsbVendorProductId(getUsbVendorProductId());
    }

    public int getUsbProductId() {
        return getUsbProductIdByUsbVendorProductId(getUsbVendorProductId());
    }

    public int getUsbBaudRate() {
        return getInt(R.string.preference_key_usb_baud_rate, getDefaultUsbBaudRate());
    }

    public int getUsbDataBits() {
        return getInt(R.string.preference_key_usb_data_bits, getDefaultUsbDataBits());
    }

    public int getUsbStopBits() {
        return getInt(R.string.preference_key_usb_stop_bits, getDefaultUsbStopBits());
    }

    public int getUsbParity() {
        return getInt(R.string.preference_key_usb_parity, getDefaultUsbParity());
    }

    public long getUsbUsbDataRequestCycle() {
        return getLong(R.string.preference_key_usb_data_request_cycle, getDefaultUsbDataRequestCycle());
    }

    void setDefaultUsbDataRequestCycle(long cycle) {
        checkDataRequestCycle(cycle);
        mDefaultUsbDataRequestCycle = cycle;
    }

    public String getDefaultUsbProtocol() {
        return mDefaultUsbProtocol;
    }

    public String getUsbProtocol() {
        return getString(R.string.preference_key_usb_protocol, getDefaultUsbProtocol());
    }

    //修复SwitchPreference自带初始值的问题
    void clearRedundantRecordInFirstRun() {
        getSharedPreferences().edit().clear().commit();
    }

    public long getDataBrowseValueContainerConfigurationProviderId() {
        return getSharedPreferences().getLong(mContext.getString(R.string.preference_key_data_browse_config_provider_id), 0);
    }

    public void setDataBrowseValueContainerConfigurationProviderId(long id) {
        getSharedPreferences()
                .edit()
                .putLong(mContext.getString(R.string.preference_key_data_browse_config_provider_id), id)
                .commit();
    }

    public String getOutputFilePath() {
        return getString(R.string.preference_key_output_file_path, Environment.getExternalStorageDirectory() + File.separator + "WsnBox");
    }

    @IntDef({VM_PHYSICAL_SENSOR, VM_LOGICAL_SENSOR, VM_DEVICE_NODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewMode{}
    public static final int VM_PHYSICAL_SENSOR = 1;
    public static final int VM_LOGICAL_SENSOR = 2;
    public static final int VM_DEVICE_NODE = 3;

    //数据浏览设置
    public @ViewMode int getLastDataBrowseViewMode() {
        return getSharedPreferences().getInt("view_mode", VM_PHYSICAL_SENSOR);
    }

    public void setLastDataBrowseViewMode(@ViewMode int mode) {
        getSharedPreferences()
                .edit()
                .putInt("view_mode", mode)
                .commit();
    }

    public void clearLastDataBrowseViewMode() {
        getSharedPreferences().edit().remove("view_mode").commit();
    }

    public void checkLatestVersionInfo() {

    }
}
