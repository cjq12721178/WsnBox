package com.weisi.tool.wsnbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.handler.CrashHandler;
import com.weisi.tool.wsnbox.io.SensorDatabase;
import com.weisi.tool.wsnbox.service.DataPrepareService;

public class MainActivity
        extends BaseActivity
        implements BaseDialog.OnDialogConfirmListener,
        View.OnClickListener {

    private static final String DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED = "import_sensor_config_failed";
    private static final String DIALOG_TAG_LAUNCH_COMMUNICATORS_FAILED = "launch_communicators_failed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.home_title);

        ExceptionLog.initialize(getApplicationContext(), "WsnBox");
        ClosableLog.setEnablePrint(true);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        startService(new Intent(this, DataPrepareService.class));
    }

    @Override
    protected void onServiceConnectionCreate(DataPrepareService service) {
        if (service.importSensorConfigurations()) {
            if (service.launchCommunicators()) {
                if (SensorDatabase.launch(this)) {
                    service.startCaptureAndRecordSensorData();
                } else {
                    ConfirmDialog dialog = new ConfirmDialog();
                    dialog.show(getSupportFragmentManager(),
                            "launch_sensor_database_failed",
                            getString(R.string.launch_sensor_database_failed),
                            false);
                }
            } else {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.show(getSupportFragmentManager(),
                        DIALOG_TAG_LAUNCH_COMMUNICATORS_FAILED,
                        getString(R.string.launch_communicators_failed),
                        false);
            }
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.show(getSupportFragmentManager(),
                    DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED,
                    getString(R.string.import_sensor_configurations_failed),
                    false);
        }
    }

    @Override
    protected void onServiceConnectionDestroy(DataPrepareService service) {
        service.shutdownCommunicators();
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
    public boolean onConfirm(BaseDialog dialog) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_IMPORT_SENSOR_CONFIGURATIONS_FAILED:
            case DIALOG_TAG_LAUNCH_COMMUNICATORS_FAILED:
                finish();
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
            case R.id.cl_freedom_scout:
            case R.id.cl_scout_config:
            case R.id.cl_scout_record:
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.show(getSupportFragmentManager(),
                        "function_expect",
                        R.string.function_expect,
                        false);
                break;
        }
    }
}
