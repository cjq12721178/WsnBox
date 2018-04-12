package com.weisi.tool.wsnbox.processor.exporter;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.Sensor;
import com.weisi.tool.wsnbox.bean.data.SensorData;
import com.weisi.tool.wsnbox.io.database.SensorDatabase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by CJQ on 2017/11/9.
 */

public class SensorDataExporter
        implements Runnable,
        SensorDatabase.SensorDataProvider,
        Sensor.OnDynamicValueCaptureListener {

    public static final int DATABASE_INSERT_SENSOR_DATA_ERROR = 1;
    public static final int SENSOR_DATA_RECORDER_SHUTDOWN = 2;
    
    private static final int MAX_AFFORDABLE_ERROR_TIMES = 10;

    private final int MAX_BUFFER_SIZE = 100;
    private final Handler mMessageSender;
    private long mMinTimeIntervalForDuplicateValue;
    private boolean mSensorDataRecording;
    private LinkedList<SensorData> mTemporarySensorData;
    private Map<Long, SensorData> mLastSensorDataMap;

    public SensorDataExporter(@NonNull Handler messageSender) {
        if (messageSender == null) {
            throw new NullPointerException("message sender may not be null");
        }
        mMessageSender = messageSender;
    }

    public void setMinTimeIntervalForDuplicateValue(long gatherCycle) {
        mMinTimeIntervalForDuplicateValue = gatherCycle;
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
        if (mTemporarySensorData == null) {
            mTemporarySensorData = new LinkedList<>();
        } else {
            mTemporarySensorData.clear();
        }
        if (mLastSensorDataMap == null) {
            mLastSensorDataMap = new HashMap<>();
        } else {
            mLastSensorDataMap.clear();
        }
        Sensor.setOnDynamicValueCaptureListener(this);
        final long MAX_RECORD_TIME_INTERVAL = 30000;
        long currentTime, lastRecordTime = System.currentTimeMillis();
        int continuousErrorTimes = 0;
        while (mSensorDataRecording) {
            currentTime = System.currentTimeMillis();
            while (mSensorDataRecording
                    && (currentTime - lastRecordTime >= MAX_RECORD_TIME_INTERVAL
                    || mTemporarySensorData.size() >= MAX_BUFFER_SIZE)) {
                if (SensorDatabase.batchSaveSensorData(this)) {
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
            SensorDatabase.batchSaveSensorData(this);
        }
        mTemporarySensorData.clear();
        mLastSensorDataMap.clear();
        mSensorDataRecording = false;
        mMessageSender.sendEmptyMessage(SENSOR_DATA_RECORDER_SHUTDOWN);
    }

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
        if (lastData.getTimestamp() + mMinTimeIntervalForDuplicateValue
                > data.getTimestamp()) {
            return SensorDatabase.SensorDataProvider.DUPLICATE_DATA;
        }
        lastData.setTimestamp(data.getTimestamp());
        lastData.setBatteryVoltage(data.getBatteryVoltage());
        lastData.setRawValue(data.getRawValue());
        return SensorDatabase.SensorDataProvider.NORMAL_DATA;
    }

    @Override
    public void onDynamicValueCapture(int address, byte dataTypeValue, int dataTypeValueIndex, long timestamp, float batteryVoltage, double rawValue) {
        mTemporarySensorData.addLast(SensorData.build(address,
                dataTypeValue, dataTypeValueIndex,
                timestamp, batteryVoltage, rawValue));
    }
}
