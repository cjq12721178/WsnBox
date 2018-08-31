package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.data.Sorter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorNetInTimeSorter<S extends Sensor> extends Sorter<S> {

//    @Override
//    public int add(List<S> sensors, S sensor) {
//        sensors.add(sensor);
//        return sensors.size() - 1;
//    }

    @Override
    public int compare(S s1, S s2) {
        return Long.compare(s1.getNetInTimestamp(), s2.getNetInTimestamp());
    }
}
