package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.sensor.Filter;
import com.cjq.lib.weisi.sensor.Sensor;

/**
 * Created by CJQ on 2017/9/19.
 */

public class SensorUseForRealtimeFilter implements Filter {

    @Override
    public boolean isMatch(Sensor sensor) {
        return sensor.getNetInTimestamp() != 0;
    }
}
