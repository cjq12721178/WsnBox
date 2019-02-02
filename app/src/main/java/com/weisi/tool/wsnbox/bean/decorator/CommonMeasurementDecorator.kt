package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.DisplayMeasurement

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonMeasurementDecorator(customName: String) : CommonBaseDecorator<DisplayMeasurement.Value>(customName) {

    override fun decorateValue(rawValue: Double, para: Int): String {
        return ""
    }

    override fun decorateValue(value: DisplayMeasurement.Value, para: Int): String {
        return ""
    }
}