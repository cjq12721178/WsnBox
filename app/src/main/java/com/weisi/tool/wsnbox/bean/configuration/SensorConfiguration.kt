package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.node.Sensor
import com.cjq.lib.weisi.node.ValueContainer

/**
 * Created by CJQ on 2018/2/13.
 */
open class SensorConfiguration : Sensor.Configuration {

    private var decorator: ValueContainer.Decorator<Sensor.Value>? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: ValueContainer.Decorator<Sensor.Value>?) {
        this.decorator = decorator
    }
}