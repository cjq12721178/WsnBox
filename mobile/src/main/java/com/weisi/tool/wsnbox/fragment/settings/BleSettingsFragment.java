package com.weisi.tool.wsnbox.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.preference.EditPreferenceHelper;
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper;
import com.weisi.tool.wsnbox.processor.accessor.BleSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.SensorDynamicDataAccessor;

/**
 * Created by CJQ on 2018/1/4.
 */

public class BleSettingsFragment extends BaseSettingsFragment {

    private SwitchPreferenceHelper mEnablePreferenceHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mScanCyclePreferenceHelper.getPreference().setEnabled(checked);
            mScanDurationPreferenceHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreference preference) {
            setChecked(getSettings().isBleEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            final BleSensorDataAccessor accessor = getPreferenceActivity()
                    .getDataPrepareService()
                    .getBleSensorDataAccessor();
            final Context context = getActivity();
            if (enabled) {
                accessor.startDataAccess(
                        getActivity(),
                        getSettings(),
                        getPreferenceActivity(),
                        new SensorDynamicDataAccessor.OnStartResultListener() {
                            @Override
                            public void onStartSuccess(SensorDynamicDataAccessor accessor) {
                                SimpleCustomizeToast.show(R.string.ble_launch_succeed);
                            }

                            @Override
                            public void onStartFailed(SensorDynamicDataAccessor accessor, int cause) {
                                SimpleCustomizeToast.show(R.string.ble_launch_failed);
                                accessor.stopDataAccess(context);
                                mEnablePreferenceHelper.setChecked(false);
                            }
                        });
            } else {
                accessor.stopDataAccess(context);
                SimpleCustomizeToast.show(getString(R.string.ble_shutdown));
            }
            return true;
        }
    };

    private EditPreferenceHelper mScanCyclePreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultBleScanCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                getSettings().checkBleScanCycle(newCycle);
                if (!getPreferenceActivity()
                        .getDataPrepareService()
                        .getBleSensorDataAccessor()
                        .restartBleScan(newCycle,
                                getSettings().getBleScanDuration())) {
                    throw new RuntimeException("restart ble scan failed");
                }
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.ble_scan_cycle_out_of_bounds);
            }
            return false;
        }
    };

    private EditPreferenceHelper mScanDurationPreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultBleScanDuration();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newDuration = Long.parseLong((String) newValue);
                getSettings().checkBleScanDuration(newDuration);
                if (!getPreferenceActivity()
                        .getDataPrepareService()
                        .getBleSensorDataAccessor()
                        .restartBleScan(getSettings().getBleScanCycle(), newDuration)) {
                    throw new RuntimeException("restart ble scan failed");
                }
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.ble_scan_duration_out_of_bounds);
            }
            return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_ble);
        setHasOptionsMenu(true);

        mScanCyclePreferenceHelper.initialize(this, R.string.preference_key_ble_scan_cycle);
        mScanDurationPreferenceHelper.initialize(this, R.string.preference_key_ble_scan_duration);
        mEnablePreferenceHelper.initialize(this, R.string.preference_key_ble_enable);
    }
}
