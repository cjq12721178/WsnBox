package com.weisi.tool.wsnbox.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.StringRes;

import com.cjq.lib.weisi.sensor.ConfigurationManager;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.io.SensorDatabase;
import com.weisi.tool.wsnbox.permission.PermissionsRequester;
import com.weisi.tool.wsnbox.processor.BleSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.CommonSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.SensorDataAccessor;
import com.weisi.tool.wsnbox.processor.SensorDataExporter;
import com.weisi.tool.wsnbox.processor.SerialPortSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.UdpSensorDataAccessor;
import com.weisi.tool.wsnbox.processor.UsbSensorDataAccessor;

import java.util.concurrent.TimeUnit;

public class DataPrepareService extends Service implements SensorDataAccessor.OnStartResultListener {

    private final LocalBinder mLocalBinder = new LocalBinder();
    //private UdpKit mUdpKit;
    //private BleKit mBleKit;
    //private SerialPortKit mSerialPortKit;
    //private ScoutUdpSensorProtocol mUdpProtocol;
    //private ScoutBleSensorProtocol mBleProtocol;
    //private ScoutUdpSensorProtocol mSerialPortProtocol;
    //private UdpDataRequestTask mUdpDataRequestTask;
    //private SerialPortDataRequestTask mSerialPortDataRequestTask;
    //private DataReceiver mSerialPortDataReceiver;

    //private Timer mDataRequestTimer;
    //private long mLastNetInTimestamp;
    //private final Object mLastNetInTimeLocker = new Object();
    //private SensorDataAccessor.OnSensorNetInListener mOnSensorNetInListener;
    //private SensorDataAccessor.OnSensorValueUpdateListener mOnSensorValueUpdateListener;
    private SensorDataExporter mSensorDataExporter;

    private BleSensorDataAccessor mBleSensorDataAccessor;
    private UdpSensorDataAccessor mUdpSensorDataAccessor;
    private SerialPortSensorDataAccessor mSerialPortSensorDataAccessor;
    private UsbSensorDataAccessor mUsbSensorDataAccessor;

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

//    private Timer getDataRequestTimer() {
//        if (mDataRequestTimer == null) {
//            mDataRequestTimer = new Timer();
//        }
//        return mDataRequestTimer;
//    }

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
            mSerialPortSensorDataAccessor = new SerialPortSensorDataAccessor();
        }
        return mSerialPortSensorDataAccessor;
    }

    public UsbSensorDataAccessor getUsbSensorDataAccessor() {
        if (mUsbSensorDataAccessor == null) {
            mUsbSensorDataAccessor = new UsbSensorDataAccessor();
        }
        return mUsbSensorDataAccessor;
    }

    public boolean importSensorConfigurations() {
        return ConfigurationManager.importEthernetConfiguration(this)
                && ConfigurationManager.importBleConfiguration(this);
    }

    public void startAccessSensorData(PermissionsRequester.Builder builder) {
        Settings settings = getBaseApplication().getSettings();
//        if (settings.isUdpEnable()) {
//            if (!launchUdp(settings)) {
//                SimpleCustomizeToast.show(this, getString(R.string.udp_launch_failed));
//            }
//        }

        if (settings.isUdpEnable()) {
            getUdpSensorDataAccessor()
                    .startDataAccess(this, settings, builder, this);
        }

//        if (settings.isBleEnable()) {
//            if (!launchBle(settings)) {
//                SimpleCustomizeToast.show(this, getString(R.string.ble_launch_failed));
//            }
//        }

        if (settings.isBleEnable()) {
            getBleSensorDataAccessor()
                    .startDataAccess(this, settings, builder, this);
        }

//        if (settings.isSerialPortEnable()) {
//            if (!launchSerialPort(settings)) {
//                SimpleCustomizeToast.show(this, getString(R.string.serial_port_launch_failed));
//            }
//        }
        if (settings.isSerialPortEnable()) {
            getSerialPortSensorDataAccessor().startDataAccess(this, settings, builder, this);
        }

        if (settings.isUsbEnable()) {
            getUsbSensorDataAccessor().startDataAccess(this, settings, null, this);
        }
    }

