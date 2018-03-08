package com.weisi.tool.wsnbox.bean.storage;

import com.cjq.lib.weisi.node.Sensor;
import com.cjq.lib.weisi.node.SensorManager;
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

    public int getSensorSize() {
        return mSensors.size();
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

    public BaseSensorStorage setSorter(SensorSorter sorter, boolean isDescend) {
        return setSorter(sorter, isDescend, false, null);
    }

    public BaseSensorStorage setSorter(SensorSorter sorter, boolean isDescend, OnSensorSorterChangeListener listener) {
        return setSorter(sorter, isDescend, true, listener);
    }

    protected BaseSensorStorage setSorter(SensorSorter sorter, boolean isDescend, boolean isCommit, OnSensorSorterChangeListener listener) {
        if (mSensorSorter != sorter) {
            mSensorSorter = sorter;
            mIsDescend = isDescend;
            if (isCommit) {
                resort();
                notifySorterChangeListener(listener);
            }
        } else {
            if (mIsDescend != isDescend) {
                mIsDescend = isDescend;
                if (isCommit) {
                    notifyOrderChangeListener(listener);
                }
            }
        }
        return this;
    }

    protected void resort() {
        if (mSensorSorter != null) {
            mSensorSorter.sort(mSensors);
        }
    }

    private void notifySorterChangeListener(OnSensorSorterChangeListener listener) {
        if (listener != null) {
            listener.onSorterChange(mSensorSorter);
        }
    }

    private void notifyOrderChangeListener(OnSensorSorterChangeListener listener) {
        if (listener != null) {
            listener.onOrderChange(mIsDescend);
        }
    }

    public BaseSensorStorage addFilter(Sensor.Filter filter) {
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

    public BaseSensorStorage removeFilter(Sensor.Filter filter) {
        getSensorFilters().remove(filter);
        return this;
    }

    public void commitFilter(OnSensorFilterChangeListener listener) {
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

    protected SensorSorter getSensorSorter() {
        return mSensorSorter;
    }

    public boolean getSensorOrder() {
        return mIsDescend;
    }

    public interface OnSensorSorterChangeListener {
        void onSorterChange(SensorSorter newSorter);
        void onOrderChange(boolean newOrder);
    }

    public interface OnSensorFilterChangeListener {
        void onSensorSizeChange(int previousSize, int currentSize);
    }
}
