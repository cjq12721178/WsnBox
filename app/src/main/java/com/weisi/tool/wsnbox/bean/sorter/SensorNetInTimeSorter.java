package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.node.Sensor;

import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorNetInTimeSorter extends SensorSorter {

    @Override
    public int add(List<Sensor> sensors, Sensor sensor) {
        sensors.add(sensor);
        return sensors.size() - 1;
    }

    @Override
    public int compare(Sensor s1, Sensor s2) {
        long t1 = s1.getNetInTimestamp();
        long t2 = s2.getNetInTimestamp();
        return (t1 < t2) ? -1 : ((t1 == t2) ? 0 : 1);
    }
}
