package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.LogicalSensor

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonLogicalSensorDecorator(customName: String) : CommonBaseDecorator<LogicalSensor.Value>(customName) {

    override fun decorateValue(value: LogicalSensor.Value, para: Int): String {
        throw UnsupportedOperationException("not implemented")
    }
}