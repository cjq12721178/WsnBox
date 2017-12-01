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
import com.weisi.tool.wsnbox.io.SensorDatabase;
import com.weisi.tool.wsnbox.processor.SensorDataProcessor;
import com.weisi.tool.wsnbox.util.Tag;

import java.io.IOException;

public class DataPrepareService
        extends Service
        implements UdpKit.OnDataReceivedListener,
        BluetoothAdapter.LeScanCallback,
        OnFrameAnalyzedListener, SerialPortKit.OnDataReceivedListener {

    private final LocalBinder mLocalBinder = new LocalBinder();
    //private final UdpKit mUdpKit = new UdpKit();
    //private final BleKit mBleKit = new BleKit();
    private final SerialPortKit mSerialPortKit = new SerialPortKit();
    private final ScoutUdpSensorProtocol mUdpProtocol = new ScoutUdpSensorProtocol();
    private final ScoutBleSensorProtocol mBleProtocol = new ScoutBleSensorProtocol();
    private final ScoutUdpSensorProtocol mSerialPortProtocol = new ScoutUdpSensorProtocol();
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

    public boolean importSensorConfigurations() {
        return ConfigurationManager.importEthernetConfiguration(this)
                && ConfigurationManager.importBleConfiguration(this);
    }

    public boolean launchCommunicators() {
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

        if (!powerOnSerialPort()) {
            return false;
        }
        if (!mSerialPortKit.launch("ttyHSL1", 115200, 0)) {
            return false;
        }
        mSerialPortKit.startListen(this);

        return true;
    }

    public void shutdownCommunicators() {
//        mUdpKit.close();
//        mBleKit.stopScan();
        mSerialPortKit.shutdown();
        powerOffSerialPort();
    }

    private boolean powerOnSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_gpio_112"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
            return true;
        } catch (IOException e) {
            ExceptionLog.record(e);
        }
        return false;
    }

    private void powerOffSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
            SimpleCustomizeToast.show(this, "power off");
        } catch (IOException e) {
            ExceptionLog.record(e);
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
        if (mSensorDataProcessor == null) {
            mSensorDataProcessor = new SensorDataProcessor(mEventHandler);
        }
        mSensorDataProcessor.startCaptureAndRecordSensorData();
    }

    public void stopCaptureAndRecordSensorData() {
        if (mSensorDataProcessor == null) {
            return;
        }
        mSensorDataProcessor.stopCaptureAndRecordSensorData();
    }
}
