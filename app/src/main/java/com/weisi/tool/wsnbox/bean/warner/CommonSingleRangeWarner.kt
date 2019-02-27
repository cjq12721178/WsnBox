package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.DisplayMeasurement.SingleRangeWarner.*

/**
 * Created by CJQ on 2018/2/8.
 */
open class CommonSingleRangeWarner : DisplayMeasurement.SingleRangeWarner {

    var highLimit = 0.0
    var lowLimit = 0.0

    override fun test(value: DisplayMeasurement.Value): Int {
        val rawValue = value.rawValue
        return when {
            rawValue > highLimit -> RESULT_ABOVE_HIGH_LIMIT
            rawValue < lowLimit -> RESULT_BELOW_LOW_LIMIT
            else -> RESULT_NORMAL
        }
    }
}