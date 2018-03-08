package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.node.Sensor
import com.cjq.lib.weisi.node.SensorManager

/**
 * Created by CJQ on 2018/2/9.
 */
class CommonValueContainerConfigurationProvider(
        private val sensorConfigurations: Map<Int, Sensor.Configuration>,
        private val measurementConfigurations: Map<Long, Sensor.Measurement.Configuration>
) : SensorManager.ValueContainerConfigurationProvider {

    override fun getSensorConfiguration(address: Int): Sensor.Configuration? {
        return sensorConfigurations.get(address)
    }

    override fun getMeasurementConfiguration(address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int): Sensor.Measurement.Configuration? {
        return measurementConfigurations.get(Sensor.Measurement.ID.getId(address, dataTypeValue, dataTypeValueIndex))
    }
}