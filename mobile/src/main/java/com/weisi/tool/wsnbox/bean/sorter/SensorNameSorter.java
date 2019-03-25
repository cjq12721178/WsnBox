package com.weisi.tool.wsnbox.bean.sorter;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2018/4/17.
 */

public class SensorNameSorter<S extends Sensor> extends SensorIdSorter<S> {

    @Override
    public int compare(@NonNull S s1, @NonNull S s2) {
        int result = s1.getMainMeasurement().getName().compareTo(s2.getMainMeasurement().getName());
        return result == 0 ? super.compare(s1, s2) : result;
    }
}
