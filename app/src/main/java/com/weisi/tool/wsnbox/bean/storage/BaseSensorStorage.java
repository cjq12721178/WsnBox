package com.weisi.tool.wsnbox.bean.storage;

import com.cjq.lib.weisi.sensor.Filter;
import com.cjq.lib.weisi.sensor.Sensor;
import com.cjq.lib.weisi.sensor.SensorManager;
import com.weisi.tool.wsnbox.bean.filter.FilterCollection;
import com.weisi.tool.wsnbox.bean.sorter.SensorSorter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2018/1/15.
 */

public class BaseSensorStorage {

    private final List<Sensor> mSensors;
    private FilterCollection mSensorFilters;
    private SensorSorter mSensorSorter;
    private boolean mIsDescend;

    public BaseSensorStorage() {
        mSensors = new ArrayList<>();
    }

    public Sensor getSensor(int position) {
        return mSensors.get(getSensorDisplayPosition(position));
    }

    //逻辑位置转视觉位置
    private int getSensorDisplayPosition(int logicalPosition) {
        return mIsDescend
                ? mSensors.size() - 1 - logicalPosition
                : logicalPosition;
    }

    public BaseSensorStorage setSorter(SensorSorter sorter, boolean isDescend, OnSorterChangeListener listener) {
        if (mSensorSorter != sorter) {
            mSensorSorter = sorter;
            mIsDescend = isDescend;
            resort();
            notifySorterChangeListener(listener);
        } else {
            if (mIsDescend != isDescend) {
                mIsDescend = isDescend;
                notifyOrderChangeListener(listener);
            }
        }
        return this;
    }

    protected void resort() {
        if (mSensorSorter != null) {
            mSensorSorter.sort(mSensors);
        }
    }

    private void notifySorterChangeListener(OnSorterChangeListener listener) {
        if (listener != null) {
            listener.onSorterChange(mSensorSorter);
        }
    }

    private void notifyOrderChangeListener(OnSorterChangeListener listener) {
        if (listener != null) {
            listener.onOrderChange(mIsDescend);
        }
    }

    public BaseSensorStorage addFilter(Filter filter) {
        if (filter != null) {
            getSensorFilters().add(filter);
        }
        return this;
    }

    private FilterCollection getSensorFilters() {
        if (mSensorFilters == null) {
            mSensorFilters = new FilterCollection();
        }
        return mSensorFilters;
    }

    public BaseSensorStorage removeFilter(Filter filter) {
        getSensorFilters().remove(filter);
        return this;
    }

    public void commitFilter(OnSensorSizeChangeListener listener) {
        int previousSize = mSensors.size();
        mSensors.clear();
        SensorManager.getSensors(mSensors, mSensorFilters);
        int currentSize = mSensors.size();
        resort();
        if (listener != null) {
            listener.onSensorSizeChange(previousSize, currentSize);
        }
    }

    public int addSensor(Sensor sensor) {
        int position;
        if (match(sensor)) {
            if (mSensorSorter != null) {
                position = mSensorSorter.add(mSensors, sensor);
            } else {
                if (mSensors.add(sensor)) {
                    position = getSensorDisplayPosition(mSensors.size() - 1);
                } else {
                    position = -1;
                }
            }
        } else {
            position = -1;
        }
        return position;
    }

    private boolean match(Sensor sensor) {
        return mSensorFilters != null
                ? mSensorFilters.isMatch(sensor)
                : true;
    }

    public int findSensor(Sensor sensor) {
        if (mSensorSorter != null) {
            return mSensorSorter.find(mSensors, sensor);
        } else {
            return mSensors.indexOf(sensor);
        }
    }

    public interface OnSorterChangeListener {
        void onSorterChange(SensorSorter newSorter);
        void onOrderChange(boolean newOrder);
    }

    public interface OnSensorSizeChangeListener {
        void onSensorSizeChange(int previousSize, int currentSize);
    }
}
