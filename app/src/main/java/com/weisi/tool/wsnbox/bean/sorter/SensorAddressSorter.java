package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.iot.PhysicalSensor;

/**
 * Created by CJQ on 2017/9/14.
 */

public class SensorAddressSorter extends SensorSorter<PhysicalSensor> {

    @Override
    public int compare(PhysicalSensor s1, PhysicalSensor s2) {
        return s1.getRawAddress() - s2.getRawAddress();
    }
}
