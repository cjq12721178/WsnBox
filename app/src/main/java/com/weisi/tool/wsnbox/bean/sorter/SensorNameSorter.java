package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.iot.PhysicalSensor;

/**
 * Created by CJQ on 2018/4/17.
 */

public class SensorNameSorter extends SensorSorter<PhysicalSensor> {

    @Override
    public int compare(PhysicalSensor s1, PhysicalSensor s2) {
        return s1.getName().compareTo(s2.getName());
    }
}
