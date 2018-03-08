package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.node.Sensor;

/**
 * Created by CJQ on 2017/11/13.
 */

public class SensorWithHistoryValueFilter implements Sensor.Filter {

    @Override
    public boolean isMatch(Sensor sensor) {
        return sensor.hasHistoryValue();
    }
}
