package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.DisplayMeasurement.SwitchWarner.RESULT_ABNORMAL
import com.cjq.lib.weisi.iot.Warner.RESULT_NORMAL
import com.cjq.lib.weisi.iot.container.Corrector

/**
 * Created by CJQ on 2018/2/8.
 */
class CommonSwitchWarner(val abnormalValue: Double) : DisplayMeasurement.SwitchWarner {

    override fun test(value: DisplayMeasurement.Value, corrector: Corrector?): Int {
        val rawValue = corrector?.correctValue(value.rawValue) ?: value.rawValue
        return if (rawValue == abnormalValue) {
            RESULT_ABNORMAL
        } else {
            RESULT_NORMAL
        }
    }
}