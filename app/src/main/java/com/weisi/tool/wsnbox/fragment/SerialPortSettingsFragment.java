package com.weisi.tool.wsnbox.fragment;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.util.SerialPortFinder;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.preference.EditPreferenceHelper;
import com.weisi.tool.wsnbox.preference.ListPreferenceHelper;
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper;
import com.weisi.tool.wsnbox.processor.SensorDataAccessor;

/**
 * Created by CJQ on 2018/1/4.
 */

public class SerialPortSettingsFragment extends BaseSettingsFragment implements SensorDataAccessor.OnStartResultListener {

    private SwitchPreferenceHelper mEnablePreferenceHelper = new SwitchPreferenceHelper() {

        private PreferenceCategory mLaunchParameterCategory;
        private PreferenceCategory mUseParameterCategory;

        @Override
        protected void onCheckedChanged(boolean checked) {
            mLaunchParameterCategory.setEnabled(!checked);
            mUseParameterCategory.setEnabled(checked);
            //mNamePreferenceHelper.getPreference().setEnabled(checked);
            //mBaudRatePreferenceHelper.getPreference().setEnabled(checked);
            //mDataRequestPreferenceHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreference preference) {
            mLaunchParameterCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_launch_parameter));
            mUseParameterCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_use_parameter));
            setChecked(getSettings().isSerialPortEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getSerialPortSensorDataAccessor()
                        .startDataAccess(
                                getActivity(),
                                getSettings(),
                                getPreferenceActivity(),
                                SerialPortSettingsFragment.this);
            } else {
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getSerialPortSensorDataAccessor()
                        .stopDataAccess(getActivity());
                SimpleCustomizeToast.show(getActivity(), getString(R.string.serial_port_shutdown));
            }
            return true;
        }
    };

    private ListPreferenceHelper mNamePreferenceHelper = new ListPreferenceHelper() {

        @Override
        protected void onInitialize(ListPreference preference) {
            super.onInitialize(preference);
            SerialPortFinder finder = new SerialPortFinder();
            preference.setEntries(finder.getAllDevices());
            preference.setEntryValues(finder.getAllDevicesPath());
        }

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultSerialPortName();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            return true;
        }
    };

    private ListPreferenceHelper mBaudRatePreferenceHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultSerialPortBaudRate();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            return true;
        }
    };

    private EditPreferenceHelper mDataRequestPreferenceHelper = new EditPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultSerialPortDataRequestCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                getSettings().checkDataRequestCycle(newCycle);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getSerialPortSensorDataAccessor()
                        .restartDataRequestTask(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(getActivity(), R.string.data_request_less_than_min_cycle);
            }
            return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_serial_port);
        setHasOptionsMenu(true);

        mNamePreferenceHelper.initialize(this, R.string.preference_key_serial_port_name);
        mBaudRatePreferenceHelper.initialize(this, R.string.preference_key_serial_port_baud_rate);
        mDataRequestPreferenceHelper.initialize(this, R.string.preference_key_serial_port_data_request_cycle);
        mEnablePreferenceHelper.initialize(this, R.string.preference_key_serial_port_enable);

    }

    @Override
    public void onStartSuccess(SensorDataAccessor accessor) {
        SimpleCustomizeToast.show(getActivity(), R.string.serial_port_launch_succeed);
    }

    @Override
    public void onStartFailed(SensorDataAccessor accessor, int cause) {
        SimpleCustomizeToast.show(getActivity(), R.string.serial_port_launch_failed);
        getPreferenceActivity()
                .getDataPrepareService()
                .getSerialPortSensorDataAccessor()
                .stopDataAccess(getActivity());
        mEnablePreferenceHelper.setChecked(false);
    }
}
