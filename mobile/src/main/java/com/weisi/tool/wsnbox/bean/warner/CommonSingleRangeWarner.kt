package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.DisplayMeasurement.SingleRangeWarner.*
import com.cjq.lib.weisi.iot.container.Corrector

/**
 * Created by CJQ on 2018/2/8.
 */
open class CommonSingleRangeWarner(val highLimit: Double, val lowLimit: Double) : DisplayMeasurement.SingleRangeWarner {

    override fun test(value: DisplayMeasurement.Value, corrector: Corrector?): Int {
        val rawValue = corrector?.correctValue(value.rawValue) ?: value.rawValue
        return when {
            rawValue > highLimit -> RESULT_ABOVE_HIGH_LIMIT
            rawValue < lowLimit -> RESULT_BELOW_LOW_LIMIT
            else -> RESULT_NORMAL
        }
    }
}