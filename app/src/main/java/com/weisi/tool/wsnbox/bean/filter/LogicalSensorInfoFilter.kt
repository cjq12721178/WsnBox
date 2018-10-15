package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.iot.LogicalSensor

/**
 * Created by CJQ on 2018/6/6.
 */
class LogicalSensorInfoFilter(var keyWord: String = "") : Filter<LogicalSensor> {

    override fun match(sensor: LogicalSensor): Boolean {
        return sensor.practicalMeasurement.name.contains(keyWord)
    }
}