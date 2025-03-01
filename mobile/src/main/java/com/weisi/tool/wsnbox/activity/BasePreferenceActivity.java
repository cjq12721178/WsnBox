package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.PracticalMeasurement;
import com.cjq.lib.weisi.iot.Sensor;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
public abstract class BasePreferenceActivity
        extends PreferenceActivity
        implements BaseActivityFunction {

    private AppCompatDelegate mDelegate;
    private ActivityFunctionDelegate mFunctionDelegate;

    @Override
    public DataPrepareService getDataPrepareService() {
        return getFunctionDelegate().getDataPrepareService();
    }

    @Override
    @NonNull
    public final BaseApplication getBaseApplication() {
        return getFunctionDelegate().getBaseApplication();
    }

    @Override
    public void notifyRegisteredFragmentsServiceConnectionCreate() {
    }

    @Override
    public void notifyRegisteredFragmentsServiceConnectionStart() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getFunctionDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    @NonNull
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return super.onOptionsItemSelected(item);
        }
        if (getFunctionDelegate().onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFunctionDelegate().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFunctionDelegate().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
        getFunctionDelegate().onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getFunctionDelegate().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getFunctionDelegate().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    private ActivityFunctionDelegate getFunctionDelegate() {
        if (mFunctionDelegate == null) {
            mFunctionDelegate = new ActivityFunctionDelegate(this);
        }
        return mFunctionDelegate;
    }

//    @Override
//    public void onHeaderClick(Header header, int position) {
//        if (header.fragment == null && header.intent != null) {
//            //getString(R.string.action_start_about_activity)
//            header.intent.setAction(getString(getResources().getIdentifier(header.intent.getAction(), "string", getPackageName())));
//        }
//        super.onHeaderClick(header, position);
//    }

    @Override
    public void onServiceConnectionCreate(@NonNull DataPrepareService service) {
    }

    @Override
    public void onServiceConnectionStart(@NonNull DataPrepareService service) {
    }

    @Override
    public void onServiceConnectionStop(@NonNull DataPrepareService service) {
    }

    @Override
    public void onServiceConnectionDestroy(@NonNull DataPrepareService service) {
    }

    @Override
    public void onInitActionBar(@NonNull View customView) {
    }

    @Override
    public PermissionsRequester build(int type) {
        return getFunctionDelegate().build(type);
    }

    @Override
    public boolean invalid() {
        return getFunctionDelegate().isActivityInvalid();
    }

    @Override
    public void onSensorConfigurationChanged() {
        getFunctionDelegate().onSensorConfigurationChanged();
    }

    @Override
    public boolean onValueTestResult(@NotNull Sensor.Info info, @NotNull PracticalMeasurement measurement, @NotNull DisplayMeasurement.Value value, int warnResult) {
        return getFunctionDelegate().onValueTestResult(info, measurement, value, warnResult);
    }
}