//    public boolean launchUdp(Settings settings) {
//        if (mUdpKit == null) {
//            mUdpKit = new UdpKit();
//        }
//        if (!mUdpKit.launch(0)) {
//            return false;
//        }
//        mUdpKit.startListen(true, this);
//        startUdpDataRequestTask(settings.getBaseStationIp(),
//                settings.getBaseStationPort(),
//                settings.getUdpDataRequestCycle());
//        return true;
//    }

//    private void startUdpDataRequestTask(String ip, int port, long cycle) {
//        if (mUdpProtocol == null) {
//            mUdpProtocol = new ScoutUdpSensorProtocol();
//        }
//        if (mUdpDataRequestTask == null) {
//            mUdpDataRequestTask = new UdpDataRequestTask();
//        }
//        mUdpDataRequestTask.setTargetIp(ip);
//        mUdpDataRequestTask.setTargetPort(port);
//        mUdpDataRequestTask.setDataRequestFrame(mUdpProtocol.makeDataRequestFrame());
//        getDataRequestTimer().schedule(mUdpDataRequestTask, 0, cycle);
//    }

//    public boolean launchBle(Settings settings) {
//        if (mBleKit == null) {
//            mBleKit = new BleKit();
//        }
//        if (!mBleKit.launch(this)) {
//            return false;
//        }
//        if (mBleProtocol == null) {
//            mBleProtocol = new ScoutBleSensorProtocol();
//        }
//        return startBleScanImpl(settings.getBleScanCycle(), settings.getBleScanDuration());
//    }

//    public boolean launchSerialPort(Settings settings) {
//        return launchSerialPortImpl(settings.isSerialPortEnable(),
//                settings.getSerialPortName(),
//                settings.getSerialPortBaudRate(),
//                settings.getSerialPortDataRequestCycle());
//    }

//    private boolean launchSerialPortImpl(boolean enabled, String portName, int baudRate, long dataRequestCycle) {
//        if (enabled) {
//            if (!SerialPortProcessor.processPreLaunch()) {
//                return false;
//            }
//            if (mSerialPortKit == null) {
//                mSerialPortKit = new SerialPortKit();
//            }
//            if (!mSerialPortKit.launch(portName, baudRate, 0)) {
//                return false;
//            }
//            mSerialPortKit.startListen(this);
//            startSerialPortDataRequestTask(dataRequestCycle);
//        }
//        return true;
//    }

//    private void startSerialPortDataRequestTask(long cycle) {
//        if (mSerialPortProtocol == null) {
//            mSerialPortProtocol = new ScoutUdpSensorProtocol();
//        }
//        if (mSerialPortDataRequestTask == null) {
//            mSerialPortDataRequestTask = new SerialPortDataRequestTask();
//        }
//        mSerialPortDataRequestTask.setDataRequestFrame(mSerialPortProtocol.makeDataRequestFrame());
//        getDataRequestTimer().schedule(mSerialPortDataRequestTask, 0, cycle);
//    }

    public void shutdownCommunicators() {
        //shutdownUdp();
        if (mUdpSensorDataAccessor != null) {
            mUdpSensorDataAccessor.stopDataAccess(this);
            mUdpSensorDataAccessor = null;
        }
        //shutdownBle();
        if (mBleSensorDataAccessor != null) {
            mBleSensorDataAccessor.stopDataAccess(this);
            mBleSensorDataAccessor = null;
        }
        //shutdownSerialPort();
        if (mSerialPortSensorDataAccessor != null) {
            mSerialPortSensorDataAccessor.stopDataAccess(this);
            mSerialPortSensorDataAccessor = null;
        }

        if (mUsbSensorDataAccessor != null) {
            mUsbSensorDataAccessor.stopDataAccess(this);
            mUsbSensorDataAccessor = null;
        }
        //shutdownDataRequestTimer();
        CommonSensorDataAccessor.release();
    }

//    public void shutdownUdp() {
//        stopUdpDataRequestTask();
//        if (mUdpKit != null) {
//            mUdpKit.close();
//            mUdpKit = null;
//        }
//        if (mUdpProtocol != null) {
//            mUdpProtocol = null;
//        }
//    }

