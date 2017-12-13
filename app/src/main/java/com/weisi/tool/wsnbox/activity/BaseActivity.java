package com.weisi.tool.wsnbox.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.service.DataPrepareService;

/**
 * Created by CJQ on 2017/9/7.
 */

public class BaseActivity extends AppCompatActivity {

    private DataPrepareService mDataPrepareService;
    private TextView mTvTitle;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mDataPrepareService == null) {
                mDataPrepareService = ((DataPrepareService.LocalBinder)service).getService();
                onServiceConnectionCreate(mDataPrepareService);
            }
            onServiceConnectionStart(mDataPrepareService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            onServiceConnectionStop(mDataPrepareService);
            onServiceConnectionDestroy(mDataPrepareService);
            mDataPrepareService = null;
        }
    };

    public DataPrepareService getDataPrepareService() {
        return mDataPrepareService;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.bg_title));
        actionBar.setCustomView(R.layout.title_general);
        View customView = actionBar.getCustomView();
        mTvTitle = (TextView) customView.findViewById(R.id.tv_title);
        if (mTvTitle != null) {
            setTitle(getTitle());
            mTvTitle.setGravity(Gravity.CENTER);
        }
        onInitActionBar(customView);
    }

    protected void onInitActionBar(View customView) {

    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTvTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mTvTitle.setText(titleId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, DataPrepareService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onServiceConnectionStop(mDataPrepareService);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDataPrepareService != null) {
            onServiceConnectionDestroy(mDataPrepareService);
            mDataPrepareService = null;
        }
    }

    protected void onServiceConnectionCreate(DataPrepareService service) {

    }

    protected void onServiceConnectionStart(DataPrepareService service) {

    }

    protected void onServiceConnectionStop(DataPrepareService service) {

    }

    protected void onServiceConnectionDestroy(DataPrepareService service) {

    }

    public final BaseApplication getBaseApplication() {
        return (BaseApplication) getApplication();
    }
}
