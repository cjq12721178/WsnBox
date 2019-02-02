package com.weisi.tool.wsnbox.bean.comparator

import com.cjq.lib.weisi.iot.PhysicalSensor
import com.wsn.lib.wsb.util.ExpandComparator

class SensorAddressComparator : ExpandComparator<PhysicalSensor, Int> {
    override fun compare(a: PhysicalSensor, b: Int): Int {
        return a.rawAddress.compareTo(b)
    }
}