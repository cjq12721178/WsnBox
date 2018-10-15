package com.weisi.tool.wsnbox.adapter.config

import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration

class RatchetWheelConfigAdapterDelegate : MeasurementConfigAdapterDelegate(SensorConfiguration.Measure.CT_RATCHET_WHEEL) {

    override fun getExtraConfigLayoutRes(): Int {
        return R.layout.group_ratchet_wheel
    }


}