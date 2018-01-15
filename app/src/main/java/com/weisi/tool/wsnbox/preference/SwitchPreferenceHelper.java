package com.weisi.tool.wsnbox.preference;

import android.preference.Preference;
import android.preference.SwitchPreference;

import com.weisi.tool.wsnbox.fragment.BaseSettingsFragment;
import com.weisi.tool.wsnbox.preference.PreferenceHelper;
import com.weisi.tool.wsnbox.processor.SensorDataAccessor;


/**
 * Created by CJQ on 2018/1/4.
 */

public abstract class SwitchPreferenceHelper extends PreferenceHelper<SwitchPreference> {

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

    @Override
    public Object getDefaultValue() {
        return null;
    }
}
