package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.PhysicalSensor

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonPhysicalSensorDecorator(customName: String) : CommonBaseDecorator<PhysicalSensor.Value>(customName) {
    override fun decorateValue(value: PhysicalSensor.Value, para: Int): String {
        throw UnsupportedOperationException("not implemented")
    }
}