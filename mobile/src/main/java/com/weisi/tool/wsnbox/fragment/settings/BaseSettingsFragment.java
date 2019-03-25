package com.weisi.tool.wsnbox.fragment.settings;

import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.weisi.tool.wsnbox.activity.BasePreferenceActivity;
import com.weisi.tool.wsnbox.bean.configuration.Settings;

/**
 * Created by CJQ on 2018/1/4.
 */

public class BaseSettingsFragment extends PreferenceFragment {

    public BasePreferenceActivity getPreferenceActivity() {
        return (BasePreferenceActivity) getActivity();
    }

    public Settings getSettings() {
        return getPreferenceActivity().getBaseApplication().getSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