//    private void stopUdpDataRequestTask() {
//        if (mUdpDataRequestTask != null) {
//            mUdpDataRequestTask.cancel();
//            mUdpDataRequestTask = null;
//        }
//    }

//    public void shutdownBle() {
//        if (mBleKit != null) {
//            mBleKit.stopScan();
//        }
//        if (mBleProtocol != null) {
//            mBleProtocol = null;
//        }
//    }

//    public void shutdownSerialPort() {
//        stopSerialPortDataRequestTask();
//        if (mSerialPortKit != null) {
//            mSerialPortKit.shutdown();
//            mSerialPortKit = null;
//        }
//        if (mSerialPortProtocol != null) {
//            mSerialPortProtocol = null;
//        }
//        SerialPortProcessor.processPostShutdown();
//    }

//    private void stopSerialPortDataRequestTask() {
//        if (mSerialPortDataRequestTask != null) {
//            mSerialPortDataRequestTask.cancel();
//            mSerialPortDataRequestTask = null;
//        }
//    }

//    public void shutdownDataRequestTimer() {
//        if (mDataRequestTimer != null) {
//            mDataRequestTimer.cancel();
//            mDataRequestTimer = null;
//        }
//    }

//    //UDP
//    @Override
//    public void onDataReceived(byte[] data) {
//        mUdpProtocol.analyze(data, this);
//    }

//    //BLE
//    @Override
//    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        mBleProtocol.analyze(device.getAddress(), scanRecord, this);
//    }

//    //SerialPort
//    @Override
//    public int onDataReceived(byte[] data, int len) {
//        mSerialPortProtocol.analyze(data, 0, len, this);
//        return len;
//    }

//    @Override
//    public void onDataAnalyzed(int sensorAddress,
//                               byte dataTypeValue,
//                               int dataTypeIndex,
//                               ValueBuildDelegator valueBuildDelegator) {
//        //printCommunicationData(sensorAddress, dataTypeValue, dataTypeIndex);
//        Sensor sensor = SensorManager.getSensor(sensorAddress, true);
//        int position = sensor.addDynamicValue(dataTypeValue, dataTypeIndex, valueBuildDelegator);
//        if (!recordSensorNetIn(sensor)) {
//            if (mOnSensorValueUpdateListener != null) {
//                mOnSensorValueUpdateListener.onSensorValueUpdate(sensor, position);
//            }
//        }
//    }

//    @Override
//    public void onTimeSynchronizationFinished(int i, int i1, int i2, int i3, int i4, int i5) {
//
//    }

//    private void printCommunicationData(int sensorAddress, byte dataTypeValue, int dataTypeIndex) {
//        ClosableLog.d(Tag.LOG_TAG_D_COMMUNICATION_DATA,
//                String.format("sensor address = %06X, data type value = %02X, index = %d",
//                        sensorAddress, dataTypeValue, dataTypeIndex));
//    }

