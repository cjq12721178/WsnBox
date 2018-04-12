package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.Warner

/**
 * Created by CJQ on 2018/2/13.
 */
class LogicalSensorConfiguration : LogicalSensor.Configuration {

    private var decorator: Decorator<LogicalSensor.Value>? = null
    private var warner: Warner<LogicalSensor.Value>? = null

    override fun getDecorator() = decorator

    override fun setDecorator(decorator: Decorator<LogicalSensor.Value>?) {
        this.decorator = decorator
    }

    override fun getWarner() = warner

    override fun setWarner(warner: Warner<LogicalSensor.Value>?) {
        this.warner = warner
    }
}