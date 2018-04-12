package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.iot.PhysicalSensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorEarliestValueTimeSorter extends SensorSorter<PhysicalSensor> {

    @Override
    public int compare(PhysicalSensor s1, PhysicalSensor s2) {
        return Long.compare(s1.getHistoryValueContainer().getEarliestValue().getTimestamp(),
                s2.getHistoryValueContainer().getEarliestValue().getTimestamp());
//        long t1 = s1.getEarliestHistoryValue().getTimestamp();
//        long t2 = s2.getEarliestHistoryValue().getTimestamp();
//        return (t1 < t2) ? -1 : ((t1 == t2) ? 0 : 1);
    }
}
