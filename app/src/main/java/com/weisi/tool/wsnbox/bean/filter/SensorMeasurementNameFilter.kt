package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.node.Sensor

/**
 * Created by CJQ on 2018/2/6.
 */
class SensorMeasurementNameFilter(var keyWord: String = "") : Sensor.Filter {

    override fun isMatch(sensor: Sensor?): Boolean {
        if (sensor!!.name.contains(keyWord)) {
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