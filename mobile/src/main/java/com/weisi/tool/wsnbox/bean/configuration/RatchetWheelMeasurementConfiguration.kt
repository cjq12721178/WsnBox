package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.RatchetWheelMeasurement

class RatchetWheelMeasurementConfiguration : DisplayMeasurementConfiguration(), RatchetWheelMeasurement.Configuration {

    private var distance = 0.0
    private var value = 0.0

    override fun getInitialDistance(): Double {
        return distance
    }

    override fun getInitialValue(): Double {
        return value
    }

    fun setInitialDistance(distance: Double) {
        this.distance = distance
    }

    fun setInitialValue(value: Double) {
        this.value = value
    }
}