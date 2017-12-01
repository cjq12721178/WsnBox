package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.sensor.Filter;
import com.cjq.lib.weisi.sensor.Sensor;

/**
 * Created by CJQ on 2017/11/10.
 */

public class HistoryDataFilter implements Filter {

    @Override
    public boolean isMatch(Sensor sensor) {
        return sensor.getHistoryValueSize() > 0;
    }
}
