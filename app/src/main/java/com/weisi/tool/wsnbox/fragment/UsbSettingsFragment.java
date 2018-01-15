package com.weisi.tool.wsnbox.fragment;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.preference.EditPreferenceHelper;
import com.weisi.tool.wsnbox.preference.ListPreferenceHelper;
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper;
import com.weisi.tool.wsnbox.processor.SensorDataAccessor;

/**
 * Created by CJQ on 2018/1/4.
 */

public class UsbSettingsFragment extends BaseSettingsFragment implements SensorDataAccessor.OnStartResultListener {

    private SwitchPreferenceHelper mEnablePreferenceHelper = new SwitchPreferenceHelper() {

        private PreferenceCategory mLaunchParameterCategory;
        private PreferenceCategory mUseParameterCategory;

        @Override
        protected void onInitialize(SwitchPreference preference) {
            mLaunchParameterCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_launch_parameter_serial_port));
            mUseParameterCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_use_parameter_serial_port));
            setChecked(getSettings().isUsbEnable());
        }

        @Override
        protected void onCheckedChanged(boolean checked) {
            mLaunchParameterCategory.setEnabled(!checked);
            mUseParameterCategory.setEnabled(checked);
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getUsbSensorDataAccessor()
                        .startDataAccess(
                                getActivity(),
                                getSettings(),
                                getPreferenceActivity(),
                                UsbSettingsFragment.this);
            } else {
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getUsbSensorDataAccessor()
                        .stopDataAccess(getActivity());
                SimpleCustomizeToast.show(getActivity(), getString(R.string.usb_shut_down));
            }
            return true;
        }
    };

    private ListPreferenceHelper mProtocolPreferenceHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbProtocol();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            getPreferenceActivity()
                    .getDataPrepareService()
                    .getUsbSensorDataAccessor()
                    .setProtocol(newValue.toString());
            return true;
        }
    };

    private ListPreferenceHelper mVendorProductIdPreferenceHelper = new ListPreferenceHelper() {
        @Override
        public boolean onPreferenceChange(Object newValue) {
            return true;
        }

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbVendorProductId();
        }
    };

    private ListPreferenceHelper mBaudRatePreferenceHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbBaudRate();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            int newBaudRate = Integer.parseInt(newValue.toString());
            Settings settings = getSettings();
            if (!getPreferenceActivity()
                    .getDataPrepareService()
                    .getUsbSensorDataAccessor()
                    .setCommunicationParameter(
                            newBaudRate,
                            settings.getUsbDataBits(),
                            settings.getUsbStopBits(),
                            settings.getUsbParity())) {
                SimpleCustomizeToast.show(getActivity(), R.string.usb_baud_rate_set_failed);
                return false;
            }
            return true;
        }
    };

    private ListPreferenceHelper mDataBitsPreferenceHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbDataBits();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            int newDataBits = Integer.parseInt(newValue.toString());
            Settings settings = getSettings();
            if (!getPreferenceActivity()
                    .getDataPrepareService()
                    .getUsbSensorDataAccessor()
                    .setCommunicationParameter(
                            settings.getUsbBaudRate(),
                            newDataBits,
                            settings.getUsbStopBits(),
                            settings.getUsbParity())) {
                SimpleCustomizeToast.show(getActivity(), R.string.usb_data_bits_set_failed);
                return false;
            }
            return true;
        }
    };

    private ListPreferenceHelper mStopBitsPreferenceHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbStopBits();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            int newStopBits = Integer.parseInt(newValue.toString());
            Settings settings = getSettings();
            if (!getPreferenceActivity()
                    .getDataPrepareService()
                    .getUsbSensorDataAccessor()
                    .setCommunicationParameter(
                            settings.getUsbBaudRate(),
                            settings.getUsbDataBits(),
                            newStopBits,
                            settings.getUsbParity())) {
                SimpleCustomizeToast.show(getActivity(), R.string.usb_stop_bits_set_failed);
                return false;
            }
            return true;
        }
    };

    private ListPreferenceHelper mParityPreferenceHelper = new ListPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbParity();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            int newParity = Integer.parseInt(newValue.toString());
            Settings settings = getSettings();
            if (!getPreferenceActivity()
                    .getDataPrepareService()
                    .getUsbSensorDataAccessor()
                    .setCommunicationParameter(
                            settings.getUsbBaudRate(),
                            settings.getUsbDataBits(),
                            settings.getUsbStopBits(),
                            newParity)) {
                SimpleCustomizeToast.show(getActivity(), R.string.usb_parity_set_failed);
                return false;
            }
            return true;
        }
    };

    private EditPreferenceHelper mDataRequestPreferenceHelper = new EditPreferenceHelper() {
        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUsbDataRequestCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                getSettings().checkDataRequestCycle(newCycle);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getUsbSensorDataAccessor()
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
        addPreferencesFromResource(R.xml.settings_usb);
        setHasOptionsMenu(true);

        mEnablePreferenceHelper.initialize(this, R.string.preference_key_usb_enable);
        mVendorProductIdPreferenceHelper.initialize(this, R.string.preference_key_usb_vendor_product_id);
        mProtocolPreferenceHelper.initialize(this, R.string.preference_key_usb_protocol);
        mBaudRatePreferenceHelper.initialize(this, R.string.preference_key_usb_baud_rate);
        mDataBitsPreferenceHelper.initialize(this, R.string.preference_key_usb_data_bits);
        mStopBitsPreferenceHelper.initialize(this, R.string.preference_key_usb_stop_bits);
        mParityPreferenceHelper.initialize(this, R.string.preference_key_usb_parity);
        mDataRequestPreferenceHelper.initialize(this, R.string.preference_key_usb_data_request_cycle);
    }

    @Override
    public void onStartSuccess(SensorDataAccessor accessor) {
        SimpleCustomizeToast.show(getActivity(), R.string.usb_launch_succeed);
    }

    @Override
    public void onStartFailed(SensorDataAccessor accessor, int cause) {
        SimpleCustomizeToast.show(getActivity(), R.string.usb_launch_failed);
        getPreferenceActivity()
                .getDataPrepareService()
                .getUsbSensorDataAccessor()
                .stopDataAccess(getActivity());
        mEnablePreferenceHelper.setChecked(false);
    }
}
