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

import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;

/**
 * A {@link PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
public abstract class BasePreferenceActivity extends PreferenceActivity implements ActivityFunctionDelegate.CallBack {

    private AppCompatDelegate mDelegate;
    private ActivityFunctionDelegate mFunctionDelegate;

    public DataPrepareService getDataPrepareService() {
        return getFunctionDelegate().getDataPrepareService();
    }

    public final BaseApplication getBaseApplication() {
        return getFunctionDelegate().getBaseApplication();
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
            mFunctionDelegate = new ActivityFunctionDelegate(this, this);
        }
        return mFunctionDelegate;
    }

    @Override
    public void onServiceConnectionCreate(DataPrepareService service) {

    }

    @Override
    public void onServiceConnectionStart(DataPrepareService service) {

    }

    @Override
    public void onServiceConnectionStop(DataPrepareService service) {

    }

    @Override
    public void onServiceConnectionDestroy(DataPrepareService service) {

    }

    @Override
    public void onInitActionBar(View customView) {

    }

    @Override
    public PermissionsRequester build(int type) {
        return getFunctionDelegate().build(type);
    }
}
