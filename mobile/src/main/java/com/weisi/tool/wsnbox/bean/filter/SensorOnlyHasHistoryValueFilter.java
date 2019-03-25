package com.weisi.tool.wsnbox.bean.filter;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/11/13.
 */

public class SensorOnlyHasHistoryValueFilter<S extends Sensor> implements Filter<S> {

    @Override
    public boolean match(@NonNull S sensor) {
        return sensor.getMainMeasurement().hasHistoryValue() && !sensor.getMainMeasurement().hasRealTimeValue();
    }
}
