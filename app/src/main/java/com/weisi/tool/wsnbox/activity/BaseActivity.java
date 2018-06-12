package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.weisi.tool.wsnbox.service.DataPrepareService;

/**
 * Created by CJQ on 2017/9/7.
 */

public class BaseActivity
        extends AppCompatActivity
        implements PermissionsRequesterBuilder,
        ActivityFunctionDelegate.CallBack {

    private ActivityFunctionDelegate mFunctionDelegate;

    public DataPrepareService getDataPrepareService() {
        return getFunctionDelegate().getDataPrepareService();
    }

    public final BaseApplication getBaseApplication() {
        return getFunctionDelegate().getBaseApplication();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFunctionDelegate().onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getFunctionDelegate().onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        getFunctionDelegate().onMenuOpened(featureId, menu);
        return super.onMenuOpened(featureId, menu);
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
    protected void onDestroy() {
        super.onDestroy();
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
    public void onInitActionBar(View customView) {

    }

    @Override
    public PermissionsRequester build(int type) {
        return getFunctionDelegate().build(type);
    }

    private ActivityFunctionDelegate getFunctionDelegate() {
        if (mFunctionDelegate == null) {
            mFunctionDelegate = new ActivityFunctionDelegate(this, this);
        }
        return mFunctionDelegate;
    }
}
