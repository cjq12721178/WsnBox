package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.node.Sensor
import com.cjq.lib.weisi.node.SensorManager
import java.util.*

/**
 * Created by CJQ on 2018/1/30.
 */
class SensorTypeFilter(selectedSensorTypeNos: List<Int>) : Sensor.Filter {

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
            SensorManager.getBleSensorTypes().forEach(fun(type: Sensor.Type) {
                sensorTypeNameSet.add(type.sensorGeneralName)
            })
            SensorManager.getEsbSensorTypes().forEach(fun(type: Sensor.Type) {
                sensorTypeNameSet.add(type.sensorGeneralName)
            })
            return sensorTypeNameSet.toTypedArray()
        }
    }

    override fun isMatch(sensor: Sensor?): Boolean {
        if (selectedSensorTypeNos.isEmpty()) {
            return true
        }
        for (selectedSensorTypeNo in selectedSensorTypeNos) {
            if (sensor?.type?.sensorGeneralName.equals(sensorTypeNames[selectedSensorTypeNo])) {
                return true
            }
        }
        return false
    }
}