package com.weisi.tool.wsnbox.bean.filter


import android.os.Parcel
import android.os.Parcelable
import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.SensorManager
import java.util.*

/**
 * Created by CJQ on 2018/1/30.
 */
class PhysicalSensorTypeFilter(selectedSensorTypeNos: List<Int>) : Filter<PhysicalSensor>, Parcelable {

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
            sensorTypeNameSet.add("未知传感器")
            return sensorTypeNameSet.toTypedArray()
        }

        val CREATOR : Parcelable.Creator<PhysicalSensorTypeFilter> = object : Parcelable.Creator<PhysicalSensorTypeFilter> {
            override fun createFromParcel(parcel: Parcel): PhysicalSensorTypeFilter {
                return PhysicalSensorTypeFilter(parcel)
            }

            override fun newArray(size: Int): Array<PhysicalSensorTypeFilter?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun match(sensor: PhysicalSensor): Boolean {
        if (selectedSensorTypeNos.isEmpty()) {
            return true
        }
        var i = 0
        val size = selectedSensorTypeNos.size
        while (i < size) {
            if (sensor.type.sensorGeneralName == sensorTypeNames[selectedSensorTypeNos[i]]) {
                return true
            }
            ++i
        }
        return false
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(selectedSensorTypeNos)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor(parcel: Parcel) : this(arrayListOf()) {
        parcel.readList(selectedSensorTypeNos, javaClass.classLoader)
    }
}