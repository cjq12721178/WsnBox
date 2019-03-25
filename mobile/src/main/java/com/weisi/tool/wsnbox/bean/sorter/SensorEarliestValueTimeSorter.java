package com.weisi.tool.wsnbox.bean.sorter;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.Measurement;
import com.cjq.lib.weisi.iot.Sensor;

public class SensorEarliestValueTimeSorter<S extends Sensor> extends SensorIdSorter<S> {

    @Override
    public int compare(@NonNull S s1, @NonNull S s2) {
        Measurement<?, ?> m1 = s1.getMainMeasurement();
        Measurement<?, ?> m2 = s2.getMainMeasurement();
        int diff = Long.compare(m1.getHistoryValueContainer().getEarliestValue().getTimestamp(),
                m2.getHistoryValueContainer().getEarliestValue().getTimestamp());
        return diff == 0 ? super.compare(s1, s2) : diff;
    }
}
