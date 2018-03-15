package com.weisi.tool.wsnbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.StringRes;

import com.cjq.lib.weisi.node.SensorManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.io.database.SensorDatabase;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.processor.BleSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.CommonSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.SensorDataAccessor;
import com.weisi.tool.wsnbox.processor.SensorDataExporter;
import com.weisi.tool.wsnbox.processor.SerialPortSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.TcpSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.UdpSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.UsbSensorDataAccessor;

import java.util.concurrent.TimeUnit;

public class DataPrepareService extends Service implements SensorDataAccessor.OnStartResultListener {

    private final LocalBinder mLocalBinder = new LocalBinder();
    private SensorDataExporter mSensorDataExporter;
    private BleSensorDataAccessor mBleSensorDataAccessor;
    private UdpSensorDataAccessor mUdpSensorDataAccessor;
    private SerialPortSensorDataAccessor mSerialPortSensorDataAccessor;
    private UsbSensorDataAccessor mUsbSensorDataAccessor;
    private TcpSensorDataAccessor mTcpSensorDataAccessor;

    private final Handler mEventHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorDataExporter.DATABASE_INSERT_SENSOR_DATA_ERROR:
                    SimpleCustomizeToast.show(DataPrepareService.this,
                            getString(R.string.database_insert_sensor_data_error));
                    break;
                case SensorDataExporter.SENSOR_DATA_RECORDER_SHUTDOWN:
                    SensorDatabase.shutdown();
                    break;
            }
        }
    };

    public DataPrepareService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onStartSuccess(SensorDataAccessor accessor) {
    }

    @Override
    public void onStartFailed(SensorDataAccessor accessor, int cause) {
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
                    toastStringRes = R.string.tcp_launch_failed;
                    break;
                case TcpSensorDataAccessor.ERR_START_LISTEN_FAILED:
                    toastStringRes = R.string.start_tcp_listen_failed;
                    break;
            }
        }
        if (toastStringRes != 0) {
            SimpleCustomizeToast.show(this, toastStringRes);
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
            //mSerialPortSensorDataAccessor = new SerialPortSensorDataAccessorImpl();
            try {
                mSerialPortSensorDataAccessor = (SerialPortSensorDataAccessor) Class.forName("com.weisi.tool.wsnbox.processor.SerialPortSensorDataAccessorImpl").newInstance();
            } catch (Exception e) {
                mSerialPortSensorDataAccessor = new SerialPortSensorDataAccessor();
            }
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

    public void startAccessSensorData(PermissionsRequester.Builder builder) {
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
        CommonSensorDataAccessor.release();
    }

    public void setOnSensorNetInListener(SensorDataAccessor.OnSensorNetInListener listener) {
        SensorDataAccessor.setOnSensorNetInListener(listener);
    }

    public void startSensorValueUpdater(SensorDataAccessor.OnSensorValueUpdateListener listener) {
        SensorDataAccessor.setOnSensorValueUpdateListener(listener);
    }

    public void stopSensorValueUpdater() {
        SensorDataAccessor.setOnSensorValueUpdateListener(null);
    }

    public void startCaptureAndRecordSensorData() {
        Settings settings = getBaseApplication().getSettings();
        if (!settings.isSensorDataGatherEnable()) {
            return;
        }
        startCaptureAndRecordSensorDataWithoutAllowance();
    }

    public void startCaptureAndRecordSensorDataWithoutAllowance() {
        if (mSensorDataExporter == null) {
            mSensorDataExporter = new SensorDataExporter(mEventHandler);
        }
        setSensorDataGatherCycleImpl(getBaseApplication().getSettings().getSensorDataGatherCycle());
        mSensorDataExporter.startCaptureAndRecordSensorData();
    }

    private void setSensorDataGatherCycleImpl(long cycle) {
        mSensorDataExporter.setMinTimeIntervalForDuplicateValue(TimeUnit.SECONDS.toMillis(cycle));
    }

    public void stopCaptureAndRecordSensorData() {
        if (mSensorDataExporter == null) {
            return;
        }
        mSensorDataExporter.stopCaptureAndRecordSensorData();
    }

    public void setSensorDataGatherCycle(long cycle) {
        if (mSensorDataExporter == null) {
            return;
        }
        setSensorDataGatherCycleImpl(cycle);
    }
}
