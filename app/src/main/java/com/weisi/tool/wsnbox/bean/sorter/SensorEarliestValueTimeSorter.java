package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.data.Sorter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorEarliestValueTimeSorter<S extends Sensor> extends Sorter<S> {

    @Override
    public int compare(S s1, S s2) {
        int diff = Long.compare(s1.getHistoryValueContainer().getEarliestValue().getTimestamp(),
                s2.getHistoryValueContainer().getEarliestValue().getTimestamp());
        return diff == 0 ? s1.getId().compareTo(s2.getId()) : diff;
//        long t1 = s1.getEarliestHistoryValue().getTimestamp();
//        long t2 = s2.getEarliestHistoryValue().getTimestamp();
//        return (t1 < t2) ? -1 : ((t1 == t2) ? 0 : 1);
    }
}
