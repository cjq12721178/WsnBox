package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.iot.PhysicalSensor


/**
 * Created by CJQ on 2018/2/6.
 */
class PhysicalSensorInfoFilter(var keyWord: String = "") : Filter<PhysicalSensor> {

    override fun match(sensor: PhysicalSensor): Boolean {
        if (sensor.info.name.contains(keyWord)) {
            return true
        }
        for (i in 0 until sensor.displayMeasurementSize) {
            if (sensor.getDisplayMeasurementByPosition(i).name.contains(keyWord)) {
                return true
            }
        }
        return false;
    }
}