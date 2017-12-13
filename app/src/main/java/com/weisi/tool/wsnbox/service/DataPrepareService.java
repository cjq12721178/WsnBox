package com.weisi.tool.wsnbox.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.cjq.lib.weisi.communicator.BleKit;
import com.cjq.lib.weisi.communicator.SerialPortKit;
import com.cjq.lib.weisi.communicator.UdpKit;
import com.cjq.lib.weisi.protocol.OnFrameAnalyzedListener;
import com.cjq.lib.weisi.protocol.ScoutBleSensorProtocol;
import com.cjq.lib.weisi.protocol.ScoutUdpSensorProtocol;
import com.cjq.lib.weisi.sensor.ConfigurationManager;
import com.cjq.lib.weisi.sensor.Sensor;
import com.cjq.lib.weisi.sensor.SensorManager;
import com.cjq.lib.weisi.sensor.ValueBuildDelegator;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.application.BaseApplication;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.io.SensorDatabase;
import com.weisi.tool.wsnbox.processor.SensorDataProcessor;
import com.weisi.tool.wsnbox.processor.SerialPortProcessor;
import com.weisi.tool.wsnbox.util.Tag;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DataPrepareService
        extends Service
        implements UdpKit.OnDataReceivedListener,
        BluetoothAdapter.LeScanCallback,
        OnFrameAnalyzedListener, SerialPortKit.OnDataReceivedListener {

    private final LocalBinder mLocalBinder = new LocalBinder();
    private UdpKit mUdpKit;
    private BleKit mBleKit;
    private SerialPortKit mSerialPortKit;
    private ScoutUdpSensorProtocol mUdpProtocol;
    private ScoutBleSensorProtocol mBleProtocol;
    private ScoutUdpSensorProtocol mSerialPortProtocol;
    private UdpDataRequestTask mUdpDataRequestTask;
    private SerialPortDataRequestTask mSerialPortDataRequestTask;
    private Timer mDataRequestTimer;
    private long mLastNetInTimestamp;
    private final Object mLastNetInTimeLocker = new Object();
    private OnSensorNetInListener mOnSensorNetInListener;
    private OnSensorValueUpdateListener mOnSensorValueUpdateListener;
    private SensorDataProcessor mSensorDataProcessor;

    private final Handler mEventHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorDataProcessor.DATABASE_INSERT_SENSOR_DATA_ERROR:
                    SimpleCustomizeToast.show(DataPrepareService.this,
                            getString(R.string.database_insert_sensor_data_error));
                    break;
                case SensorDataProcessor.SENSOR_DATA_RECORDER_SHUTDOWN:
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

    public class LocalBinder extends Binder {

        public DataPrepareService getService() {
            return DataPrepareService.this;
        }
    }

    public BaseApplication getBaseApplication() {
        return (BaseApplication) getApplication();
    }

    private Timer getDataRequestTimer() {
        if (mDataRequestTimer == null) {
            mDataRequestTimer = new Timer();
        }
        return mDataRequestTimer;
    }

    public boolean importSensorConfigurations() {
        return ConfigurationManager.importEthernetConfiguration(this)
                && ConfigurationManager.importBleConfiguration(this);
    }

    public void launchCommunicators() {
        Settings settings = getBaseApplication().getSettings();
        if (settings.isUdpEnable()) {
            if (!launchUdp(settings)) {
                SimpleCustomizeToast.show(this, getString(R.string.udp_launch_failed));
            }
        }

        if (settings.isBleEnable()) {
            if (!launchBle(settings)) {
                SimpleCustomizeToast.show(this, getString(R.string.ble_launch_failed));
            }
        }

        if (settings.isSerialPortEnable()) {
            if (!launchSerialPort(settings)) {
                SimpleCustomizeToast.show(this, getString(R.string.serial_port_launch_failed));
            }
        }
    }

    public boolean launchUdp(Settings settings) {
        if (mUdpKit == null) {
            mUdpKit = new UdpKit();
        }
        if (!mUdpKit.launch(0)) {
            return false;
        }
        mUdpKit.startListen(true, this);
        startUdpDataRequestTask(settings.getBaseStationIp(),
                settings.getBaseStationPort(),
                settings.getUdpDataRequestCycle());
        return true;
    }

    private void startUdpDataRequestTask(String ip, int port, long cycle) {
        if (mUdpProtocol == null) {
            mUdpProtocol = new ScoutUdpSensorProtocol();
        }
        if (mUdpDataRequestTask == null) {
            mUdpDataRequestTask = new UdpDataRequestTask();
        }
        mUdpDataRequestTask.setTargetIp(ip);
        mUdpDataRequestTask.setTargetPort(port);
        mUdpDataRequestTask.setDataRequestFrame(mUdpProtocol.makeDataRequestFrame());
        getDataRequestTimer().schedule(mUdpDataRequestTask, 0, cycle);
    }

    public boolean launchBle(Settings settings) {
        if (mBleKit == null) {
            mBleKit = new BleKit();
        }
        if (!mBleKit.launch(this)) {
            return false;
        }
        if (mBleProtocol == null) {
            mBleProtocol = new ScoutBleSensorProtocol();
        }
        startBleScanImpl(settings.getBleScanCycle(), settings.getBleScanDuration());
        return true;
    }

    public boolean launchSerialPort(Settings settings) {
        return launchSerialPortImpl(settings.isSerialPortEnable(),
                settings.getSerialPortName(),
                settings.getSerialPortBaudRate(),
                settings.getSerialPortDataRequestCycle());
    }

    private boolean launchSerialPortImpl(boolean enabled, String portName, int baudRate, long dataRequestCycle) {
        if (enabled) {
            if (!SerialPortProcessor.processPreLaunch()) {
                return false;
            }
            if (mSerialPortKit == null) {
                mSerialPortKit = new SerialPortKit();
            }
            if (!mSerialPortKit.launch(portName, baudRate, 0)) {
                return false;
            }
            mSerialPortKit.startListen(this);
            startSerialPortDataRequestTask(dataRequestCycle);
        }
        return true;
    }

    private void startSerialPortDataRequestTask(long cycle) {
        if (mSerialPortProtocol == null) {
            mSerialPortProtocol = new ScoutUdpSensorProtocol();
        }
        if (mSerialPortDataRequestTask == null) {
            mSerialPortDataRequestTask = new SerialPortDataRequestTask();
        }
        mSerialPortDataRequestTask.setDataRequestFrame(mSerialPortProtocol.makeDataRequestFrame());
        getDataRequestTimer().schedule(mSerialPortDataRequestTask, 0, cycle);
    }

    public void shutdownCommunicators() {
        shutdownUdp();
        shutdownBle();
        shutdownSerialPort();
        shutdownDataRequestTimer();
    }

    public void shutdownUdp() {
        stopUdpDataRequestTask();
        if (mUdpKit != null) {
            mUdpKit.close();
            mUdpKit = null;
        }
        if (mUdpProtocol != null) {
            mUdpProtocol = null;
        }
    }

    private void stopUdpDataRequestTask() {
        if (mUdpDataRequestTask != null) {
            mUdpDataRequestTask.cancel();
            mUdpDataRequestTask = null;
        }
    }

    public void shutdownBle() {
        if (mBleKit != null) {
            mBleKit.stopScan();
        }
        if (mBleProtocol != null) {
            mBleProtocol = null;
        }
    }

    public void shutdownSerialPort() {
        stopSerialPortDataRequestTask();
        if (mSerialPortKit != null) {
            mSerialPortKit.shutdown();
            mSerialPortKit = null;
        }
        if (mSerialPortProtocol != null) {
            mSerialPortProtocol = null;
        }
        SerialPortProcessor.processPostShutdown();
    }

    private void stopSerialPortDataRequestTask() {
        if (mSerialPortDataRequestTask != null) {
            mSerialPortDataRequestTask.cancel();
            mSerialPortDataRequestTask = null;
        }
    }

    public void shutdownDataRequestTimer() {
        if (mDataRequestTimer != null) {
            mDataRequestTimer.cancel();
            mDataRequestTimer = null;
        }
    }

    //UDP
    @Override
    public void onDataReceived(byte[] data) {
        mUdpProtocol.analyze(data, this);
    }

    //BLE
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mBleProtocol.analyze(device.getAddress(), scanRecord, this);
    }

    //SerialPort
    @Override
    public int onDataReceived(byte[] data, int len) {
        mSerialPortProtocol.analyze(data, 0, len, this);
        return len;
    }

    @Override
    public void onDataAnalyzed(int sensorAddress,
                               byte dataTypeValue,
                               int dataTypeIndex,
                               ValueBuildDelegator valueBuildDelegator) {
        //printCommunicationData(sensorAddress, dataTypeValue, dataTypeIndex);
        Sensor sensor = SensorManager.getSensor(sensorAddress, true);
        int position = sensor.addDynamicValue(dataTypeValue, dataTypeIndex, valueBuildDelegator);
        if (!recordSensorNetIn(sensor)) {
            if (mOnSensorValueUpdateListener != null) {
                mOnSensorValueUpdateListener.onSensorValueUpdate(sensor, position);
            }
        }
    }

    @Override
    public void onTimeSynchronizationFinished(int i, int i1, int i2, int i3, int i4, int i5) {

    }

    private void printCommunicationData(int sensorAddress, byte dataTypeValue, int dataTypeIndex) {
        ClosableLog.d(Tag.LOG_TAG_D_COMMUNICATION_DATA,
                String.format("sensor address = %06X, data type value = %02X, index = %d",
                        sensorAddress, dataTypeValue, dataTypeIndex));
    }

    private boolean recordSensorNetIn(Sensor sensor) {
        if (sensor.getNetInTimestamp() == 0) {
            synchronized (mLastNetInTimeLocker) {
                long currentNetInTimestamp = System.currentTimeMillis();
                if (mLastNetInTimestamp >= currentNetInTimestamp) {
                    currentNetInTimestamp = mLastNetInTimestamp + 1;
                }
                sensor.setNetInTimestamp(currentNetInTimestamp);
                mLastNetInTimestamp = currentNetInTimestamp;
                if (mOnSensorNetInListener != null) {
                    mOnSensorNetInListener.onSensorNetIn(sensor);
                }
            }
            return true;
        }
        return false;
    }

    public void setOnSensorNetInListener(OnSensorNetInListener listener) {
        mOnSensorNetInListener = listener;
    }

    public interface OnSensorNetInListener {
        void onSensorNetIn(Sensor sensor);
    }

    public void startSensorValueUpdater(OnSensorValueUpdateListener listener) {
        mOnSensorValueUpdateListener = listener;
    }

    public void stopSensorValueUpdater() {
        mOnSensorValueUpdateListener = null;
    }

    public interface OnSensorValueUpdateListener {
        void onSensorValueUpdate(Sensor sensor, int valuePosition);
    }

    public void startCaptureAndRecordSensorData() {
        Settings settings = getBaseApplication().getSettings();
        if (!settings.isSensorDataGatherEnable()) {
            return;
        }
        startCaptureAndRecordSensorDataWithoutAllowance();
    }

    public void startCaptureAndRecordSensorDataWithoutAllowance() {
        if (mSensorDataProcessor == null) {
            mSensorDataProcessor = new SensorDataProcessor(mEventHandler);
        }
        setSensorDataGatherCycleImpl(getBaseApplication().getSettings().getSensorDataGatherCycle());
        mSensorDataProcessor.startCaptureAndRecordSensorData();
    }

    private void setSensorDataGatherCycleImpl(long cycle) {
        mSensorDataProcessor.setMinTimeIntervalForDuplicateValue(TimeUnit.SECONDS.toMillis(cycle));
    }

    public void stopCaptureAndRecordSensorData() {
        if (mSensorDataProcessor == null) {
            return;
        }
        mSensorDataProcessor.stopCaptureAndRecordSensorData();
    }

    private class UdpDataRequestTask extends TimerTask {

        private InetAddress mTargetIp;
        private int mTargetPort;
        private byte[] mDataRequestFrame;

        public boolean setTargetIp(String ip) {
            try {
                mTargetIp = InetAddress.getByName(ip);
                return true;
            } catch (UnknownHostException e) {
                mTargetIp = null;
                ExceptionLog.debug(e);
            }
            return false;
        }

        public void setTargetPort(int port) {
            mTargetPort = port;
        }

        public void setDataRequestFrame(byte[] dataRequestFrame) {
            mDataRequestFrame = dataRequestFrame;
        }

        public boolean isPrepared() {
            return mTargetIp != null
                    && mTargetPort >= 0
                    && mTargetPort < 65536
                    && mDataRequestFrame != null;
        }

        @Override
        public void run() {
            try {
                mUdpKit.sendData(mTargetIp, mTargetPort, mDataRequestFrame);
            } catch (IOException e) {
                ExceptionLog.debug(e);
            }
        }
    }

    private class SerialPortDataRequestTask extends TimerTask {

        private byte[] mDataRequestFrame;

        public void setDataRequestFrame(byte[] dataRequestFrame) {
            mDataRequestFrame = dataRequestFrame;
        }

        @Override
        public void run() {
            try {
                mSerialPortKit.send(mDataRequestFrame);
            } catch (IOException e) {
                ExceptionLog.debug(e);
            }
        }
    };

    public boolean setUdpBaseStationIp(String ip) {
        if (mUdpDataRequestTask == null) {
            return false;
        }
        return mUdpDataRequestTask.setTargetIp(ip);
    }

    public void setUdpBaseStationPort(int port) {
        if (mUdpDataRequestTask == null) {
            return;
        }
        mUdpDataRequestTask.setTargetPort(port);
    }

    public void setUdpDataRequest(long cycle) {
        if (mDataRequestTimer == null) {
            return;
        }
        stopUdpDataRequestTask();
        Settings settings = getBaseApplication().getSettings();
        startUdpDataRequestTask(settings.getBaseStationIp(),
                settings.getBaseStationPort(),
                cycle);
        //mDataRequestTimer.schedule(mUdpDataRequestTask, 0, cycle);
    }

    public void setBleScanCycle(long cycle) {
        if (mBleKit == null) {
            return;
        }
        mBleKit.stopScan();
        startBleScanImpl(cycle, getBaseApplication().getSettings().getBleScanDuration());
    }

    private void startBleScanImpl(long cycle, long duration) {
        mBleKit.startScan(this,
                TimeUnit.SECONDS.toMillis(cycle),
                TimeUnit.SECONDS.toMillis(duration));
    }

    public void setBleScanDuration(long duration) {
        if (mBleKit == null) {
            return;
        }
        mBleKit.stopScan();
        startBleScanImpl(getBaseApplication().getSettings().getBleScanCycle(), duration);
    }

    public boolean setSerialPortName(String portName) {
        shutdownSerialPort();
        Settings settings = getBaseApplication().getSettings();
        return launchSerialPortImpl(settings.isSerialPortEnable(),
                portName,
                settings.getSerialPortBaudRate(),
                settings.getSerialPortDataRequestCycle());
    }

    public boolean setSerialPortBaudRate(int baudRate) {
        shutdownSerialPort();
        Settings settings = getBaseApplication().getSettings();
        return launchSerialPortImpl(settings.isSerialPortEnable(),
                settings.getSerialPortName(),
                baudRate,
                settings.getSerialPortDataRequestCycle());
    }

    public void setSerialPortDataRequestCycle(long cycle) {
        if (mDataRequestTimer == null) {
            return;
        }
        stopSerialPortDataRequestTask();
        startSerialPortDataRequestTask(cycle);
        //mSerialPortDataRequestTask.cancel();
        //mDataRequestTimer.schedule(mSerialPortDataRequestTask, 0, cycle);
    }

    public void setSensorDataGatherCycle(long cycle) {
        if (mSensorDataProcessor == null) {
            return;
        }
        setSensorDataGatherCycleImpl(cycle);
    }
}
