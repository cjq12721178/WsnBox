package com.weisi.tool.wsnbox.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.preference.EditPreferenceHelper;
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper;
import com.weisi.tool.wsnbox.processor.accessor.SensorDynamicDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.TcpSensorDataAccessor;

/**
 * Created by CJQ on 2018/1/4.
 */

public class TcpSettingsFragment extends BaseSettingsFragment {

    private SwitchPreferenceHelper mEnablePreferenceHelper = new SwitchPreferenceHelper() {

        private PreferenceCategory mLaunchParameterCategory;
        private PreferenceCategory mUseParameterCategory;

        @Override
        protected void onCheckedChanged(boolean checked) {
            mLaunchParameterCategory.setEnabled(!checked);
            mUseParameterCategory.setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreference preference) {
            mLaunchParameterCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_launch_parameter));
            mUseParameterCategory = (PreferenceCategory) findPreference(getString(R.string.preference_key_use_parameter));
            setChecked(getSettings().isTcpEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            TcpSensorDataAccessor accessor = getPreferenceActivity()
                    .getDataPrepareService()
                    .getTcpSensorDataAccessor();
            Context context = getActivity();
            if (enabled) {
                accessor.startDataAccess(
                        context,
                        getSettings(),
                        getPreferenceActivity(),
                        new SensorDynamicDataAccessor.OnStartResultListener() {
                            @Override
                            public void onStartSuccess(SensorDynamicDataAccessor accessor) {
                                SimpleCustomizeToast.show(R.string.tcp_launch_succeed);
                            }

                            @Override
                            public void onStartFailed(SensorDynamicDataAccessor accessor, int cause) {
                                if (cause == TcpSensorDataAccessor.ERR_IS_CONNECTING) {
                                    SimpleCustomizeToast.show(R.string.tcp_connecting);
                                } else {
                                    SimpleCustomizeToast.show(R.string.tcp_launch_failed);
                                    accessor.stopDataAccess(context);
                                    mEnablePreferenceHelper.setChecked(false);
                                }
                            }
                        });
            } else {
                accessor.stopDataAccess(getActivity());
                SimpleCustomizeToast.show(R.string.tcp_shutdown);
            }
            return true;
        }
    };

    private EditPreferenceHelper mIpPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultRemoteServerIp();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                String newIp = (String) newValue;
                getSettings().checkIp(newIp);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.ip_format_error);
            }
            return false;
        }
    };

    private EditPreferenceHelper mPortPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultRemoteServerPort();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                int newPort = Integer.parseInt((String) newValue);
                getSettings().checkPort(newPort);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.port_out_of_bounds);
            }
            return false;
        }
    };

    private EditPreferenceHelper mTcpDataRequestPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultTcpDataRequestCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                getSettings().checkDataRequestCycle(newCycle);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .getTcpSensorDataAccessor()
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
        addPreferencesFromResource(R.xml.settings_tcp);
        setHasOptionsMenu(true);

        mIpPreferenceHelper.initialize(this, R.string.preference_key_remote_server_ip);
        mPortPreferenceHelper.initialize(this, R.string.preference_key_remote_server_port);
        mTcpDataRequestPreferenceHelper.initialize(this, R.string.preference_key_tcp_data_request_cycle);
        mEnablePreferenceHelper.initialize(this, R.string.preference_key_tcp_enable);
    }
}
