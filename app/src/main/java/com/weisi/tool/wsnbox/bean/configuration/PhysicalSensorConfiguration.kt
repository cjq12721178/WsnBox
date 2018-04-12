package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.PhysicalSensor


/**
 * Created by CJQ on 2018/2/13.
 */
open class PhysicalSensorConfiguration : PhysicalSensor.Configuration {

    private var decorator: Decorator<PhysicalSensor.Value>? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: Decorator<PhysicalSensor.Value>?) {
        this.decorator = decorator
    }
}