package com.weisi.tool.wsnbox.preference;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.StringRes;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.R;

/**
 * Created by CJQ on 2018/1/4.
 */

public abstract class PreferenceHelper<P extends Preference>
        implements Preference.OnPreferenceChangeListener {

    private P mPreference;

    public void initialize(PreferenceFragment fragment, @StringRes int preferenceKeyRes) {
        mPreference = (P) fragment.findPreference(fragment.getString(preferenceKeyRes));
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

    public abstract Object getDefaultValue();

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
            SimpleCustomizeToast.show(R.string.parameter_format_error);
        } catch (Exception e) {
            ExceptionLog.record(e);
            SimpleCustomizeToast.show(R.string.set_failed);
        }
        return false;
    }

    public abstract boolean onPreferenceChange(Object newValue);

    public P getPreference() {
        return mPreference;
    }
}
