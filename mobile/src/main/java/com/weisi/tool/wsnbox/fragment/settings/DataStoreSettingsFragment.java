package com.weisi.tool.wsnbox.fragment.settings;

import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.preference.EditPreferenceHelper;
import com.weisi.tool.wsnbox.preference.SwitchPreferenceHelper;

/**
 * Created by CJQ on 2018/1/4.
 */

public class DataStoreSettingsFragment extends BaseSettingsFragment {

    private SwitchPreferenceHelper mEnablePreferenceHelper = new SwitchPreferenceHelper() {

        @Override
        protected void onCheckedChanged(boolean checked) {
            mGatherCyclePreferenceHelper.getPreference().setEnabled(checked);
        }

        @Override
        protected void onInitialize(SwitchPreference preference) {
            setChecked(getSettings().isSensorDataGatherEnable());
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            boolean enabled = (boolean) newValue;
            if (enabled) {
                getPreferenceActivity()
                        .getDataPrepareService()
                        .startCaptureAndRecordSensorDataWithoutAllowance();
            } else {
                getPreferenceActivity()
                        .getDataPrepareService()
                        .stopCaptureAndRecordSensorData();
            }
            return true;
        }
    };

    private EditPreferenceHelper mGatherCyclePreferenceHelper = new EditPreferenceHelper() {

        @Override
        public Object getDefaultValue() {
            return getSettings().getDefaultSensorDataGatherCycle();
        }

        @Override
        public boolean onPreferenceChange(Object newValue) {
            try {
                long newCycle = Long.parseLong((String) newValue);
                getSettings().checkSensorDataGatherCycle(newCycle);
                getPreferenceActivity()
                        .getDataPrepareService()
                        .setSensorDataGatherCycle(newCycle);
                return true;
            } catch (IllegalArgumentException iae) {
                SimpleCustomizeToast.show(R.string.sensor_data_gather_cycle_out_of_bounds);
            }
            return false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_data_store);
        setHasOptionsMenu(true);

        mGatherCyclePreferenceHelper.initialize(this, R.string.preference_key_sensor_data_gather_cycle);
        mEnablePreferenceHelper.initialize(this, R.string.preference_key_sensor_data_gather_enable);
    }
}
