package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.permission.PriorPermissionsRequester;
import com.weisi.tool.wsnbox.service.DataPrepareService;

import java.util.Objects;

public class MainActivity
        extends BaseActivity
        implements BaseDialog.OnDialogConfirmListener,
        ConfirmDialog.OnDialogConfirmListener,
        ConfirmDialog.OnDialogCancelListener,
        View.OnClickListener {

    private static final String DIALOG_TAG_CONFIGURATION_NOT_PREPARED = "tag_cfg_no_prep";
    private static final String DIALOG_TAG_APP_LACK_OF_PERMISSIONS = "lack_permissions";
    private static final String DIALOG_TAG_ENABLE_MONITOR_DATA_BACKGROUND = "en_mon_data_bg";

    private static final int INSTALL_PERMISS_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(R.string.home_page_title);

        //Log.d(Tag.LOG_TAG_D_TEST, "MainActivity onCreate");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (getBaseApplication().isConfigurationPrepared()) {
            new PriorPermissionsRequester(this).requestPermissions(new PermissionsRequester.OnRequestResultListener() {
                @Override
                public void onPermissionsGranted() {
                    checkVersionAndDecideIfUpdate(false);
                    startService(new Intent(MainActivity.this, DataPrepareService.class));
                }

                @Override
                public void onPermissionsDenied() {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setTitle(R.string.app_lack_of_permissions);
                    dialog.setDrawCancelButton(false);
                    dialog.show(getSupportFragmentManager(),
                            DIALOG_TAG_APP_LACK_OF_PERMISSIONS);
                }
            });
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(R.string.application_configuration_not_prepared);
            dialog.setDrawCancelButton(false);
            dialog.show(getSupportFragmentManager(),
                    DIALOG_TAG_CONFIGURATION_NOT_PREPARED);
        }
    }

//    @Override
//    public void onServiceConnectionCreate(@NonNull DataPrepareService service) {
//        Log.d(Tag.LOG_TAG_D_TEST, "MainActivity onServiceConnectionCreate");
//        if (service.isInitialized()) {
//            return;
//        }
//        if (service.importSensorAssets()) {
//            service.startAccessSensorData(this);
//            service.startListenDataAlarm();
//            if (SensorDatabase.launch(this)) {
//                service.finishInitialization();
//                service.startCaptureAndRecordSensorData();
//                service.importSensorConfigurations();
//            } else {
//                ConfirmDialog dialog = new ConfirmDialog();
//                dialog.setTitle(R.string.launch_sensor_database_failed);
//                dialog.setDrawCancelButton(false);
//                dialog.show(getSupportFragmentManager(),
//                        "launch_sensor_database_failed");
//            }
//        } else {
//            ConfirmDialog dialog = new ConfirmDialog();
//            dialog.setTitle(R.string.import_sensor_configurations_failed);
//            dialog.setDrawCancelButton(false);
//            dialog.show(getSupportFragmentManager(),
//                    DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED);
//        }
//    }

//    @Override
//    public void onServiceConnectionDestroy(@NonNull DataPrepareService service) {
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!getSettings().isDataMonitorBackgroundEnable()) {
            stopService(new Intent(this, DataPrepareService.class));
        }
        //Log.d(Tag.LOG_TAG_D_TEST, "MainActivity onDestroy");
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
    public boolean onConfirm(@NonNull BaseDialog dialog) {
        switch (Objects.requireNonNull(dialog.getTag())) {
            case DIALOG_TAG_CONFIGURATION_NOT_PREPARED:
            case DIALOG_TAG_APP_LACK_OF_PERMISSIONS:
            case ActivityFunctionDelegate.DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED:
                finish();
                break;
            case DIALOG_TAG_UPDATE_APP:
                updateVersion(dialog);
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
                showExpectDialog();
                break;
            case R.id.cl_product_display:
                if (getSettings().getDataBrowseValueContainerConfigurationProviderId() != 0) {
                    startActivity(new Intent(this, DemonstrationActivity.class));
                } else {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.setTitle(R.string.no_configuration_provider);
                    dialog.setDrawCancelButton(false);
                    dialog.show(getSupportFragmentManager(), "pd_no_cfg");
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSettings().isDataMonitorBackgroundEnableSet()) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(R.string.enable_monitor_data_background);
            dialog.setDrawNoPromptTag(true);
            dialog.show(getSupportFragmentManager(), DIALOG_TAG_ENABLE_MONITOR_DATA_BACKGROUND);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onConfirm(@NonNull ConfirmDialog dialog, boolean noTips) {
        if (dialog.getTag() == null) {
            return true;
        }
        switch (dialog.getTag()) {
            case DIALOG_TAG_ENABLE_MONITOR_DATA_BACKGROUND:
                getSettings().setDataMonitorBackgroundEnable(true);
                super.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onCancel(@NonNull ConfirmDialog dialog, boolean noTips) {
        if (TextUtils.isEmpty(dialog.getTag())) {
            return;
        }
        switch (dialog.getTag()) {
            case DIALOG_TAG_ENABLE_MONITOR_DATA_BACKGROUND:
                if (noTips) {
                    getSettings().setDataMonitorBackgroundEnable(false);
                }
                super.onBackPressed();
                break;
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == INSTALL_PERMISS_CODE) {
//            //Toast.makeText(this,"安装应用",Toast.LENGTH_SHORT).show();
//            Updater.tryStartInstallActivity(this, (DownloadManager) Objects.requireNonNull(getSystemService(DOWNLOAD_SERVICE)), Updater.getCurrentDownloadApkId(this));
//        }
//    }
}
