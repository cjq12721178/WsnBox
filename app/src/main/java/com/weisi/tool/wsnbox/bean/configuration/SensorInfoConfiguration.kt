package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.Sensor


/**
 * Created by CJQ on 2018/2/13.
 */
open class SensorInfoConfiguration : Sensor.Info.Configuration {

    private var decorator: Decorator<Sensor.Info.Value>? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: Decorator<Sensor.Info.Value>?) {
        this.decorator = decorator
    }
}