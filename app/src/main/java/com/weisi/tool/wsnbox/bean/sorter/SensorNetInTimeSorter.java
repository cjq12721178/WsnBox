package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.iot.PhysicalSensor;

import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorNetInTimeSorter extends SensorSorter<PhysicalSensor> {

    @Override
    public int add(List<PhysicalSensor> sensors, PhysicalSensor sensor) {
        sensors.add(sensor);
        return sensors.size() - 1;
    }

    @Override
    public int compare(PhysicalSensor s1, PhysicalSensor s2) {
        return Long.compare(s1.getNetInTimestamp(), s2.getNetInTimestamp());
//        long t1 = s1.getNetInTimestamp();
//        long t2 = s2.getNetInTimestamp();
//        return (t1 < t2) ? -1 : ((t1 == t2) ? 0 : 1);
    }
}
