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

    public boolean importSensorConfigurations() {
        return ConfigurationManager.importEthernetConfiguration(this)
                && ConfigurationManager.importBleConfiguration(this);
    }

    public void launchCommunicators() {
        Settings settings = getBaseApplication().getSettings();
        if (!launchUdp(settings)) {
            SimpleCustomizeToast.show(this, getString(R.string.udp_launch_failed));
        }
        if (!launchBle(settings)) {
            SimpleCustomizeToast.show(this, getString(R.string.ble_launch_failed));
        }
        if (!launchSerialPort(settings)) {
            SimpleCustomizeToast.show(this, getString(R.string.serial_port_launch_failed));
        }
//        if (!mUdpKit.launch(0)) {
//            return false;
//        }
//        mUdpKit.startListen(true, this);
//        mUdpKit.sendData("192.168.1.18", 5000, mUdpProtocol.makeDataRequestFrame(), 500);
//
//        if (!mBleKit.launch(this)) {
//            return false;
//        }
//        mBleKit.startScan(this, 5000, 10000);
//
//        if (!powerOnSerialPort()) {
//            return false;
//        }
//        if (!mSerialPortKit.launch("ttyHSL1", 115200, 0)) {
//            return false;
//        }
//        mSerialPortKit.startListen(this);
//
//        mDataRequestTimer.schedule(mSerialPortDataRequestTask, 1000, 2000);
//
//        return true;
    }

    public boolean launchUdp(Settings settings) {
        if (settings.isUdpEnable()) {
            if (mUdpKit == null) {
                mUdpKit = new UdpKit();
                mUdpKit.startListen(true, this);
            }
            if (!mUdpKit.launch(0)) {
                return false;
            }
            mUdpKit.startListen(true, this);
            if (mUdpDataRequestTask == null) {
                mUdpDataRequestTask = new UdpDataRequestTask();
            }
            if (mUdpProtocol == null) {
                mUdpProtocol = new ScoutUdpSensorProtocol();
            }
            mUdpDataRequestTask.setTargetIp(settings.getBaseStationIp());
            mUdpDataRequestTask.setTargetPort(settings.getBaseStationPort());
            mUdpDataRequestTask.setDataRequestFrame(mUdpProtocol.makeDataRequestFrame());
            if (mDataRequestTimer == null) {
                mDataRequestTimer = new Timer();
            }
            mDataRequestTimer.schedule(mUdpDataRequestTask, 0, settings.getUdpDataRequestCycle());
        }
        return true;
    }

    public boolean launchBle(Settings settings) {
        if (settings.isBleEnable()) {
            if (mBleKit == null) {
                mBleKit = new BleKit();
            }
            if (!mBleKit.launch(this)) {
                return false;
            }
            if (mBleProtocol == null) {
                mBleProtocol = new ScoutBleSensorProtocol();
            }
            mBleKit.startScan(this, settings.getBleScanCycle(), settings.getBleScanDuration());
        }
        return true;
    }

    public boolean launchSerialPort(Settings settings) {
        if (settings.isSerialPortEnable()) {
            if (!SerialPortProcessor.processPreLaunch()) {
                return false;
            }
            if (mSerialPortKit == null) {
                mSerialPortKit = new SerialPortKit();
            }
            if (!mSerialPortKit.launch(settings.getSerialPortName(), settings.getSerialPortBaudRate(), 0)) {
                return false;
            }
            if (mSerialPortProtocol == null) {
                mSerialPortProtocol = new ScoutUdpSensorProtocol();
            }
            mSerialPortKit.startListen(this);
            if (mSerialPortDataRequestTask == null) {
                mSerialPortDataRequestTask = new SerialPortDataRequestTask();
            }
            mSerialPortDataRequestTask.setDataRequestFrame(mSerialPortProtocol.makeDataRequestFrame());
            if (mDataRequestTimer == null) {
                mDataRequestTimer = new Timer();
            }
            mDataRequestTimer.schedule(mSerialPortDataRequestTask, 0, settings.getSerialPortDataRequestCycle());
        }
        return true;
    }

    public void shutdownCommunicators() {
        shutdownUdp();
        shutdownBle();
        shutdownSerialPort();
        shutdownDataRequestTimer();
//        mUdpKit.close();
//        mBleKit.stopScan();
        //mDataRequestTimer.cancel();
        //mSerialPortKit.shutdown();
        //powerOffSerialPort();
    }

    public void shutdownUdp() {
        if (mUdpDataRequestTask != null) {
            mUdpDataRequestTask.cancel();
            mUdpDataRequestTask = null;
        }
        if (mUdpKit != null) {
            mUdpKit.close();
            mUdpKit = null;
        }
        if (mUdpProtocol != null) {
            mUdpProtocol = null;
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
        if (mSerialPortDataRequestTask != null) {
            mSerialPortDataRequestTask.cancel();
            mSerialPortDataRequestTask = null;
        }
        if (mSerialPortKit != null) {
            mSerialPortKit.shutdown();
            mSerialPortKit = null;
        }
        if (mSerialPortProtocol != null) {
            mSerialPortProtocol = null;
        }
        SerialPortProcessor.processPostShutdown();
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
        printCommunicationData(sensorAddress, dataTypeValue, dataTypeIndex);
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
        if (mSensorDataProcessor == null) {
            mSensorDataProcessor = new SensorDataProcessor(mEventHandler);
        }
        mSensorDataProcessor.setMinTimeIntervalForDuplicateValue(settings.getDefaultSensorDataGatherCycle());
        mSensorDataProcessor.startCaptureAndRecordSensorData();
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

        public void setTargetIp(String ip) {
            try {
                mTargetIp = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                mTargetIp = null;
                ExceptionLog.debug(e);
            }
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
}
