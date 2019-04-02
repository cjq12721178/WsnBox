package com.weisi.tool.wsnbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.PracticalMeasurement;
import com.cjq.lib.weisi.iot.Sensor;
import com.cjq.lib.weisi.iot.SensorManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.BuildConfig;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.permission.PermissionsRequesterBuilder;
import com.weisi.tool.wsnbox.processor.ValueAlarmer;
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
import com.weisi.tool.wsnbox.processor.importer.SensorConfigurationsImporter;
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
        OnSensorHistoryDataAccessListener,
        Sensor.OnValueAlarmListener/*, DataApi.DataListener */ {

    private final LocalBinder mLocalBinder = new LocalBinder();
    private ServiceInfoObserver mServiceInfoObserver;
    private SensorDataSQLiteExporter mSensorDataSQLiteExporter;
    private BleSensorDataAccessor mBleSensorDataAccessor;
    private UdpSensorDataAccessor mUdpSensorDataAccessor;
    private SerialPortSensorDataAccessor mSerialPortSensorDataAccessor;
    private UsbSensorDataAccessor mUsbSensorDataAccessor;
    private TcpSensorDataAccessor mTcpSensorDataAccessor;

    private IntelligentGasketSimulationDataAccess mIntelligentGasketSimulationDataAccess;

    private SensorHistoryDataAccessor mSensorHistoryDataAccessor;
    private final DataTransferStation mDataTransferStation = new DataTransferStation();

    private ValueAlarmer mValueAlarmer;
    private boolean mInitialized;

    //private GoogleApiClient mGoogleApiClient;

    private final Handler mEventHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorDataSQLiteExporter.DATABASE_INSERT_SENSOR_DATA_ERROR:
                    SimpleCustomizeToast.show(getString(R.string.database_insert_sensor_data_error));
                    break;
                case SensorDataSQLiteExporter.SENSOR_DATA_RECORDER_SHUTDOWN:
                    //SensorDatabase.shutdown();
                    break;
            }
        }
    };

    public DataPrepareService() {
    }

    public void setServiceInfoObserver(ServiceInfoObserver serviceInfoObserver) {
        mServiceInfoObserver = serviceInfoObserver;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public void finishInitialization() {
        mInitialized = true;
    }

    @Override
    public void onCreate() {
        //Log.d(Tag.LOG_TAG_D_TEST, "DataPrepareService onCreate");
        mDataTransferStation.init();
    }

    @Override
    public void onDestroy() {
        //Log.d(Tag.LOG_TAG_D_TEST, "DataPrepareService onDestroy");
        disconnectWearable();
        stopListenDataAlarm();
        stopAccessSensorData();
        stopCaptureAndRecordSensorData();
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

    @Override
    public void onValueTestResult(@NonNull Sensor.Info info, @NonNull PracticalMeasurement measurement, @NonNull DisplayMeasurement.Value value, int warnResult) {
        if (!mServiceInfoObserver.onValueTestResult(info, measurement, value, warnResult)) {
            getValueAlarmer().processValueTest(getBaseApplication().getSettings(), info, measurement, value, warnResult);
        }
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

    public ValueAlarmer getValueAlarmer() {
        if (mValueAlarmer == null) {
            mValueAlarmer = new ValueAlarmer(getApplicationContext());
        }
        return mValueAlarmer;
    }

    public boolean importSensorAssets() {
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
        if (BuildConfig.APP_DEBUG) {
            if (mIntelligentGasketSimulationDataAccess == null) {
                mIntelligentGasketSimulationDataAccess = new IntelligentGasketSimulationDataAccess();
            }
            mIntelligentGasketSimulationDataAccess.startDataAccess(this, settings, null, this);
        }
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

    public void importSensorConfigurations() {
        new SensorConfigurationsImporter(new SafeAsyncTask.ResultAchiever<SensorManager.MeasurementConfigurationProvider, SensorManager.MeasurementConfigurationProvider>() {
            @Override
            public void onProgressUpdate(@NotNull SensorManager.MeasurementConfigurationProvider[] values) {
            }

            @Override
            public void onResultAchieved(@Nullable SensorManager.MeasurementConfigurationProvider measurementConfigurationProvider) {
                if (mServiceInfoObserver != null) {
                    mServiceInfoObserver.onSensorConfigurationChanged();
                }
            }

            @Override
            public boolean invalid() {
                return false;
            }
        }).execute(getBaseApplication().getSettings().getDataBrowseValueContainerConfigurationProviderId());
    }

    public void startListenDataAlarm() {
        startListenDataAlarm(false);
    }

    public void startListenDataAlarm(boolean withoutAllowance) {
        if (withoutAllowance || getBaseApplication().getSettings().isDataWarnEnable()) {
            Sensor.setOnValueAlarmListener(this);
            getValueAlarmer().start(getBaseApplication().getSettings());
        }
    }

    public void stopListenDataAlarm() {
        Sensor.setOnValueAlarmListener(null);
        getValueAlarmer().stop();
//        if (mValueAlarmer != null) {
//            mValueAlarmer.stop();
//            mValueAlarmer = null;
//        }
    }

    public void connectWearable() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(@android.support.annotation.Nullable Bundle bundle) {
//                        Log.d(Tag.LOG_TAG_D_TEST, "wear connected, " + BuildConfig.APPLICATION_ID);
//                        new Thread(new Runnable() {
//
//                            private int count;
//
//                            @Override
//                            public void run() {
//                                for (int i = 0;i < 10;++i) {
//                                    try {
//                                        Thread.sleep(4000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                    Log.d(Tag.LOG_TAG_D_TEST, "send data to wear, count: " + count);
//                                    PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
//                                    putDataMapReq.getDataMap().putInt("count", count++);
//                                    PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//                                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
//                                        @Override
//                                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
//                                            if (!dataItemResult.getStatus().isSuccess()) {
//                                                Log.d(Tag.LOG_TAG_D_TEST, "send failed");
//                                            } else {
//                                                Log.d(Tag.LOG_TAG_D_TEST, "send success");
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        }).start();
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//                        Log.d(Tag.LOG_TAG_D_TEST, "wear connection suspended");
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//                        Log.d(Tag.LOG_TAG_D_TEST, "wear connection failed");
//                    }
//                })
//                .build();
//        mGoogleApiClient.connect();
//        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    public void disconnectWearable() {
        //Wearable.DataApi.removeListener(mGoogleApiClient, this);
        //mGoogleApiClient.disconnect();
    }

//    @Override
//    public void onDataChanged(DataEventBuffer dataEventBuffer) {
//        for (DataEvent event : dataEventBuffer) {
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                // DataItem changed
//                DataItem item = event.getDataItem();
//                if (item.getUri().getPath().compareTo("/count") == 0) {
//                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    Log.d(Tag.LOG_TAG_D_TEST, "mobile data changed, count: " + dataMap.getInt("count"));
//                }
//            } else if (event.getType() == DataEvent.TYPE_DELETED) {
//                // DataItem deleted
//                Log.d(Tag.LOG_TAG_D_TEST, "mobile data deleted");
//            }
//        }
//    }
}
