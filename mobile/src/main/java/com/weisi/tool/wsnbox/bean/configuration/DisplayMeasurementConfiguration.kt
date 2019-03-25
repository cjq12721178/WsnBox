package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.Warner
import com.cjq.lib.weisi.iot.container.Corrector

/**
 * Created by CJQ on 2018/2/13.
 */
open class DisplayMeasurementConfiguration : DisplayMeasurement.Configuration {

    private var decorator: Decorator? = null
    private var warner: Warner<DisplayMeasurement.Value>? = null
    private var corrector: Corrector? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: Decorator?) {
        this.decorator = decorator
    }

    override fun getWarner() = warner

    override fun setWarner(warner: Warner<DisplayMeasurement.Value>?) {
        this.warner = warner
    }

    override fun setCorrector(corrector: Corrector?) {
        this.corrector = corrector
    }

    override fun getCorrector() = corrector
}