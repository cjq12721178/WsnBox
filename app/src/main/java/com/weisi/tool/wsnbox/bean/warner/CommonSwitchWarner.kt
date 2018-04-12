package com.weisi.tool.wsnbox.bean.warner

import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.LogicalSensor.SwitchWarner.RESULT_IN_ABNORMAL_STATE
import com.cjq.lib.weisi.iot.LogicalSensor.SwitchWarner.RESULT_IN_NORMAL_STATE

/**
 * Created by CJQ on 2018/2/8.
 */
class CommonSwitchWarner : LogicalSensor.SwitchWarner {

    var abnormalValue = 0.0

    override fun test(value: LogicalSensor.Value): Int {
        return if (value.rawValue == abnormalValue) {
            RESULT_IN_ABNORMAL_STATE
        } else {
            RESULT_IN_NORMAL_STATE
        }
    }
}