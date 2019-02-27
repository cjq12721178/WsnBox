package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.DisplayMeasurement.SwitchWarner.RESULT_ABNORMAL
import com.cjq.lib.weisi.iot.Warner.RESULT_NORMAL

/**
 * Created by CJQ on 2018/2/8.
 */
class CommonSwitchWarner : DisplayMeasurement.SwitchWarner {

    var abnormalValue = 0.0

    override fun test(value: DisplayMeasurement.Value): Int {
        return if (value.rawValue == abnormalValue) {
            RESULT_ABNORMAL
        } else {
            RESULT_NORMAL
        }
    }
}