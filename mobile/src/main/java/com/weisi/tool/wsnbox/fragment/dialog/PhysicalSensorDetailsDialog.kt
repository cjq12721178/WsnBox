package com.weisi.tool.wsnbox.fragment.dialog

import android.os.Bundle
import com.cjq.lib.weisi.iot.*

class PhysicalSensorDetailsDialog : SensorDetailsDialog<PhysicalSensor>() {

    override fun init(sensor: PhysicalSensor) {
        super.init(sensor)
        initMeasurements(sensor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            sensor = SensorManager.getPhysicalSensor(it.getLong(ARGUMENT_KEY_SENSOR))
        }
    }

    override fun enableUpdateMeasurementValue(measurementId: Long): Boolean {
        return sensor.rawAddress == ID.getAddress(measurementId)
    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
        if (isRealTime()) {
            return
        }
        super.onSensorInfoHistoryValueUpdate(info, valuePosition)
        if (!viewPrepared) {
            return
        }
        updateMainMeasurementTableValue(info, valuePosition)
    }

    override fun onSensorInfoDynamicValueUpdate(info: Sensor.Info, valuePosition: Int) {
        setStateLabel()
        if (!isRealTime()) {
            return
        }
        super.onSensorInfoDynamicValueUpdate(info, valuePosition)
        //Log.d(Tag.LOG_TAG_D_TEST, "info, physical position = ${info.uniteValueContainer.getPhysicalPositionByLogicalPosition(valuePosition)}")
        if (!viewPrepared) {
            return
        }
        updateMainMeasurementTableValue(info, valuePosition)
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (!isRealTime()) {
            return
        }
        super.onMeasurementDynamicValueUpdate(measurement, valuePosition)
        //Log.d(Tag.LOG_TAG_D_TEST, "measurement(${measurement.name}), physical position = ${measurement.uniteValueContainer.getPhysicalPositionByLogicalPosition(valuePosition)}")
        if (!viewPrepared) {
            return
        }
        updateMeasurementTableValue(measurement, valuePosition)
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (isRealTime()) {
            return
        }
        super.onMeasurementHistoryValueUpdate(measurement, valuePosition)
        if (!viewPrepared) {
            return
        }
        updateMeasurementTableValue(measurement, valuePosition)
    }
}