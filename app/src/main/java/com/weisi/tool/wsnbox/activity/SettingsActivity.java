package com.weisi.tool.wsnbox.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.fragment.settings.BleSettingsFragment;
import com.weisi.tool.wsnbox.fragment.settings.DataStoreSettingsFragment;
import com.weisi.tool.wsnbox.fragment.settings.SerialPortSettingsFragment;
import com.weisi.tool.wsnbox.fragment.settings.TcpSettingsFragment;
import com.weisi.tool.wsnbox.fragment.settings.UdpSettingsFragment;
import com.weisi.tool.wsnbox.fragment.settings.UsbSettingsFragment;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.util.UriHelper;

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

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.fragment == null && header.intent == null) {
            switch (header.titleRes) {
                case R.string.data_export:
                    new PermissionsRequester(this, 2, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }) {
                        @Override
                        protected int getRequestRationaleRes() {
                            return R.string.grant_write_permission;
                        }
                    }.requestPermissions(new PermissionsRequester.OnRequestResultListener() {
                        @Override
                        public void onPermissionsGranted() {
                            getDataPrepareService().exportSensorDataToExcel();
                        }

                        @Override
                        public void onPermissionsDenied() {
                            SimpleCustomizeToast.show(R.string.lack_write_permissions);
                        }
                    });
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 3) {
            String filePath = UriHelper.getRealFilePath(this, data.getData());
            if (TextUtils.isEmpty(filePath)) {
                SimpleCustomizeToast.show(R.string.config_provider_null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
