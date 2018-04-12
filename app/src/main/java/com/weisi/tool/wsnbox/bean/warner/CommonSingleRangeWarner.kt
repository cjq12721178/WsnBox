package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.LogicalSensor.SingleRangeWarner.*

/**
 * Created by CJQ on 2018/2/8.
 */
open class CommonSingleRangeWarner : LogicalSensor.SingleRangeWarner {

    var highLimit = 0.0
    var lowLimit = 0.0

    override fun test(value: LogicalSensor.Value): Int {
        var rawValue = value.rawValue
        return if (rawValue > highLimit) {
            RESULT_ABOVE_HIGH_LIMIT
        } else if (rawValue < lowLimit) {
            RESULT_BELOW_LOW_LIMIT
        } else {
            RESULT_NORMAL
        }
    }
}