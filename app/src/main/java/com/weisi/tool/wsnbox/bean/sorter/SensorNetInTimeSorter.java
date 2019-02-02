package com.weisi.tool.wsnbox.bean.sorter;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorNetInTimeSorter<S extends Sensor> extends SensorIdSorter<S> {

//    @Override
//    public int add(List<S> sensors, S measurement) {
//        sensors.add(measurement);
//        return sensors.size() - 1;
//    }

    @Override
    public int compare(@NonNull S s1, @NonNull S s2) {
        int result = Long.compare(s1.getNetInTimestamp(), s2.getNetInTimestamp());
        return result == 0 ? super.compare(s1, s2) : result;
    }
}
