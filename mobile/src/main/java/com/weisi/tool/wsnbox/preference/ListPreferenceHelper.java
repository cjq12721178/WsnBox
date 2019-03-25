package com.weisi.tool.wsnbox.preference;

import android.preference.ListPreference;
import android.text.TextUtils;

/**
 * Created by CJQ on 2018/1/4.
 */

public abstract class ListPreferenceHelper extends PreferenceHelper<ListPreference> {

    @Override
    protected void onInitialize(ListPreference preference) {
        if (TextUtils.isEmpty(preference.getValue())) {
            boolean persistent = preference.isPersistent();
            if (persistent) {
                preference.setPersistent(false);
                preference.setValue(getDefaultValue().toString());
                preference.setPersistent(true);
            } else {
                preference.setValue(getDefaultValue().toString());
            }
        }
    }

    @Override
    public String buildSummary() {
        return String.valueOf(getPreference().getEntry());
    }
}
