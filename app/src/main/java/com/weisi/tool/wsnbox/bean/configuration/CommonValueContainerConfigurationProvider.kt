package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.Configuration
import com.cjq.lib.weisi.iot.ID
import com.cjq.lib.weisi.iot.SensorManager


/**
 * Created by CJQ on 2018/2/9.
 */
class CommonValueContainerConfigurationProvider(
        private val sensorConfigurations: Map<ID, Configuration<*>>
) : SensorManager.MeasurementConfigurationProvider {

    override fun <C : Configuration<*>?> getConfiguration(id: ID?): C {
        return sensorConfigurations[id] as C
    }
}