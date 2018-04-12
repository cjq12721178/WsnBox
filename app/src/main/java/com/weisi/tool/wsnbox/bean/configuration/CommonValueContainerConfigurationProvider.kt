package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.SensorManager


/**
 * Created by CJQ on 2018/2/9.
 */
class CommonValueContainerConfigurationProvider(
        private val sensorConfigurations: Map<Sensor.ID?, Sensor.Configuration<*>?>
) : SensorManager.SensorConfigurationProvider {

    override fun <C : Sensor.Configuration<*>?> getSensorConfiguration(id: Sensor.ID?): C {
        return sensorConfigurations.get(id) as C
    }
}