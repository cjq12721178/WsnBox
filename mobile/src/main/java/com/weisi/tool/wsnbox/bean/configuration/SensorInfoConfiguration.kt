package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Configuration
import com.cjq.lib.weisi.iot.container.Corrector
import com.cjq.lib.weisi.iot.Decorator


/**
 * Created by CJQ on 2018/2/13.
 */
open class SensorInfoConfiguration : Configuration {

    private var decorator: Decorator? = null
    private var corrector: Corrector? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: Decorator?) {
        this.decorator = decorator
    }
}