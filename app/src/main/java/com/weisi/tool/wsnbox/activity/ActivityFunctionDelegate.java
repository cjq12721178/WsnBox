package com.weisi.tool.wsnbox.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.permission.BlePermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CJQ on 2018/1/4.
 */

public class ActivityFunctionDelegate {

    private final Activity mActivity;
    private final CallBack mCallBack;
    private DataPrepareService mDataPrepareService;
    private TextView mTvTitle;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mDataPrepareService == null) {
                mDataPrepareService = ((DataPrepareService.LocalBinder)service).getService();
                mCallBack.onServiceConnectionCreate(mDataPrepareService);
            }
            mCallBack.onServiceConnectionStart(mDataPrepareService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCallBack.onServiceConnectionStop(mDataPrepareService);
            mCallBack.onServiceConnectionDestroy(mDataPrepareService);
            mDataPrepareService = null;
        }
    };

    public ActivityFunctionDelegate(@NonNull Activity activity, @NonNull CallBack callBack) {
        mActivity = activity;
        mCallBack = callBack;
    }

    public DataPrepareService getDataPrepareService() {
        return mDataPrepareService;
    }

    public final BaseApplication getBaseApplication() {
        return (BaseApplication) mActivity.getApplication();
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar;
        if (mActivity instanceof AppCompatActivity) {
            actionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
        } else if (mActivity instanceof BasePreferenceActivity) {
            actionBar = ((BasePreferenceActivity) mActivity).getSupportActionBar();
        } else {
            actionBar = null;
        }
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(mActivity, R.color.bg_title));
        actionBar.setCustomView(R.layout.title_general);
        View customView = actionBar.getCustomView();
        mTvTitle = (TextView) customView.findViewById(R.id.tv_title);
        if (mTvTitle != null) {
            //setTitle(mActivity.getTitle());
            mTvTitle.setGravity(Gravity.CENTER);
        }
        mCallBack.onInitActionBar(customView);
    }

    public void setTitle(CharSequence title) {
        mTvTitle.setText(title);
    }

    public void setTitle(int titleId) {
        mTvTitle.setText(titleId);
    }

    protected void onResume() {
        mActivity.bindService(new Intent(mActivity, DataPrepareService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onPause() {
        mCallBack.onServiceConnectionStop(mDataPrepareService);
        mActivity.unbindService(mServiceConnection);
    }

    protected void onDestroy() {
        if (mDataPrepareService != null) {
            mCallBack.onServiceConnectionDestroy(mDataPrepareService);
            mDataPrepareService = null;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsRequester requester = PermissionsRequester.Manager.find(requestCode);
        if (requester != null) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requester);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionsRequester requester = PermissionsRequester.Manager.find(requestCode);
        if (requester != null) {
            requester.onActivityResult(resultCode, data);
        }
    }

    public PermissionsRequester build(int type) {
        switch (type) {
            case PermissionsRequester.TYPE_BLE:
                return new BlePermissionsRequester(mActivity);
            default:return null;
        }
    }

    public interface CallBack extends PermissionsRequester.Builder {
        void onServiceConnectionCreate(DataPrepareService service);
        void onServiceConnectionStart(DataPrepareService service);
        void onServiceConnectionStop(DataPrepareService service);
        void onServiceConnectionDestroy(DataPrepareService service);
        void onInitActionBar(View customView);
        PermissionsRequester build(int type);
    }
}
