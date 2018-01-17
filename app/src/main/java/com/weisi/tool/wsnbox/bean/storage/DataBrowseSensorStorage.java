package com.weisi.tool.wsnbox.bean.storage;

import com.cjq.lib.weisi.sensor.Filter;
import com.weisi.tool.wsnbox.bean.filter.SensorUseForRealtimeFilter;
import com.weisi.tool.wsnbox.bean.filter.SensorWithHistoryValueFilter;
import com.weisi.tool.wsnbox.bean.sorter.SensorAddressSorter;
import com.weisi.tool.wsnbox.bean.sorter.SensorEarliestValueTimeSorter;
import com.weisi.tool.wsnbox.bean.sorter.SensorNetInTimeSorter;
import com.weisi.tool.wsnbox.bean.sorter.SensorSorter;

/**
 * Created by CJQ on 2018/1/15.
 * 注意，本类中凡是方法中带listener的都会自动commit，否则需手动commit
 */

public class DataBrowseSensorStorage extends BaseSensorStorage {

    public static final int FROM_BLE_ONLY = 1;
    public static final int FROM_UDP_ONLY = 2;
    public static final int FROM_BOTH = 3;

    public static final int SORTED_BY_ADDRESS = 1;
    public static final int SORTED_BY_TIME = 2;

    private Filter mDataSourceFilter;

    public DataBrowseSensorStorage setDataSource(boolean isRealTime) {
        return setDataSource(isRealTime, false, null);
    }

    public DataBrowseSensorStorage setDataSource(boolean isRealTime, OnSensorDataSourceChangeListener listener) {
        return setDataSource(isRealTime, true, listener);
    }

    private DataBrowseSensorStorage setDataSource(boolean isRealTime, boolean isCommit, OnSensorDataSourceChangeListener listener) {
        Filter oldDataSourceFilter = mDataSourceFilter;
        if (isRealTime) {
            if (!(mDataSourceFilter instanceof SensorUseForRealtimeFilter)) {
                mDataSourceFilter = new SensorUseForRealtimeFilter();
            }
        } else {
            if (!(mDataSourceFilter instanceof SensorWithHistoryValueFilter)) {
                mDataSourceFilter = new SensorWithHistoryValueFilter();
            }
        }
        boolean changed = oldDataSourceFilter != mDataSourceFilter;
        if (changed) {
            removeFilter(oldDataSourceFilter);
            addFilter(mDataSourceFilter);
            setSorter(getSensorSortType(), getSensorOrder(), isRealTime);
            if (isCommit) {
                commitFilter(listener);
                if (listener != null) {
                    listener.onDataSourceChange(isRealTime);
                }
            }
        }
        return this;
    }

    public int getSensorSortType() {
        SensorSorter sorter = getSensorSorter();
        if (sorter instanceof SensorNetInTimeSorter) {
            return SORTED_BY_TIME;
        } else if (sorter instanceof SensorEarliestValueTimeSorter) {
            return SORTED_BY_TIME;
        } else if (sorter instanceof SensorAddressSorter) {
            return SORTED_BY_ADDRESS;
        }
        return SORTED_BY_TIME;
    }

    public DataBrowseSensorStorage setFilter(boolean isRealTime,
                                             OnSensorFilterChangeListener listener) {
        setDataSource(isRealTime);
        commitFilter(listener);
        return this;
    }

    public DataBrowseSensorStorage setSorter(int type,
                                             boolean isDescend,
                                             boolean isRealTime) {
        return setSorter(type, isDescend, isRealTime, false, null);
    }

    public DataBrowseSensorStorage setSorter(int type,
                                             boolean isDescend,
                                             boolean isRealTime,
                                             OnSensorSorterChangeListener listener) {
        return setSorter(type, isDescend, isRealTime, true, listener);
    }

    private DataBrowseSensorStorage setSorter(int type,
                                              boolean isDescend,
                                              boolean isRealTime,
                                              boolean isCommit,
                                              OnSensorSorterChangeListener listener) {
        switch (type) {
            case SORTED_BY_ADDRESS:
                setSorter(new SensorAddressSorter(), isDescend, isCommit, listener);
                break;
            case SORTED_BY_TIME:
            default:
                if (isRealTime) {
                    setSorter(new SensorNetInTimeSorter(), isDescend, isCommit, listener);
                } else {
                    setSorter(new SensorEarliestValueTimeSorter(), isDescend, isCommit, listener);
                }
                break;
        }
        return this;
    }

    public interface OnSensorDataSourceChangeListener
            extends OnSensorFilterChangeListener, OnSensorSorterChangeListener {
        void onDataSourceChange(boolean isRealTime);
    }
}
