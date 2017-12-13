package com.weisi.tool.wsnbox.fragment;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.TextUtils;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.cjq.tool.qbox.util.SerialPortFinder;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.activity.SettingsActivity;
import com.weisi.tool.wsnbox.bean.configuration.Settings;

/**
 * Created by CJQ on 2017/12/8.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private Settings mSettings;
    //private EditTextPreference mEtPrefIp;
    //private EditTextPreference mEtPrefPort;
    //private EditTextPreference mEtPrefUdpDataRequest;
    //private EditTextPreference mEtPrefBleScanCycle;
    //private EditTextPreference mEtPrefBleScanDuration;
//    private ListPreference mListPrefSerialPortName;
//    private ListPreference mListPrefBaudRate;
//    private EditTextPreference mEtPrefSerialPortDataRequest;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Settings.PREFERENCE_FILE_NAME);
        addPreferencesFromResource(R.xml.settings);

        mSettings = getSettingsActivity().getBaseApplication().getSettings();

        //初始化UDP通讯模块设置
        mIpPrefHelper.initialize(R.string.preference_key_base_station_ip);
        mPortPrefHelper.initialize(R.string.preference_key_base_station_port);
        mUdpDataRequestPrefHelper.initialize(R.string.preference_key_udp_data_request_cycle);
        mUdpEnablePrefHelper.initialize(R.string.preference_key_udp_enable);

        //初始化BLE通讯模块设置
        mBleScanCyclePrefHelper.initialize(R.string.preference_key_ble_scan_cycle);
        mBleScanDurationPrefHelper.initialize(R.string.preference_key_ble_scan_duration);
        mBleEnablePrefHelper.initialize(R.string.preference_key_ble_enable);

        //初始化串口通讯模块设置
        mSerialPortNamePrefHelper.initialize(R.string.preference_key_serial_port_name);
        mSerialPortBaudRatePrefHelper.initialize(R.string.preference_key_serial_port_baud_rate);
        mSerialPortDataRequestPrefHelper.initialize(R.string.preference_key_serial_port_data_request_cycle);
        mSerialPortEnablePrefHelper.initialize(R.string.preference_key_serial_port_enable);

        //初始化传感器数据保存设置
        mSensorDataGatherCyclePrefHelper.initialize(R.string.preference_key_sensor_data_gather_cycle);
        mSensorDataGatherEnablePrefHelper.initialize(R.string.preference_key_sensor_data_gather_enable);

//        initializePreferences(R.string.preference_key_udp_enable,
//                mSettings.isDefaultUdpEnable(),
//                mUdpEnablePrefHelper);
//        mEtPrefIp = (EditTextPreference) initializePreferences(
//                R.string.preference_key_base_station_ip,
//                mSettings.getDefaultBaseStationIp(),
//                mIpPrefHelper);
//        mEtPrefPort = (EditTextPreference) initializePreferences(
//                R.string.preference_key_base_station_port,
//                mSettings.getDefaultBaseStationPort(),
//                mPortPrefHelper);
//        mEtPrefUdpDataRequest = (EditTextPreference) initializePreferences(
//                R.string.preference_key_udp_data_request_cycle,
//                mSettings.getDefaultUdpDataRequestCycle(),
//                mUdpDataRequestPrefHelper);

//        initializePreferences(R.string.preference_key_ble_enable,
//                mSettings.isDefaultBleEnable(),
//                mBleEnablePrefHelper);
//        mEtPrefBleScanCycle = (EditTextPreference) initializePreferences(
//                R.string.preference_key_ble_scan_cycle,
//                mSettings.getDefaultBleScanCycle(),
//                mBleScanCyclePrefHelper);
//        mEtPrefBleScanDuration = (EditTextPreference) initializePreferences(
//                R.string.preference_key_ble_scan_duration,
//                mSettings.getDefaultBleScanDuration(),
//                mBleScanDurationPrefHelper);
//
//        initializePreferences(R.string.preference_key_serial_port_enable,
//                mSettings.isDefaultSerialPortEnable(),
//                mSerialPortEnablePrefHelper);

    }

//    private Preference initializePreferences(@StringRes int preferenceKeyRes,
//                                       Object defaultValue,
//                                       Preference.OnPreferenceChangeListener listener) {
//        Preference preference = findPreference(getString(preferenceKeyRes));
//        if (preference != null && listener != null) {
//            preference.setOnPreferenceChangeListener(listener);
//            if (preference instanceof EditTextPreference) {
//                preference.setSummary(preference.getSharedPreferences().getString(preference.getKey(), defaultValue.toString()));
//            } else if (preference instanceof ListPreference) {
//                ListPreference listPreference = (ListPreference) preference;
//                String value = preference.getSharedPreferences().getString(preference.getKey(), defaultValue.toString());
//                switch (preferenceKeyRes) {
//                    case R.string.preference_key_serial_port_name:
//                        SerialPortFinder finder = new SerialPortFinder();
//                        listPreference.setEntries(finder.getAllDevices());
//                        String[] values = finder.getAllDevicesPath();
//                        listPreference.setEntryValues(values);
//                        if (Arrays.asList(values).contains(value)) {
//                            listPreference.setSummary(value);
//                        }
//                        break;
//                    case R.string.preference_key_serial_port_baud_rate:
//                        listPreference.setSummary(value);
//                        break;
//                }
//            }
//        }
//        return preference;
//    }

    private SwitchPreferenceHelper mUdpEnablePrefHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mIpPrefHelper.getPreference().setEnabled(checked);
            mPortPrefHelper.getPreference().setEnabled(checked);
            mUdpDataRequestPrefHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreferenceCompat preference) {
            setChecked(mSettings.isUdpEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                if (getSettingsActivity().getDataPrepareService().launchUdp(mSettings)) {
                    SimpleCustomizeToast.show(getContext(), getString(R.string.udp_launch_succeed));
                } else {
                    SimpleCustomizeToast.show(getContext(), getString(R.string.udp_launch_failed));
                    return false;
                }
            } else {
                getSettingsActivity().getDataPrepareService().shutdownUdp();
                SimpleCustomizeToast.show(getContext(), getString(R.string.udp_shutdown));
            }
            return true;
        }
    };

    private EditPreferenceHelper mIpPrefHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultBaseStationIp();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                String newIp = (String) newValue;
                mSettings.checkIp(newIp);
                if (!getSettingsActivity().getDataPrepareService().setUdpBaseStationIp(newIp)) {
                    SimpleCustomizeToast.show(getContext(), R.string.set_base_station_ip_failed);
                }
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.ip_format_error);
            }
            return false;
        }
    };

    private EditPreferenceHelper mPortPrefHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultBaseStationPort();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                int newPort = Integer.parseInt((String) newValue);
                mSettings.checkPort(newPort);
                getSettingsActivity().getDataPrepareService().setUdpBaseStationPort(newPort);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.port_out_of_bounds);
            }
            return false;
        }
    };

    private EditPreferenceHelper mUdpDataRequestPrefHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultUdpDataRequestCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                mSettings.checkDataRequestCycle(newCycle);
                getSettingsActivity().getDataPrepareService().setUdpDataRequest(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.data_request_less_than_min_cycle);
            }
            return false;
        }
    };

    private SwitchPreferenceHelper mBleEnablePrefHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mBleScanCyclePrefHelper.getPreference().setEnabled(checked);
            mBleScanDurationPrefHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreferenceCompat preference) {
            setChecked(mSettings.isBleEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                if (getSettingsActivity().getDataPrepareService().launchBle(mSettings)) {
                    SimpleCustomizeToast.show(getContext(), R.string.ble_launch_succeed);
                } else {
                    SimpleCustomizeToast.show(getContext(), R.string.ble_launch_failed);
                    return false;
                }
            } else {
                getSettingsActivity().getDataPrepareService().shutdownBle();
                SimpleCustomizeToast.show(getContext(), getString(R.string.ble_shutdown));
            }
            return true;
        }
    };

    private EditPreferenceHelper mBleScanCyclePrefHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultBleScanCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                mSettings.checkBleScanCycle(newCycle);
                getSettingsActivity().getDataPrepareService().setBleScanCycle(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.ble_scan_cycle_out_of_bounds);
            }
            return false;
        }
    };

    private EditPreferenceHelper mBleScanDurationPrefHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultBleScanDuration();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newDuration = Long.parseLong((String) newValue);
                mSettings.checkBleScanDuration(newDuration);
                getSettingsActivity().getDataPrepareService().setBleScanDuration(newDuration);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.ble_scan_duration_out_of_bounds);
            }
            return false;
        }
    };

    private SwitchPreferenceHelper mSerialPortEnablePrefHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mSerialPortNamePrefHelper.getPreference().setEnabled(checked);
            mSerialPortBaudRatePrefHelper.getPreference().setEnabled(checked);
            mSerialPortDataRequestPrefHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreferenceCompat preference) {
            setChecked(mSettings.isSerialPortEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                if (getSettingsActivity().getDataPrepareService().launchSerialPort(mSettings)) {
                    SimpleCustomizeToast.show(getContext(), R.string.serial_port_launch_succeed);
                } else {
                    SimpleCustomizeToast.show(getContext(), R.string.serial_port_launch_failed);
                    return false;
                }
            } else {
                getSettingsActivity().getDataPrepareService().shutdownSerialPort();
                SimpleCustomizeToast.show(getContext(), getString(R.string.serial_port_shutdown));
            }
            return true;
        }
    };

    private ListPreferenceHelper mSerialPortNamePrefHelper = new ListPreferenceHelper() {

        @Override
        protected void onInitialize(ListPreference preference) {
            super.onInitialize(preference);
            SerialPortFinder finder = new SerialPortFinder();
            preference.setEntries(finder.getAllDevices());
            preference.setEntryValues(finder.getAllDevicesPath());
        }

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultSerialPortName();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            String newName = (String) newValue;
            if (getSettingsActivity().getDataPrepareService().setSerialPortName(newName)) {
                return true;
            } else {
                getSettingsActivity().getDataPrepareService().shutdownSerialPort();
                mSerialPortEnablePrefHelper.setChecked(false);
                SimpleCustomizeToast.show(getContext(), R.string.change_serial_port_name_failed);
            }
            return false;
        }
    };

    private ListPreferenceHelper mSerialPortBaudRatePrefHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultSerialPortBaudRate();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            int newBaudRate = Integer.parseInt((String) newValue);
            if (getSettingsActivity().getDataPrepareService().setSerialPortBaudRate(newBaudRate)) {
                return true;
            } else {
                getSettingsActivity().getDataPrepareService().shutdownSerialPort();
                mSerialPortEnablePrefHelper.setChecked(false);
                SimpleCustomizeToast.show(getContext(), R.string.serial_port_launch_failed);
            }
            return false;
        }
    };

    private EditPreferenceHelper mSerialPortDataRequestPrefHelper = new EditPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultSerialPortDataRequestCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                mSettings.checkDataRequestCycle(newCycle);
                getSettingsActivity().getDataPrepareService().setSerialPortDataRequestCycle(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.data_request_less_than_min_cycle);
            }
            return false;
        }
    };

    private SwitchPreferenceHelper mSensorDataGatherEnablePrefHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mSensorDataGatherCyclePrefHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreferenceCompat preference) {
            setChecked(mSettings.isSensorDataGatherEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                getSettingsActivity().getDataPrepareService().startCaptureAndRecordSensorDataWithoutAllowance();
            } else {
                getSettingsActivity().getDataPrepareService().stopCaptureAndRecordSensorData();
            }
            return true;
        }
    };

    private EditPreferenceHelper mSensorDataGatherCyclePrefHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return mSettings.getDefaultSensorDataGatherCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                mSettings.checkSensorDataGatherCycle(newCycle);
                getSettingsActivity().getDataPrepareService().setSensorDataGatherCycle(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getContext(), R.string.sensor_data_gather_cycle_out_of_bounds);
            }
            return false;
        }
    };

    private SettingsActivity getSettingsActivity() {
        return (SettingsActivity) getActivity();
    }

    private abstract class PreferenceHelper<P extends Preference> implements Preference.OnPreferenceChangeListener {

        protected P mPreference;

        public void initialize(@StringRes int preferenceKeyRes) {
            mPreference = (P) findPreference(getString(preferenceKeyRes));
            if (mPreference != null) {
                onInitialize(mPreference);
                if (canSetSummary()) {
                    mPreference.setSummary(buildSummary());
                }
                mPreference.setOnPreferenceChangeListener(this);
            }
        }

        protected void onInitialize(P preference) {
        }

        public String buildSummary() {
            return mPreference.getSharedPreferences().getString(mPreference.getKey(), getDefaultValue().toString());
        }

        public Object getDefaultValue() {
            return null;
        }

        protected boolean canSetSummary() {
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (processPreferenceChange(newValue)) {
                if (canSetSummary()) {
                    preference.setSummary(newValue.toString());
                }
                return true;
            }
            return false;
        }

        public boolean processPreferenceChange(Object newValue) {
            try {
                return onPreferenceChange(newValue);
            } catch (NumberFormatException nfe) {
                SimpleCustomizeToast.show(getContext(), R.string.parameter_format_error);
            } catch (Exception e) {
                ExceptionLog.record(e);
                SimpleCustomizeToast.show(getContext(), R.string.set_failed);
            }
            return false;
        }

        public abstract boolean onPreferenceChange(Object newValue);

        public P getPreference() {
            return mPreference;
        }
    }

    private abstract class EditPreferenceHelper extends PreferenceHelper<EditTextPreference> {

        @Override
        protected void onInitialize(EditTextPreference preference) {
            if (TextUtils.isEmpty(preference.getText())) {
                boolean persistent = preference.isPersistent();
                if (persistent) {
                    preference.setPersistent(false);
                    preference.setText(buildSummary());
                    preference.setPersistent(true);
                } else {
                    preference.setText(buildSummary());
                }
            }
        }
    }

    private abstract class SwitchPreferenceHelper extends PreferenceHelper<SwitchPreferenceCompat> {

        @Override
        protected boolean canSetSummary() {
            return false;
        }

        public void setChecked(boolean checked) {
            getPreference().setChecked(checked);
            onCheckedChanged(checked);
        }

        protected abstract void onCheckedChanged(boolean checked);

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                onCheckedChanged((Boolean) newValue);
                return true;
            }
            return false;
        }
    }

    private abstract class ListPreferenceHelper extends PreferenceHelper<ListPreference> {

        @Override
        protected void onInitialize(ListPreference preference) {
            if (TextUtils.isEmpty(preference.getValue())) {
                boolean persistent = preference.isPersistent();
                if (persistent) {
                    preference.setPersistent(false);
                    preference.setValue(buildSummary());
                    preference.setPersistent(true);
                } else {
                    preference.setValue(buildSummary());
                }
            }
        }
    }
}
