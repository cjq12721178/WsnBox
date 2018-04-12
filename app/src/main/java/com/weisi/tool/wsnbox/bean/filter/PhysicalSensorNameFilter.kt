package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.Sensor


/**
 * Created by CJQ on 2018/2/6.
 */
class PhysicalSensorNameFilter(var keyWord: String = "") : Sensor.Filter<PhysicalSensor> {

    override fun isMatch(sensor: PhysicalSensor): Boolean {
        if (sensor.name.contains(keyWord)) {
            return true
        }
        for (measurement in sensor.measurementCollections) {
            if (measurement.name.contains(keyWord)) {
                return true
            }
        }
        return false;
    }
}