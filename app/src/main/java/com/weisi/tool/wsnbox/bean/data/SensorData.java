package com.weisi.tool.wsnbox.bean.data;

import com.cjq.lib.weisi.iot.Sensor;

import java.util.LinkedList;

/**
 * Created by CJQ on 2017/11/30.
 */

public class SensorData {

    private static LinkedList<SensorData> mRecycleBin = new LinkedList<>();

    private boolean mRecycled;
    private int mAddress;
    //private long mId;
    private byte mDataTypeValue;
    private int mDataTypeValueIndex;
    private long mTimestamp;
    private float mBatteryVoltage;
    private double mRawValue;

    public static SensorData build(long sensorId,
                                   long timestamp,
                                   float batteryVoltage,
                                   double rawValue) {
        return build(Sensor.ID.getAddress(sensorId),
                Sensor.ID.getDataTypeValue(sensorId),
                Sensor.ID.getDataTypeValueIndex(sensorId),
                timestamp,
                batteryVoltage,
                rawValue);
    }

    public static SensorData build(SensorData src) {
        return build(src.mAddress,
                src.mDataTypeValue,
                src.mDataTypeValueIndex,
                src.mTimestamp,
                src.mBatteryVoltage,
                src.mRawValue);
    }

    public static SensorData build(int address,
                                   byte dataTypeValue,
                                   int dataTypeValueIndex,
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
        sensorData.mDataTypeValue = dataTypeValue;
        sensorData.mDataTypeValueIndex = dataTypeValueIndex;
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

    public byte getDataTypeValue() {
        return mDataTypeValue;
    }

    public int getDataTypeValueIndex() {
        return mDataTypeValueIndex;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public float getBatteryVoltage() {
        return mBatteryVoltage;
    }

    public long getId() {
        return Sensor.ID.getId(mAddress, mDataTypeValue, mDataTypeValueIndex);
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
