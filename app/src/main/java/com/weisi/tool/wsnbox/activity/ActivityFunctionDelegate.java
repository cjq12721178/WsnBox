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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.permission.BlePermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.weisi.tool.wsnbox.service.DataPrepareService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by CJQ on 2018/1/4.
 */

public class ActivityFunctionDelegate {

    private final Activity mActivity;
    private final CallBack mCallBack;
    private DataPrepareService mDataPrepareService;
    //private TextView mTvTitle;

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
        actionBar.setDisplayHomeAsUpEnabled(true);
        setOverflowShowingAlways();
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(mActivity, R.color.bg_title));
//        actionBar.setCustomView(R.layout.title_general);
//        View customView = actionBar.getCustomView();
//        mTvTitle = (TextView) customView.findViewById(R.id.tv_title);
//        if (mTvTitle != null) {
//            //setTitle(mActivity.getTitle());
//            mTvTitle.setGravity(Gravity.CENTER);
//        }
//        mCallBack.onInitActionBar(customView);
    }

    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(mActivity);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(mActivity);
                if (NavUtils.shouldUpRecreateTask(mActivity, upIntent)) {
                    TaskStackBuilder.create(mActivity)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(mActivity, upIntent);
                }
                return true;
            default:
                return false;
        }
    }

    public void onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
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
        PermissionsRequester.performRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionsRequester.onActivityResult(requestCode, resultCode, data);
    }

    public PermissionsRequester build(int type) {
        switch (type) {
            case PermissionsRequesterBuilder.TYPE_BLE:
                return new BlePermissionsRequester(mActivity);
            default:return null;
        }
    }

    public void showExpectDialog(@NonNull FragmentManager fragmentManager) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setTitle(R.string.function_expect);
        dialog.setDrawCancelButton(false);
        dialog.show(fragmentManager,
                "function_expect");
    }

    public interface CallBack extends PermissionsRequesterBuilder {
        void onServiceConnectionCreate(DataPrepareService service);
        void onServiceConnectionStart(DataPrepareService service);
        void onServiceConnectionStop(DataPrepareService service);
        void onServiceConnectionDestroy(DataPrepareService service);
        void onInitActionBar(View customView);
        PermissionsRequester build(int type);
    }
}
