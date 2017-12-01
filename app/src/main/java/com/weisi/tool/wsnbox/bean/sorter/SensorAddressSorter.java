package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.sensor.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorAddressSorter extends SensorSorter {

    @Override
    public int compare(Sensor s1, Sensor s2) {
        return s1.getRawAddress() - s2.getRawAddress();
    }
}
