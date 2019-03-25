package com.weisi.tool.wsnbox.preference;

import android.preference.EditTextPreference;
import android.text.TextUtils;

/**
 * Created by CJQ on 2018/1/4.
 */

public abstract class EditPreferenceHelper extends PreferenceHelper<EditTextPreference> {

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
