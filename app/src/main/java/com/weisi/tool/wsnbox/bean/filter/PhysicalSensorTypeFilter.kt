package com.weisi.tool.wsnbox.bean.filter


import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.SensorManager
import java.util.*

/**
 * Created by CJQ on 2018/1/30.
 */
class PhysicalSensorTypeFilter(selectedSensorTypeNos: List<Int>) : Sensor.Filter<PhysicalSensor> {

    var selectedSensorTypeNos = selectedSensorTypeNos
        set(value) {
        if (value.size <= sensorTypeNames.size) {
            field = value
        }
    }

    companion object {
        @JvmStatic
        val sensorTypeNames by lazy {
            generateSensorTypeNames()
        }

        private fun generateSensorTypeNames(): Array<String> {

            var sensorTypeNameSet = HashSet<String>()
            SensorManager.getBleSensorTypes().forEach(fun(type: PhysicalSensor.Type) {
                sensorTypeNameSet.add(type.sensorGeneralName)
            })
            SensorManager.getEsbSensorTypes().forEach(fun(type: PhysicalSensor.Type) {
                sensorTypeNameSet.add(type.sensorGeneralName)
            })
            return sensorTypeNameSet.toTypedArray()
        }
    }

    override fun isMatch(sensor: PhysicalSensor): Boolean {
        if (selectedSensorTypeNos.isEmpty()) {
            return true
        }
        for (selectedSensorTypeNo in selectedSensorTypeNos) {
            if (sensor.type.sensorGeneralName == sensorTypeNames[selectedSensorTypeNo]) {
                return true
            }
        }
        return false
    }
}