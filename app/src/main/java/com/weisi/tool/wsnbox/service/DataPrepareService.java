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
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.io.SensorDatabase;
import com.weisi.tool.wsnbox.processor.SensorDataProcessor;
import com.weisi.tool.wsnbox.util.Tag;

public class DataPrepareService
        extends Service
        implements UdpKit.OnDataReceivedListener,
        BluetoothAdapter.LeScanCallback,
        OnFrameAnalyzedListener {

    private final LocalBinder mLocalBinder = new LocalBinder();
    private final UdpKit mUdpKit = new UdpKit();
    private final BleKit mBleKit = new BleKit();
    private final ScoutUdpSensorProtocol mUdpProtocol = new ScoutUdpSensorProtocol();
    private final ScoutBleSensorProtocol mBleProtocol = new ScoutBleSensorProtocol();
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
        if (!mUdpKit.launch(0)) {
            return false;
        }
        mUdpKit.startListen(true, this);
        mUdpKit.sendData("192.168.1.18", 5000, mUdpProtocol.makeDataRequestFrame(), 500);

        if (!mBleKit.launch(this)) {
            return false;
        }
        mBleKit.startScan(this, 5000, 10000);
        return true;
    }

    public void shutdownCommunicators() {
        mUdpKit.close();
        mBleKit.stopScan();
    }

    @Override
    public void onDataReceived(byte[] data) {
        mUdpProtocol.analyze(data, this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mBleProtocol.analyze(device.getAddress(), scanRecord, this);
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
