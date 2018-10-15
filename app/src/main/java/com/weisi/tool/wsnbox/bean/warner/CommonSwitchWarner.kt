package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.DisplayMeasurement.SwitchWarner.RESULT_IN_ABNORMAL_STATE
import com.cjq.lib.weisi.iot.DisplayMeasurement.SwitchWarner.RESULT_IN_NORMAL_STATE

/**
 * Created by CJQ on 2018/2/8.
 */
class CommonSwitchWarner : DisplayMeasurement.SwitchWarner {

    var abnormalValue = 0.0

    override fun test(value: DisplayMeasurement.Value): Int {
        return if (value.rawValue == abnormalValue) {
            RESULT_IN_ABNORMAL_STATE
        } else {
            RESULT_IN_NORMAL_STATE
        }
    }
}