package com.weisi.tool.wsnbox.bean.storage;

import com.cjq.lib.weisi.sensor.Filter;
import com.weisi.tool.wsnbox.bean.filter.SensorUseForRealtimeFilter;
import com.weisi.tool.wsnbox.bean.filter.SensorWithHistoryValueFilter;

/**
 * Created by CJQ on 2018/1/15.
 */

public class DataBrowseSensorStorage extends BaseSensorStorage {

    private Filter mDataSourceFilter;

    public DataBrowseSensorStorage setDataSource(boolean isRealTime, OnSensorSizeChangeListener listener) {
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
            commitFilter(listener);
        }
        return this;
    }
}
