package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.node.Sensor

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonMeasurementDecorator(customName: String) : CommonBaseDecorator<Sensor.Measurement.Value>(customName) {

    override fun decorateValue(p0: Sensor.Measurement.Value?, p1: Int): String {
        throw UnsupportedOperationException("not implemented")
    }
}