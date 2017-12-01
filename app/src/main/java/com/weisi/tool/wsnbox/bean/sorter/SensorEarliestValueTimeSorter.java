package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.sensor.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorEarliestValueTimeSorter extends SensorSorter {

    @Override
    public int compare(Sensor s1, Sensor s2) {
        long t1 = s1.getHistoryValue(0).getTimeStamp();
        long t2 = s2.getHistoryValue(0).getTimeStamp();
        return (t1 < t2) ? -1 : ((t1 == t2) ? 0 : 1);
    }
}
