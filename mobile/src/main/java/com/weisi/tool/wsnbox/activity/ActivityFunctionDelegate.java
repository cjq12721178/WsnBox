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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.PracticalMeasurement;
import com.cjq.lib.weisi.iot.Sensor;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.io.database.SensorDatabase;
import com.weisi.tool.wsnbox.permission.BlePermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.weisi.tool.wsnbox.service.DataPrepareService;
import com.weisi.tool.wsnbox.service.ServiceInfoObserver;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by CJQ on 2018/1/4.
 */

public class ActivityFunctionDelegate<A extends Activity & BaseActivityFunction> implements ServiceInfoObserver {

    static final String DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED = "tag_import_sns_cfg_err";

    private final A mActivity;
    private DataPrepareService mDataPrepareService;
    //private TextView mTvTitle;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mDataPrepareService == null) {
                mDataPrepareService = ((DataPrepareService.LocalBinder)service).getService();
                mDataPrepareService.setServiceInfoObserver(mActivity);
                initDataPrepareService(mDataPrepareService);
                mActivity.onServiceConnectionCreate(mDataPrepareService);
                mActivity.notifyRegisteredFragmentsServiceConnectionCreate();
            }
            mActivity.onServiceConnectionStart(mDataPrepareService);
            mActivity.notifyRegisteredFragmentsServiceConnectionStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mActivity.onServiceConnectionStop(mDataPrepareService);
            mActivity.onServiceConnectionDestroy(mDataPrepareService);
            mDataPrepareService.setServiceInfoObserver(null);
            mDataPrepareService = null;
        }
    };

    public ActivityFunctionDelegate(@NonNull A activity) {
        mActivity = activity;
    }

    public DataPrepareService getDataPrepareService() {
        return mDataPrepareService;
    }

    public final BaseApplication getBaseApplication() {
        return (BaseApplication) mActivity.getApplication();
    }

    private void initDataPrepareService(@NonNull DataPrepareService service) {
        if (mDataPrepareService.isInitialized()) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (service.importSensorAssets()) {
            service.startAccessSensorData(mActivity);
            service.startListenDataAlarm();
            service.connectWearable();
            if (SensorDatabase.launch(mActivity)) {
                service.finishInitialization();
                service.startCaptureAndRecordSensorData();
                service.importSensorConfigurations();
            } else {
                if (fragmentManager != null) {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setTitle(R.string.launch_sensor_database_failed);
                    dialog.setDrawCancelButton(false);
                    dialog.show(fragmentManager,
                            "launch_sensor_database_failed");
                } else {
                    SimpleCustomizeToast.show(R.string.launch_sensor_database_failed);
                    mActivity.finish();
                }
            }
        } else {
            if (fragmentManager != null) {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setTitle(R.string.import_sensor_configurations_failed);
                dialog.setDrawCancelButton(false);
                dialog.show(fragmentManager,
                        DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED);
            } else {
                SimpleCustomizeToast.show(R.string.import_sensor_configurations_failed);
                mActivity.finish();
            }
        }
    }

    private FragmentManager getSupportFragmentManager() {
        if (mActivity instanceof FragmentActivity) {
            return ((FragmentActivity) mActivity).getSupportFragmentManager();
        }
        return null;
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
                if (upIntent != null) {
                    if (NavUtils.shouldUpRecreateTask(mActivity, upIntent)
                            || mActivity.isTaskRoot()) {
                        TaskStackBuilder.create(mActivity)
                                .addNextIntentWithParentStack(upIntent)
                                .startActivities();
                    } else {
                        mActivity.finish();
//                        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        NavUtils.navigateUpTo(mActivity, upIntent);
                    }
//                    if (NavUtils.shouldUpRecreateTask(mActivity, upIntent)) {
//                        TaskStackBuilder.create(mActivity)
//                                .addNextIntentWithParentStack(upIntent)
//                                .startActivities();
//                    } else {
//                        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        NavUtils.navigateUpTo(mActivity, upIntent);
//                    }
                    return true;
                } else {
                    return false;
                }
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
        mActivity.onServiceConnectionStop(mDataPrepareService);
        mActivity.unbindService(mServiceConnection);
    }

    protected void onDestroy() {
        if (mDataPrepareService != null) {
            mActivity.onServiceConnectionDestroy(mDataPrepareService);
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

    public boolean isActivityInvalid() {
        return mActivity.isFinishing() || mActivity.isDestroyed();
    }

    @Override
    public void onSensorConfigurationChanged() {
    }

    @Override
    public boolean onValueTestResult(@NotNull Sensor.Info info, @NotNull PracticalMeasurement measurement, @NotNull DisplayMeasurement.Value value, int warnResult) {
        return false;
    }
}
