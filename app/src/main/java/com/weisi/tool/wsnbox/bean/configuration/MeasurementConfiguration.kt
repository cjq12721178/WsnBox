package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.node.Sensor
import com.cjq.lib.weisi.node.ValueContainer

/**
 * Created by CJQ on 2018/2/13.
 */
class MeasurementConfiguration : Sensor.Measurement.Configuration {

    private var decorator: ValueContainer.Decorator<Sensor.Measurement.Value>? = null
    private var warner: ValueContainer.Warner<Sensor.Measurement.Value>? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: ValueContainer.Decorator<Sensor.Measurement.Value>?) {
        this.decorator = decorator
    }

    override fun getWarner() = warner

    override fun setWarner(warner: ValueContainer.Warner<Sensor.Measurement.Value>?) {
        this.warner = warner
    }
}