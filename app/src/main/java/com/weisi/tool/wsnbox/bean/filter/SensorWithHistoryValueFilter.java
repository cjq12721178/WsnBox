package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/11/13.
 */

public class SensorWithHistoryValueFilter implements Sensor.Filter<PhysicalSensor> {

    @Override
    public boolean isMatch(PhysicalSensor sensor) {
        return sensor.hasHistoryValue();
    }
}
