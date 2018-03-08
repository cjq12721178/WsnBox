package com.weisi.tool.wsnbox.bean.data;

import java.util.LinkedList;

/**
 * Created by CJQ on 2017/11/30.
 */

public class SensorData {

    private static LinkedList<SensorData> mRecycleBin = new LinkedList<>();

    private boolean mRecycled;
    private int mAddress;
    private long mId;
    private long mTimestamp;
    private float mBatteryVoltage;
    private double mRawValue;

    public static SensorData build(SensorData src) {
        return build(src.mAddress,
                src.mId,
                src.mTimestamp,
                src.mBatteryVoltage,
                src.mRawValue);
    }

    public static SensorData build(int address,
                                   long id,
                                   long timestamp,
                                   float batteryVoltage,
                                   double rawValue) {
        SensorData sensorData;
        synchronized (mRecycleBin) {
            sensorData = mRecycleBin.poll();
            if (sensorData == null) {
                sensorData = new SensorData();
            } else {
                sensorData.mRecycled = false;
            }
        }
        sensorData.mAddress = address;
        sensorData.mId = id;
        sensorData.mTimestamp = timestamp;
        sensorData.mBatteryVoltage = batteryVoltage;
        sensorData.mRawValue = rawValue;
        return sensorData;
    }

    private SensorData() {
    }

    public int getAddress() {
        return mAddress;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public float getBatteryVoltage() {
        return mBatteryVoltage;
    }

    public long getId() {
        return mId;
    }

    public double getRawValue() {
        return mRawValue;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public void setBatteryVoltage(float batteryVoltage) {
        mBatteryVoltage = batteryVoltage;
    }

    public void setRawValue(double rawValue) {
        mRawValue = rawValue;
    }

    public void recycle() {
        if (mRecycled) {
            return;
        }
        synchronized (mRecycleBin) {
            mRecycleBin.addLast(this);
            mRecycled = true;
        }
    }
}
