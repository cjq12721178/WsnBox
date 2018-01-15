package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;

/**
 * Created by CJQ on 2017/9/7.
 */

public class BaseActivity
        extends AppCompatActivity
        implements PermissionsRequester.Builder,
        ActivityFunctionDelegate.CallBack {

    private ActivityFunctionDelegate mFunctionDelegate;

    //private DataPrepareService mDataPrepareService;
    //private TextView mTvTitle;

//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            if (mDataPrepareService == null) {
//                mDataPrepareService = ((DataPrepareService.LocalBinder)service).getService();
//                onServiceConnectionCreate(mDataPrepareService);
//            }
//            onServiceConnectionStart(mDataPrepareService);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            onServiceConnectionStop(mDataPrepareService);
//            onServiceConnectionDestroy(mDataPrepareService);
//            mDataPrepareService = null;
//        }
//    };

    public DataPrepareService getDataPrepareService() {
        //return mDataPrepareService;
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

//    private void initActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar == null) {
//            return;
//        }
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.bg_title));
//        actionBar.setCustomView(R.layout.title_general);
//        View customView = actionBar.getCustomView();
//        mTvTitle = (TextView) customView.findViewById(R.id.tv_title);
//        if (mTvTitle != null) {
//            setTitle(getTitle());
//            mTvTitle.setGravity(Gravity.CENTER);
//        }
//        onInitActionBar(customView);
//    }

//    @Override
//    public void setTitle(CharSequence title) {
//        super.setTitle(title);
//        //mTvTitle.setText(title);
//        getFunctionDelegate().setTitle(title);
//    }
//
//    @Override
//    public void setTitle(int titleId) {
//        super.setTitle(titleId);
//        //mTvTitle.setText(titleId);
//        getFunctionDelegate().setTitle(titleId);
//    }


    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getFunctionDelegate().setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //bindService(new Intent(this, DataPrepareService.class), mServiceConnection, BIND_AUTO_CREATE);
        getFunctionDelegate().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //onServiceConnectionStop(mDataPrepareService);
        //unbindService(mServiceConnection);
        getFunctionDelegate().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mDataPrepareService != null) {
//            onServiceConnectionDestroy(mDataPrepareService);
//            mDataPrepareService = null;
//        }
        getFunctionDelegate().onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        PermissionsRequester requester = PermissionsRequester.Manager.find(requestCode);
//        if (requester != null) {
//            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requester);
//        }
        getFunctionDelegate().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        PermissionsRequester requester = PermissionsRequester.Manager.find(requestCode);
//        if (requester != null) {
//            requester.onActivityResult(resultCode, data);
//        }
        getFunctionDelegate().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
//        switch (type) {
//            case PermissionsRequester.TYPE_BLE:
//                return new BlePermissionsRequester(this);
//        }
//        return null;
        return getFunctionDelegate().build(type);
    }

    private ActivityFunctionDelegate getFunctionDelegate() {
        if (mFunctionDelegate == null) {
            mFunctionDelegate = new ActivityFunctionDelegate(this, this);
        }
        return mFunctionDelegate;
    }
}
