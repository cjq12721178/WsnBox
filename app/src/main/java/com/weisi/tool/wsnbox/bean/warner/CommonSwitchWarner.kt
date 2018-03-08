package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.node.Sensor
import com.cjq.lib.weisi.node.Sensor.Measurement.SwitchWarner.*

/**
 * Created by CJQ on 2018/2/8.
 */
class CommonSwitchWarner : Sensor.Measurement.SwitchWarner {

    var abnormalValue = 0.0

    override fun test(value: Sensor.Measurement.Value?): Int {
        return if (value?.rawValue == abnormalValue) {
            RESULT_IN_ABNORMAL_STATE
        } else {
            RESULT_IN_NORMAL_STATE
        }
    }
}