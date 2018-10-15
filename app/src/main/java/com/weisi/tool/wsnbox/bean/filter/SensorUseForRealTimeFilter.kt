package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.iot.Sensor

/**
 * Created by CJQ on 2017/9/19.
 */

class SensorUseForRealTimeFilter<S : Sensor> : Filter<S> {

    override fun match(sensor: S): Boolean {
        return sensor.mainMeasurement.hasRealTimeValue()
    }
}
