package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/11/13.
 */

public class SensorWithHistoryValueFilter<S extends Sensor> implements Filter<S> {

    @Override
    public boolean match(S sensor) {
        return sensor.hasHistoryValue();
    }
}
