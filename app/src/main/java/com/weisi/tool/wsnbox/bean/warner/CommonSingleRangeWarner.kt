package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.node.Sensor
import com.cjq.lib.weisi.node.Sensor.Measurement.SingleRangeWarner.*

/**
 * Created by CJQ on 2018/2/8.
 */
open class CommonSingleRangeWarner : Sensor.Measurement.SingleRangeWarner {

    var highLimit = 0.0
    var lowLimit = 0.0

    override fun test(value: Sensor.Measurement.Value?): Int {
        var rawValue = value!!.rawValue
        return if (rawValue > highLimit) {
            RESULT_ABOVE_HIGH_LIMIT
        } else if (rawValue < lowLimit) {
            RESULT_BELOW_LOW_LIMIT
        } else {
            RESULT_NORMAL
        }
    }
}