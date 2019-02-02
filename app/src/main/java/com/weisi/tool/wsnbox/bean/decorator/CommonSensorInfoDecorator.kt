package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.Sensor

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonSensorInfoDecorator(customName: String) : CommonBaseDecorator<Sensor.Info.Value>(customName) {

    override fun decorateValue(rawValue: Double, para: Int): String {
        return ""
    }

    override fun decorateValue(value: Sensor.Info.Value, para: Int): String {
        return ""
    }
}