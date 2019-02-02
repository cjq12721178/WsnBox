package com.weisi.tool.wsnbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.StringRes;

import com.cjq.lib.weisi.iot.SensorManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.io.database.SensorDatabase;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.weisi.tool.wsnbox.processor.accessor.BleSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.CommonSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.IntelligentGasketSimulationDataAccess;
import com.weisi.tool.wsnbox.processor.accessor.OnSensorDynamicDataAccessListener;
import com.weisi.tool.wsnbox.processor.accessor.OnSensorHistoryDataAccessListener;
import com.weisi.tool.wsnbox.processor.accessor.SensorDynamicDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.SensorHistoryDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.SerialPortSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.TcpSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.UdpSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.accessor.UsbSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.exporter.SensorDataExcelExporter;
import com.weisi.tool.wsnbox.processor.exporter.SensorDataSQLiteExporter;
import com.weisi.tool.wsnbox.processor.transfer.DataTransferStation;
import com.weisi.tool.wsnbox.util.FlavorClassBuilder;
import com.weisi.tool.wsnbox.util.SafeAsyncTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import kotlin.Unit;

public class DataPrepareService
        extends Service
        implements SensorDynamicDataAccessor.OnStartResultListener,
        OnSensorDynamicDataAccessListener,
        OnSensorHistoryDataAccessListener {

    private final LocalBinder mLocalBinder = new LocalBinder();
    private SensorDataSQLiteExporter mSensorDataSQLiteExporter;
    private BleSensorDataAccessor mBleSensorDataAccessor;
    private UdpSensorDataAccessor mUdpSensorDataAccessor;
    private SerialPortSensorDataAccessor mSerialPortSensorDataAccessor;
    private UsbSensorDataAccessor mUsbSensorDataAccessor;
    private TcpSensorDataAccessor mTcpSensorDataAccessor;

    private IntelligentGasketSimulationDataAccess mIntelligentGasketSimulationDataAccess;

    private SensorHistoryDataAccessor mSensorHistoryDataAccessor;
    private final DataTransferStation mDataTransferStation = new DataTransferStation();

    private final Handler mEventHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorDataSQLiteExporter.DATABASE_INSERT_SENSOR_DATA_ERROR:
                    SimpleCustomizeToast.show(getString(R.string.database_insert_sensor_data_error));
                    break;
                case SensorDataSQLiteExporter.SENSOR_DATA_RECORDER_SHUTDOWN:
                    SensorDatabase.shutdown();
                    break;
            }
        }
    };

    public DataPrepareService() {
    }

    @Override
    public void onCreate() {
        mDataTransferStation.init();
    }

    @Override
    public void onDestroy() {
        if (mSensorHistoryDataAccessor != null) {
            mSensorHistoryDataAccessor.setOnSensorHistoryDataAccessListener(null);
        }
        mDataTransferStation.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onStartSuccess(SensorDynamicDataAccessor accessor) {
    }

    @Override
    public void onStartFailed(SensorDynamicDataAccessor accessor, int cause) {
        @StringRes int toastStringRes = 0;
        if (accessor instanceof BleSensorDataAccessor) {
            switch (cause) {
                case BleSensorDataAccessor.ERR_BLE_LAUNCH_FAILED:
                    toastStringRes = R.string.ble_launch_failed;
                    break;
                case BleSensorDataAccessor.ERR_BLE_START_SCAN_FAILED:
                    toastStringRes = R.string.ble_scan_failed;
                    break;
            }
        }
        if (accessor instanceof UdpSensorDataAccessor) {
            switch (cause) {
                case UdpSensorDataAccessor.ERR_LAUNCH_COMMUNICATOR_FAILED:
                    toastStringRes = R.string.udp_launch_failed;
                    break;
                case UdpSensorDataAccessor.ERR_START_LISTEN_FAILED:
                    toastStringRes = R.string.start_udp_listen_failed;
                    break;
                case UdpSensorDataAccessor.ERR_INIT_DATA_REQUEST_TASK_PARAMETER_FAILED:
                    toastStringRes = R.string.udp_data_request_parameter_set_failed;
                    break;
            }
        }
        if (accessor instanceof SerialPortSensorDataAccessor) {
            switch (cause) {
                case SerialPortSensorDataAccessor.ERR_PREPARE_START_DATA_ACCESS_FAILED:
                    toastStringRes = R.string.prepare_start_data_access_failed;
                    break;
                case SerialPortSensorDataAccessor.ERR_LAUNCH_COMMUNICATOR_FAILED:
                    toastStringRes = R.string.serial_port_launch_failed;
                    break;
                case SerialPortSensorDataAccessor.ERR_START_LISTEN_FAILED:
                    toastStringRes = R.string.start_serial_port_listen_failed;
                    break;
            }
        }
        if (accessor instanceof UsbSensorDataAccessor) {
            switch (cause) {
                case UsbSensorDataAccessor.ERR_LAUNCH_COMMUNICATOR_FAILED:
                    toastStringRes = R.string.usb_launch_failed;
                    break;
                case UsbSensorDataAccessor.ERR_START_LISTEN_FAILED:
                    toastStringRes = R.string.start_usb_listen_failed;
                    break;
                case UsbSensorDataAccessor.ERR_INIT_DATA_REQUEST_TASK_PARAMETER_FAILED:
                    toastStringRes = R.string.usb_parameter_set_failed;
            }
        }
        if (accessor instanceof TcpSensorDataAccessor) {
            switch (cause) {
                case TcpSensorDataAccessor.ERR_LAUNCH_COMMUNICATOR_FAILED:
                case TcpSensorDataAccessor.ERR_IS_CONNECTING:
                    toastStringRes = R.string.tcp_launch_failed;
                    break;
                case TcpSensorDataAccessor.ERR_START_LISTEN_FAILED:
                    toastStringRes = R.string.start_tcp_listen_failed;
                    break;
            }
        }
        if (toastStringRes != 0) {
            SimpleCustomizeToast.show(toastStringRes);
        }
    }

    @Override
    public void onSensorDynamicDataAccess(int sensorAddress, byte dataTypeValue, int dataTypeIndex, long timestamp, float batteryVoltage, double rawValue) {
        mDataTransferStation.processSensorDynamicDataAccess(sensorAddress, dataTypeValue, dataTypeIndex, timestamp, batteryVoltage, rawValue);
    }

    @Override
    public void onPhysicalSensorHistoryDataAccess(int address, long timestamp, float batteryVoltage) {
        mDataTransferStation.processSensorInfoHistoryDataAccess(address, timestamp, batteryVoltage);
    }

    @Override
    public void onLogicalSensorHistoryDataAccess(long sensorId, long timestamp, double rawValue) {
        mDataTransferStation.processMeasurementHistoryDataAccess(sensorId, timestamp, rawValue);
    }

    public class LocalBinder extends Binder {

        public DataPrepareService getService() {
            return DataPrepareService.this;
        }
    }

    public BaseApplication getBaseApplication() {
        return (BaseApplication) getApplication();
    }

    public BleSensorDataAccessor getBleSensorDataAccessor() {
        if (mBleSensorDataAccessor == null) {
            mBleSensorDataAccessor = new BleSensorDataAccessor();
        }
        return mBleSensorDataAccessor;
    }

    public UdpSensorDataAccessor getUdpSensorDataAccessor() {
        if (mUdpSensorDataAccessor == null) {
            mUdpSensorDataAccessor = new UdpSensorDataAccessor();
        }
        return mUdpSensorDataAccessor;
    }

    public SerialPortSensorDataAccessor getSerialPortSensorDataAccessor() {
        if (mSerialPortSensorDataAccessor == null) {
            mSerialPortSensorDataAccessor = FlavorClassBuilder.buildImplementation(SerialPortSensorDataAccessor.class);
//            try {
//                mSerialPortSensorDataAccessor = (SerialPortSensorDataAccessor) Class.forName("com.weisi.tool.wsnbox.processor.SerialPortSensorDataAccessorImpl").newInstance();
//            } catch (Exception e) {
//                mSerialPortSensorDataAccessor = new SerialPortSensorDataAccessor();
//            }
        }
        return mSerialPortSensorDataAccessor;
    }

    public UsbSensorDataAccessor getUsbSensorDataAccessor() {
        if (mUsbSensorDataAccessor == null) {
            mUsbSensorDataAccessor = new UsbSensorDataAccessor();
        }
        return mUsbSensorDataAccessor;
    }

    public TcpSensorDataAccessor getTcpSensorDataAccessor() {
        if (mTcpSensorDataAccessor == null) {
            mTcpSensorDataAccessor = new TcpSensorDataAccessor();
        }
        return mTcpSensorDataAccessor;
    }

    public boolean importSensorConfigurations() {
        return SensorManager.importEsbConfiguration(this)
                && SensorManager.importBleConfiguration(this);
    }

    public void startAccessSensorData(PermissionsRequesterBuilder builder) {
        SensorDynamicDataAccessor.setOnSensorDynamicDataAccessListener(this);
        Settings settings = getBaseApplication().getSettings();
        if (settings.isUdpEnable()) {
            getUdpSensorDataAccessor()
                    .startDataAccess(this, settings, builder, this);
        }
        if (settings.isBleEnable()) {
            getBleSensorDataAccessor()
                    .startDataAccess(this, settings, builder, this);
        }
        if (settings.isSerialPortEnable()) {
            getSerialPortSensorDataAccessor().startDataAccess(this, settings, builder, this);
        }
        if (settings.isUsbEnable()) {
            getUsbSensorDataAccessor().startDataAccess(this, settings, null, this);
        }
        if (settings.isTcpEnable()) {
            getTcpSensorDataAccessor().startDataAccess(this, settings, null, this);
        }
//        if (mIntelligentGasketSimulationDataAccess == null) {
//            mIntelligentGasketSimulationDataAccess = new IntelligentGasketSimulationDataAccess();
//        }
//        mIntelligentGasketSimulationDataAccess.startDataAccess(this, settings, null, this);
    }

    public void stopAccessSensorData() {
        if (mUdpSensorDataAccessor != null) {
            mUdpSensorDataAccessor.stopDataAccess(this);
            mUdpSensorDataAccessor = null;
        }
        if (mBleSensorDataAccessor != null) {
            mBleSensorDataAccessor.stopDataAccess(this);
            mBleSensorDataAccessor = null;
        }
        if (mSerialPortSensorDataAccessor != null) {
            mSerialPortSensorDataAccessor.stopDataAccess(this);
            mSerialPortSensorDataAccessor = null;
        }
        if (mUsbSensorDataAccessor != null) {
            mUsbSensorDataAccessor.stopDataAccess(this);
            mUsbSensorDataAccessor = null;
        }
        if (mTcpSensorDataAccessor != null) {
            mTcpSensorDataAccessor.stopDataAccess(this);
            mTcpSensorDataAccessor = null;
        }
        if (mIntelligentGasketSimulationDataAccess != null) {
            mIntelligentGasketSimulationDataAccess.stopDataAccess(this);
            mIntelligentGasketSimulationDataAccess = null;
        }
        CommonSensorDataAccessor.release();
        SensorDynamicDataAccessor.setOnSensorDynamicDataAccessListener(null);
    }

//    public void setOnSensorNetInListener(SensorDynamicDataAccessor.OnSensorNetInListener listener) {
//        SensorDynamicDataAccessor.setOnSensorNetInListener(listener);
//    }
//
//    public void startSensorValueUpdater(SensorDynamicDataAccessor.OnSensorValueUpdateListener listener) {
//        SensorDynamicDataAccessor.setOnSensorValueUpdateListener(listener);
//    }
//
//    public void stopSensorValueUpdater() {
//        SensorDynamicDataAccessor.setOnSensorValueUpdateListener(null);
//    }


    public SensorHistoryDataAccessor getSensorHistoryDataAccessor() {
        if (mSensorHistoryDataAccessor == null) {
            mSensorHistoryDataAccessor = new SensorHistoryDataAccessor();
            mSensorHistoryDataAccessor.setOnSensorHistoryDataAccessListener(this);
        }
        return mSensorHistoryDataAccessor;
    }

    public DataTransferStation getDataTransferStation() {
        return mDataTransferStation;
    }

    public void startCaptureAndRecordSensorData() {
        Settings settings = getBaseApplication().getSettings();
        if (!settings.isSensorDataGatherEnable()) {
            return;
        }
        startCaptureAndRecordSensorDataWithoutAllowance();
    }

    public void startCaptureAndRecordSensorDataWithoutAllowance() {
        if (mSensorDataSQLiteExporter == null) {
            mSensorDataSQLiteExporter = new SensorDataSQLiteExporter(mEventHandler);
        }
        setSensorDataGatherCycleImpl(getBaseApplication().getSettings().getSensorDataGatherCycle());
        mSensorDataSQLiteExporter.startCaptureAndRecordSensorData();
    }

    private void setSensorDataGatherCycleImpl(long cycle) {
        mSensorDataSQLiteExporter.setMinTimeIntervalForDuplicateValue(TimeUnit.SECONDS.toMillis(cycle));
    }

    public void stopCaptureAndRecordSensorData() {
        if (mSensorDataSQLiteExporter == null) {
            return;
        }
        mSensorDataSQLiteExporter.stopCaptureAndRecordSensorData();
    }

    public void setSensorDataGatherCycle(long cycle) {
        if (mSensorDataSQLiteExporter == null) {
            return;
        }
        setSensorDataGatherCycleImpl(cycle);
    }

    public void exportSensorDataToExcel() {
        SimpleCustomizeToast.show(R.string.exporting_sensor_data);
        if (!getBaseApplication().getSettings().isExportingSensorData()) {
//            new AsyncTask<String, Void, Boolean>() {
//
//                @Override
//                protected Boolean doInBackground(String... params) {
//                    return SensorDatabase.exportSensorDataToExcel(params[0]);
//                }
//
//                @Override
//                protected void onPostExecute(Boolean result) {
//                    SimpleCustomizeToast.show(result
//                            ? R.string.excel_export_success
//                            : R.string.excel_export_failed);
//                    getBaseApplication().getSettings().setExportingSensorData(result);
//                }
//            }.execute(getBaseApplication().getSettings().getOutputFilePath());
            new SensorDataExcelExporter(new SafeAsyncTask.ResultAchiever<Boolean, Unit>() {
                @Override
                public void onProgressUpdate(@NotNull Unit[] values) {
                }

                @Override
                public void onResultAchieved(@Nullable Boolean result) {
                    if (result != null && result) {
                        SimpleCustomizeToast.show(R.string.excel_export_success);
                        getBaseApplication().getSettings().setExportingSensorData(false);
                    } else {
                        SimpleCustomizeToast.show(R.string.excel_export_failed);
                    }
                }

                @Override
                public boolean invalid() {
                    return false;
                }
            }).execute(getBaseApplication().getSettings().getOutputFilePath());
        }
    }
}
