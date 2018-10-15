package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.data.Sorter;
import com.cjq.lib.weisi.iot.Measurement;
import com.cjq.lib.weisi.iot.Sensor;

public class SensorEarliestValueTimeSorter<S extends Sensor> extends Sorter<S> {

    @Override
    public int compare(S s1, S s2) {
        Measurement<?, ?> m1 = s1.getMainMeasurement();
        Measurement<?, ?> m2 = s2.getMainMeasurement();
        int diff = Long.compare(m1.getHistoryValueContainer().getEarliestValue().getTimestamp(),
                m2.getHistoryValueContainer().getEarliestValue().getTimestamp());
        return diff == 0 ? m1.getId().compareTo(m2.getId()) : diff;
    }
}
