package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.Warner

/**
 * Created by CJQ on 2018/2/13.
 */
open class DisplayMeasurementConfiguration : DisplayMeasurement.Configuration {

    private var decorator: Decorator<DisplayMeasurement.Value>? = null
    private var warner: Warner<DisplayMeasurement.Value>? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: Decorator<DisplayMeasurement.Value>?) {
        this.decorator = decorator
    }

    override fun getWarner() = warner

    override fun setWarner(warner: Warner<DisplayMeasurement.Value>?) {
        this.warner = warner
    }
}