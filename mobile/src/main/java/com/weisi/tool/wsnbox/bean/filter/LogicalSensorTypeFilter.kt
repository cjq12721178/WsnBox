package com.weisi.tool.wsnbox.bean.filter

import android.os.Parcel
import android.os.Parcelable
import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.SensorManager
import java.text.Collator
import java.util.*

/**
 * Created by CJQ on 2018/6/6.
 */
class LogicalSensorTypeFilter(selectedSensorTypeNumbers: List<Int>) : Filter<LogicalSensor>, Parcelable {

    var selectedSensorTypeNos = selectedSensorTypeNumbers
    set(value) {
        if (value.size <= sensorTypeNames.size) {
            field = value
        }
    }

    constructor(parcel: Parcel) : this(arrayListOf()) {
        parcel.readList(selectedSensorTypeNos, javaClass.classLoader)
    }

    companion object {
        @JvmStatic
        val sensorTypeNames by lazy {
            generateSensorTypeNames()
        }

        private fun generateSensorTypeNames(): Array<String> {
            val sensorTypeNameSet = HashSet<String>()
            SensorManager.getBleDataTypes().forEach {
                sensorTypeNameSet.add(it.name)
            }
            SensorManager.getEsbDataTypes().forEach {
                sensorTypeNameSet.add(it.name)
            }
            sensorTypeNameSet.add("未知测量量")
            val result = sensorTypeNameSet.toTypedArray()
            Arrays.sort(result, Collator.getInstance(java.util.Locale.CHINA))
            return result
        }

        val CREATOR : Parcelable.Creator<LogicalSensorTypeFilter> = object : Parcelable.Creator<LogicalSensorTypeFilter> {
            override fun createFromParcel(parcel: Parcel): LogicalSensorTypeFilter {
                return LogicalSensorTypeFilter(parcel)
            }

            override fun newArray(size: Int): Array<LogicalSensorTypeFilter?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun match(sensor: LogicalSensor): Boolean {
        if (selectedSensorTypeNos.isEmpty()) {
            return true
        }
        var i = 0
        val size = selectedSensorTypeNos.size
        while (i < size) {
            if (sensor.practicalMeasurement.dataType.name == sensorTypeNames[selectedSensorTypeNos[i]]) {
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
}