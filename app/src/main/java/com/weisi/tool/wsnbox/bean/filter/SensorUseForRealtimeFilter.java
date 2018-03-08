package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.node.Sensor;

/**
 * Created by CJQ on 2017/9/19.
 */

public class SensorUseForRealtimeFilter implements Sensor.Filter {

    @Override
    public boolean isMatch(Sensor sensor) {
        return sensor.getNetInTimestamp() != 0;
    }
}