//    private boolean recordSensorNetIn(Sensor sensor) {
//        if (sensor.getNetInTimestamp() == 0) {
//            synchronized (mLastNetInTimeLocker) {
//                long currentNetInTimestamp = System.currentTimeMillis();
//                if (mLastNetInTimestamp >= currentNetInTimestamp) {
//                    currentNetInTimestamp = mLastNetInTimestamp + 1;
//                }
//                sensor.setNetInTimestamp(currentNetInTimestamp);
//                mLastNetInTimestamp = currentNetInTimestamp;
//                if (mOnSensorNetInListener != null) {
//                    mOnSensorNetInListener.onSensorNetIn(sensor);
//                }
//            }
//            return true;
//        }
//        return false;
//    }

    public void setOnSensorNetInListener(SensorDataAccessor.OnSensorNetInListener listener) {
        //mOnSensorNetInListener = listener;
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

//    private class UdpDataRequestTask extends TimerTask {
//
//        private InetAddress mTargetIp;
//        private int mTargetPort;
//        private byte[] mDataRequestFrame;
//
//        public boolean setTargetIp(String ip) {
//            try {
//                mTargetIp = InetAddress.getByName(ip);
//                return true;
//            } catch (UnknownHostException e) {
//                mTargetIp = null;
//                ExceptionLog.debug(e);
//            }
//            return false;
//        }
//
//        public void setTargetPort(int port) {
//            mTargetPort = port;
//        }
//
//        public void setDataRequestFrame(byte[] dataRequestFrame) {
//            mDataRequestFrame = dataRequestFrame;
//        }
//
//        public boolean isPrepared() {
//            return mTargetIp != null
//                    && mTargetPort >= 0
//                    && mTargetPort < 65536
//                    && mDataRequestFrame != null;
//        }
//
//        @Override
//        public void run() {
//            try {
//                mUdpKit.sendData(mTargetIp, mTargetPort, mDataRequestFrame);
//            } catch (IOException e) {
//                ExceptionLog.debug(e);
//            }
//        }
//    }

//    private class SerialPortDataRequestTask extends TimerTask {
//
//        private byte[] mDataRequestFrame;
//
//        public void setDataRequestFrame(byte[] dataRequestFrame) {
//            mDataRequestFrame = dataRequestFrame;
//        }
//
//        @Override
//        public void run() {
//            try {
//                mSerialPortKit.send(mDataRequestFrame);
//            } catch (IOException e) {
//                ExceptionLog.debug(e);
//            }
//        }
//    };

//    public boolean setDataRequestTaskTargetIp(String ip) {
//        if (mUdpDataRequestTask == null) {
//            return false;
//        }
//        return mUdpDataRequestTask.setTargetIp(ip);
//    }

//    public void setUdpBaseStationPort(int port) {
//        if (mUdpDataRequestTask == null) {
//            return;
//        }
//        mUdpDataRequestTask.setTargetPort(port);
//    }

//    public void setUdpDataRequest(long cycle) {
//        if (mDataRequestTimer == null) {
//            return;
//        }
//        stopUdpDataRequestTask();
//        Settings settings = getBaseApplication().getSettings();
//        startUdpDataRequestTask(settings.getBaseStationIp(),
//                settings.getBaseStationPort(),
//                cycle);
//        //mDataRequestTimer.schedule(mUdpDataRequestTask, 0, cycle);
//    }

//    public void setBleScanCycle(long cycle) {
//        if (mBleKit == null) {
//            return;
//        }
//        mBleKit.stopScan();
//        startBleScanImpl(cycle, getBaseApplication().getSettings().getBleScanDuration());
//    }

//    private boolean startBleScanImpl(long cycle, long duration) {
//        return mBleKit.startScan(this,
//                TimeUnit.SECONDS.toMillis(cycle),
//                TimeUnit.SECONDS.toMillis(duration));
//    }

//    public void setBleScanDuration(long duration) {
//        if (mBleKit == null) {
//            return;
//        }
//        mBleKit.stopScan();
//        startBleScanImpl(getBaseApplication().getSettings().getBleScanCycle(), duration);
//    }

//    public boolean setSerialPortName(String portName) {
//        shutdownSerialPort();
//        Settings settings = getBaseApplication().getSettings();
//        return launchSerialPortImpl(settings.isSerialPortEnable(),
//                portName,
//                settings.getSerialPortBaudRate(),
//                settings.getSerialPortDataRequestCycle());
//    }
//
//    public boolean setSerialPortBaudRate(int baudRate) {
//        shutdownSerialPort();
//        Settings settings = getBaseApplication().getSettings();
//        return launchSerialPortImpl(settings.isSerialPortEnable(),
//                settings.getSerialPortName(),
//                baudRate,
//                settings.getSerialPortDataRequestCycle());
//    }

//    public void setSerialPortDataRequestCycle(long cycle) {
//        if (mDataRequestTimer == null) {
//            return;
//        }
//        stopSerialPortDataRequestTask();
//        startSerialPortDataRequestTask(cycle);
//        //mSerialPortDataRequestTask.cancel();
//        //mDataRequestTimer.schedule(mSerialPortDataRequestTask, 0, cycle);
//    }

    public void setSensorDataGatherCycle(long cycle) {
        if (mSensorDataExporter == null) {
            return;
        }
        setSensorDataGatherCycleImpl(cycle);
    }
}
