package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.iot.Sensor

class SensorHasValueFilter<S : Sensor> : Filter<S> {

    override fun match(s: S): Boolean {
        return s.mainMeasurement.hasValue()
    }
}