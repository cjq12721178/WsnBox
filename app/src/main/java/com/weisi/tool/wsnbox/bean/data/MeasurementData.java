package com.weisi.tool.wsnbox.bean.data;

import com.cjq.lib.weisi.sensor.MeasurementIdentifier;

import java.util.LinkedList;

/**
 * Created by CJQ on 2017/11/30.
 */

public class MeasurementData {

    private static LinkedList<MeasurementData> measurementDataList = new LinkedList<>();

    private boolean mRecycled;
    private long mId;
    private long mTimestamp;
    private double mRawValue;

    public static MeasurementData build(long id,
                                        long timestamp,
                                        double rawValue) {
        MeasurementData measurementData;
        synchronized (measurementDataList) {
            measurementData = measurementDataList.poll();
            if (measurementData == null) {
                measurementData = new MeasurementData();
            } else {
                measurementData.mRecycled = false;
            }
        }
        measurementData.mId = id;
        measurementData.mTimestamp = timestamp;
        measurementData.mRawValue = rawValue;
        return measurementData;
    }

    private MeasurementData() {
    }

    public long getId() {
        return mId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public double getRawValue() {
        return mRawValue;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public void setRawValue(double rawValue) {
        mRawValue = rawValue;
    }

    public void recycle() {
        if (mRecycled) {
            return;
        }
        synchronized (measurementDataList) {
            measurementDataList.addLast(this);
            mRecycled = true;
        }
    }
}
