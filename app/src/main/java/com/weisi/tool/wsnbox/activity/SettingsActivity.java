package com.weisi.tool.wsnbox.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.fragment.BleSettingsFragment;
import com.weisi.tool.wsnbox.fragment.DataStoreSettingsFragment;
import com.weisi.tool.wsnbox.fragment.SerialPortSettingsFragment;
import com.weisi.tool.wsnbox.fragment.TcpSettingsFragment;
import com.weisi.tool.wsnbox.fragment.UdpSettingsFragment;
import com.weisi.tool.wsnbox.fragment.UsbSettingsFragment;

import java.util.List;

/**
 * Created by CJQ on 2018/1/4.
 */

public class SettingsActivity extends BasePreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_header, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || BleSettingsFragment.class.getName().equals(fragmentName)
                || UdpSettingsFragment.class.getName().equals(fragmentName)
                || UsbSettingsFragment.class.getName().equals(fragmentName)
                || SerialPortSettingsFragment.class.getName().equals(fragmentName)
                || DataStoreSettingsFragment.class.getName().equals(fragmentName)
                || TcpSettingsFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
