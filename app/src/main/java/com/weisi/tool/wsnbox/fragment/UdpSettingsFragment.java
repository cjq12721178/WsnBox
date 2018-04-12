package com.weisi.tool.wsnbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.preference.EditPreferenceHelper;
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper;
import com.weisi.tool.wsnbox.processor.accessor.SensorDynamicDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.UdpSensorDataAccessor;

import java.net.UnknownHostException;

/**
 * Created by CJQ on 2018/1/4.
 */

public class UdpSettingsFragment extends BaseSettingsFragment {

    private SwitchPreferenceHelper mEnablePreferenceHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mIpPreferenceHelper.getPreference().setEnabled(checked);
            mPortPreferenceHelper.getPreference().setEnabled(checked);
            mUdpDataRequestPreferenceHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreference preference) {
            setChecked(getSettings().isUdpEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            UdpSensorDataAccessor accessor = getPreferenceActivity()
                    .getDataPrepareService()
                    .getUdpSensorDataAccessor();
            Context context = getActivity();
            if (enabled) {
                accessor.startDataAccess(
                        getActivity(),
                        getSettings(),
                        getPreferenceActivity(),
                        new SensorDynamicDataAccessor.OnStartResultListener() {
                            @Override
                            public void onStartSuccess(SensorDynamicDataAccessor accessor) {
                                SimpleCustomizeToast.show(R.string.udp_launch_succeed);
                            }

                            @Override
                            public void onStartFailed(SensorDynamicDataAccessor accessor, int cause) {
                                SimpleCustomizeToast.show(R.string.udp_launch_failed);
                                accessor.stopDataAccess(context);
                                mEnablePreferenceHelper.setChecked(false);
                            }
                        });
            } else {
                accessor.stopDataAccess(context);
                SimpleCustomizeToast.show(R.string.udp_shutdown);
            }
            return true;
        }
    };

    private EditPreferenceHelper mIpPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultBaseStationIp();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                String newIp = (String) newValue;
                getSettings().checkIp(newIp);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getUdpSensorDataAccessor()
                        .setDataRequestTaskTargetIp(newIp);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.ip_format_error);
            } catch (UnknownHostException e) {
                SimpleCustomizeToast.show(R.string.set_base_station_ip_failed);
            }
            return false;
        }
    };

    private EditPreferenceHelper mPortPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultBaseStationPort();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                int newPort = Integer.parseInt((String) newValue);
                getSettings().checkPort(newPort);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getUdpSensorDataAccessor()
                        .setDataRequestTaskTargetPort(newPort);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.port_out_of_bounds);
            }
            return false;
        }
    };

    private EditPreferenceHelper mUdpDataRequestPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultUdpDataRequestCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                getSettings().checkDataRequestCycle(newCycle);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getUdpSensorDataAccessor()
                        .restartDataRequestTask(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.data_request_less_than_min_cycle);
            }
            return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_udp);
        setHasOptionsMenu(true);

        mIpPreferenceHelper.initialize(this, R.string.preference_key_base_station_ip);
        mPortPreferenceHelper.initialize(this, R.string.preference_key_base_station_port);
        mUdpDataRequestPreferenceHelper.initialize(this, R.string.preference_key_udp_data_request_cycle);
        mEnablePreferenceHelper.initialize(this, R.string.preference_key_udp_enable);
    }
}
