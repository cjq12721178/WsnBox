package com.weisi.tool.wsnbox.processor;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.sensor.MeasurementIdentifier;
import com.cjq.lib.weisi.sensor.Sensor;
import com.weisi.tool.wsnbox.bean.data.SensorData;
import com.weisi.tool.wsnbox.io.SensorDatabase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by CJQ on 2017/11/9.
 */

public class SensorDataProcessor
        implements Runnable,
        SensorDatabase.SensorDataProvider,
        Sensor.OnDynamicValueCaptureListener {

    public static final int DATABASE_INSERT_SENSOR_DATA_ERROR = 1;
    public static final int SENSOR_DATA_RECORDER_SHUTDOWN = 2;
    
    private static final int MAX_AFFORDABLE_ERROR_TIMES = 10;

    private final int MAX_BUFFER_SIZE = 100;
    private final long MIN_TIME_INTERVAL_FOR_DUPLICATE_VALUE = 60000;
    private final Handler mMessageSender;
    private boolean mSensorDataRecording;
    private LinkedList<SensorData> mTemporarySensorData;
    //private LinkedList<MeasurementData> mTemporaryMeasurementData;
    private Map<Long, SensorData> mLastSensorDataMap;
    //private Map<Long, MeasurementData> mLastMeasurementValueMap;

    public SensorDataProcessor(@NonNull Handler messageSender) {
        if (messageSender == null) {
            throw new NullPointerException("message sender may not be null");
        }
        mMessageSender = messageSender;
    }

    public void startCaptureAndRecordSensorData() {
        if (mSensorDataRecording) {
            return;
        }
        mSensorDataRecording = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stopCaptureAndRecordSensorData() {
        if (!mSensorDataRecording) {
            return;
        }
        mSensorDataRecording = false;
    }

    @Override
    public void run() {
        mTemporarySensorData = new LinkedList<>();
        //mTemporaryMeasurementData = new LinkedList<>();
        mLastSensorDataMap = new HashMap<>();
        //mLastMeasurementValueMap = new HashMap<>();
        Sensor.setOnDynamicValueCaptureListener(this);
        final long MAX_RECORD_TIME_INTERVAL = 30000;
        long currentTime, lastRecordTime = System.currentTimeMillis();
        int continuousErrorTimes = 0;
        while (mSensorDataRecording) {
            currentTime = System.currentTimeMillis();
//            if (currentTime - lastRecordTime >= MAX_RECORD_TIME_INTERVAL
//                    || mTemporarySensorData.size() >= MAX_BUFFER_SIZE) {
//                lastRecordTime = currentTime;
//                SensorDatabase.batchSaveSensorInfo(this);
//            }
            while (mSensorDataRecording
                    && (currentTime - lastRecordTime >= MAX_RECORD_TIME_INTERVAL
                    || mTemporarySensorData.size() >= MAX_BUFFER_SIZE)) {
                if (SensorDatabase.batchSaveSensorInfo(this)) {
                    continuousErrorTimes = 0;
                    lastRecordTime = currentTime;
                } else if (++continuousErrorTimes > MAX_AFFORDABLE_ERROR_TIMES) {
                    mSensorDataRecording = false;
                    mMessageSender.sendEmptyMessage(DATABASE_INSERT_SENSOR_DATA_ERROR);
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Sensor.setOnDynamicValueCaptureListener(null);
        if (mTemporarySensorData.size() > 0) {
            SensorDatabase.batchSaveSensorInfo(this);
        }
        mTemporarySensorData.clear();
        mTemporarySensorData = null;
        //mTemporaryMeasurementData.clear();
        //mTemporaryMeasurementData = null;
        mLastSensorDataMap.clear();
        mLastSensorDataMap = null;
        //mLastMeasurementValueMap.clear();
        //mLastMeasurementValueMap = null;
        mSensorDataRecording = false;
//        if (continuousErrorTimes == -1) {
//            mMessageSender.sendEmptyMessage(DATABASE_INSERT_SENSOR_DATA_ERROR);
//        }
        mMessageSender.sendEmptyMessage(SENSOR_DATA_RECORDER_SHUTDOWN);
    }

//    @Override
//    public void run() {
//        mTemporarySensorData = new LinkedList<>();
//        mTemporaryMeasurementData = new LinkedList<>();
//        mLastSensorDataMap = new HashMap<>();
//        mLastMeasurementValueMap = new HashMap<>();
//        Sensor.setOnDynamicValueCaptureListener(this);
//        final long MAX_RECORD_TIME_INTERVAL = 60000;
//        long currentTime, lastRecordTime = System.currentTimeMillis();
//        int continuousErrorTimes = 0;
//        while (mSensorDataRecording) {
//            currentTime = System.currentTimeMillis();
//            while (mSensorDataRecording
//                    || currentTime - lastRecordTime >= MAX_RECORD_TIME_INTERVAL
//                    || mTemporarySensorData.size() >= MAX_BUFFER_SIZE) {
//                lastRecordTime = currentTime;
//                if (SensorDatabase.batchSaveSensorInfo(this)) {
//                    continuousErrorTimes = 0;
//                } else {
//                    if (++continuousErrorTimes > 5) {
//                        mSensorDataRecording = false;
//                        continuousErrorTimes = -1;
//                    }
//                }
//            }
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Sensor.setOnDynamicValueCaptureListener(null);
//        if (mTemporarySensorData.size() > 0) {
//            SensorDatabase.batchSaveSensorInfo(this);
//        }
//        mTemporarySensorData.clear();
//        mTemporarySensorData = null;
//        mTemporaryMeasurementData.clear();
//        mTemporaryMeasurementData = null;
//        mLastSensorDataMap.clear();
//        mLastSensorDataMap = null;
//        mLastMeasurementValueMap.clear();
//        mLastMeasurementValueMap = null;
//        mSensorDataRecording = false;
//        if (continuousErrorTimes == -1) {
//            mMessageSender.sendEmptyMessage(DATABASE_INSERT_SENSOR_DATA_ERROR);
//        }
//        mMessageSender.sendEmptyMessage(SENSOR_DATA_RECORDER_SHUTDOWN);
//    }

    @Override
    public int getSensorDataCount() {
        return mTemporarySensorData.size();
    }

    @Override
    public SensorData provideSensorData() {
        return mTemporarySensorData.pollFirst();
    }

    @Override
    public int getSensorDataState(SensorData data) {
        SensorData lastData = mLastSensorDataMap.get(data.getId());
        if (lastData == null) {
            mLastSensorDataMap.put(data.getId(), SensorData.build(data));
            return SensorDatabase.SensorDataProvider.FIRST_DATA;
        }
//        if (lastData.getRawValue() != data.getRawValue()) {
//            lastData.setTimestamp(data.getTimestamp());
//            lastData.setRawValue(data.getRawValue());
//            return SensorDatabase.SensorDataProvider.NORMAL_DATA;
//        }
        if (lastData.getTimestamp() + MIN_TIME_INTERVAL_FOR_DUPLICATE_VALUE
                > data.getTimestamp()) {
            return SensorDatabase.SensorDataProvider.DUPLICATE_DATA;
        }
        lastData.setTimestamp(data.getTimestamp());
        lastData.setBatteryVoltage(data.getBatteryVoltage());
        lastData.setRawValue(data.getRawValue());
        return SensorDatabase.SensorDataProvider.NORMAL_DATA;
//        SensorData lastData = mLastSensorDataMap.get(data.getAddress());
//        if (lastData == null) {
//            mLastSensorDataMap.put(data.getAddress(),
//                    SensorData.build(data.getAddress(),
//                            data.getTimestamp(),
//                            data.getBatteryVoltage()));
//            return SensorDatabase.SensorDataProvider.FIRST_DATA;
//        }
//        if (lastData.getBatteryVoltage() != data.getBatteryVoltage()) {
//            lastData.setTimestamp(data.getTimestamp());
//            lastData.setBatteryVoltage(data.getBatteryVoltage());
//            return SensorDatabase.SensorDataProvider.NORMAL_DATA;
//        }
//        if (lastData.getTimestamp() + MIN_TIME_INTERVAL_FOR_DUPLICATE_VALUE
//                > data.getTimestamp()) {
//            return SensorDatabase.SensorDataProvider.DUPLICATE_DATA;
//        }
//        lastData.setTimestamp(data.getTimestamp());
//        return SensorDatabase.SensorDataProvider.NORMAL_DATA;
    }

//    @Override
//    public int getMeasurementDataCount() {
//        return mTemporaryMeasurementData.size();
//    }
//
//    @Override
//    public MeasurementData provideMeasurementData() {
//        return mTemporaryMeasurementData.pollFirst();
//    }
//
//    @Override
//    public int getMeasurementDataState(MeasurementData data) {
//        MeasurementData lastData = mLastMeasurementValueMap.get(data.getId());
//        if (lastData == null) {
//            mLastMeasurementValueMap.put(data.getId(),
//                    MeasurementData.build(data.getId(),
//                            data.getTimestamp(),
//                            data.getRawValue()));
//            return SensorDatabase.SensorDataProvider.FIRST_DATA;
//        }
//        if (lastData.getRawValue() != data.getRawValue()) {
//            lastData.setTimestamp(data.getTimestamp());
//            lastData.setRawValue(data.getRawValue());
//            return SensorDatabase.SensorDataProvider.NORMAL_DATA;
//        }
//        if (lastData.getTimestamp() + MIN_TIME_INTERVAL_FOR_DUPLICATE_VALUE
//                > data.getTimestamp()) {
//            return SensorDatabase.SensorDataProvider.DUPLICATE_DATA;
//        }
//        lastData.setTimestamp(data.getTimestamp());
//        return SensorDatabase.SensorDataProvider.NORMAL_DATA;
//    }

    @Override
    public void onDynamicValueCapture(int address, byte dataTypeValue, int dataTypeValueIndex, long timestamp, float batteryVoltage, double rawValue) {
        mTemporarySensorData.addLast(SensorData.build(address,
                MeasurementIdentifier.getId(address,
                        dataTypeValue, dataTypeValueIndex),
                timestamp, batteryVoltage, rawValue));
    }
}
