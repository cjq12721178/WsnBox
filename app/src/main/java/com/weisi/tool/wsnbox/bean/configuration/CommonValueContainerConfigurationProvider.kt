package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.*


/**
 * Created by CJQ on 2018/2/9.
 */
class CommonValueContainerConfigurationProvider(
        private val sensorConfigurations: Map<ID, Configuration<*>>
) : SensorManager.MeasurementConfigurationProvider {

    override fun <C : Configuration<*>?> getConfiguration(id: ID?): C {
        return sensorConfigurations[id] as C
    }

    override fun getConfigurationIds(): MutableList<ID> {
        return sensorConfigurations.keys.toMutableList()
    }

    override fun getConfigurationsSortedById(): MutableList<Configuration<*>> {
        return sensorConfigurations.entries.toList().sortedBy { it.key }.map { it.value }.toMutableList()
    }

    override fun getDisplayMeasurementConfigurations(): MutableList<DisplayMeasurement.Configuration> {
        return sensorConfigurations.mapNotNull {
            it.value as? DisplayMeasurement.Configuration
        }.toMutableList()
    }

    override fun getSensorInfoConfigurations(): MutableList<Sensor.Info.Configuration> {
        return sensorConfigurations.mapNotNull {
            it.value as? Sensor.Info.Configuration
        }.toMutableList()
    }
}