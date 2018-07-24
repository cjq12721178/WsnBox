package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.bean.update.UpdateInfo;
import com.weisi.tool.wsnbox.bean.update.Updater;
import com.weisi.tool.wsnbox.io.database.SensorDatabase;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.ReadPermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;

public class MainActivity
        extends BaseActivity
        implements BaseDialog.OnDialogConfirmListener,
        View.OnClickListener {

    private static final String DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED = "tag_import_sns_cfg_err";
    private static final String DIALOG_TAG_CONFIGURATION_NOT_PREPARED = "tag_cfg_no_prep";
    private static final String DIALOG_TAG_UPDATE_APP = "tag_update_app";
    private static final String ARGUMENT_KEY_UPDATE_INFO = "ak_update_info";
    private static final int RC_READ_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(R.string.home_page_title);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (getBaseApplication().isConfigurationPrepared()) {
            checkVersionAndUpdate();
            startService(new Intent(this, DataPrepareService.class));
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(R.string.application_configuration_not_prepared);
            dialog.setDrawCancelButton(false);
            dialog.show(getSupportFragmentManager(),
                    DIALOG_TAG_CONFIGURATION_NOT_PREPARED);
        }
    }

    private void checkVersionAndUpdate() {
        Updater.checkLatestVersion(this, updateInfo -> {
            if (Updater.hasNewVersion(getApplicationContext(), updateInfo)) {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setTitle(getString(R.string.check_for_new_version, updateInfo.getVersionName()));
                dialog.setContent(updateInfo.getVersionDescription());
                dialog.getArguments().putParcelable(ARGUMENT_KEY_UPDATE_INFO, updateInfo);
                ConfirmDialog.Decorator decorator = dialog.getCustomDecorator();
                if (updateInfo.getForceUpdate()) {
                    dialog.setDrawCancelButton(false);
                    dialog.setCancelable(false);
                    decorator.setOkLabel(R.string.immediate_update);
                } else {
                    decorator.setOkLabel(R.string.immediate_update);
                    decorator.setCancelLabel(R.string.wait_a_minute);
                }
                dialog.show(getSupportFragmentManager(), DIALOG_TAG_UPDATE_APP);
            }
        });
    }

    @Override
    public void onServiceConnectionCreate(DataPrepareService service) {
        if (service.importSensorConfigurations()) {
            service.startAccessSensorData(this);
            if (SensorDatabase.launch(this)) {
                service.startCaptureAndRecordSensorData();
            } else {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setTitle(R.string.launch_sensor_database_failed);
                dialog.setDrawCancelButton(false);
                dialog.show(getSupportFragmentManager(),
                        "launch_sensor_database_failed");
            }
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(R.string.import_sensor_configurations_failed);
            dialog.setDrawCancelButton(false);
            dialog.show(getSupportFragmentManager(),
                    DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED);
        }
    }

    @Override
    public void onServiceConnectionDestroy(DataPrepareService service) {
        service.stopAccessSensorData();
        service.stopCaptureAndRecordSensorData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, DataPrepareService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onConfirm(BaseDialog dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED:
            case DIALOG_TAG_CONFIGURATION_NOT_PREPARED:
                finish();
                break;
            case DIALOG_TAG_UPDATE_APP:
                UpdateInfo info = dialog.getArguments().getParcelable(ARGUMENT_KEY_UPDATE_INFO);
                new ReadPermissionsRequester(this, RC_READ_PERMISSION)
                        .requestPermissions(new PermissionsRequester.OnRequestResultListener() {
                            @Override
                            public void onPermissionsGranted() {
                                Updater.update(getApplicationContext(), info);
                            }

                            @Override
                            public void onPermissionsDenied() {
                                SimpleCustomizeToast.show(R.string.lack_read_permissions);
                            }
                        });
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cl_data_browse:
                startActivity(new Intent(this, DataBrowseActivity.class));
                break;
            case R.id.cl_parameter_config:
                startActivity(new Intent(this, ParameterConfigurationActivity.class));
                break;
            case R.id.cl_freedom_scout:
            case R.id.cl_scout_config:
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setTitle(R.string.function_expect);
                dialog.setDrawCancelButton(false);
                dialog.show(getSupportFragmentManager(),
                        "function_expect");
                break;
        }
    }
}
