package com.weisi.tool.wsnbox.fragment.dialog

import android.os.Bundle
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.SensorManager

open class LogicalSensorDetailsDialog : SensorDetailsDialog<LogicalSensor>() {

    override fun init(sensor: LogicalSensor) {
        super.init(sensor)
        initMeasurements(sensor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            sensor = SensorManager.getLogicalSensor(it.getLong(ARGUMENT_KEY_SENSOR)) ?: throw IllegalArgumentException("logical sensor(id: ${it.getLong(ARGUMENT_KEY_SENSOR)}) is missing")
        }
    }

//    override fun onServiceConnectionCreate(service: DataPrepareService) {
//        super.onServiceConnectionCreate(service)
//        service.dataTransferStation.payAttentionToLogicalSensor(sensor)
//        if (isRealTime()) {
//            enableDetectSensorInfoDynamicValueUpdate = true
//            enableDetectMeasurementDynamicValueUpdate = true
//        } else {
//            enableDetectSensorInfoHistoryValueUpdate = true
//            enableDetectMeasurementHistoryValueUpdate = true
//        }
//    }
//
//    override fun onServiceConnectionStart(service: DataPrepareService) {
//    }
//
//    override fun onServiceConnectionStop(service: DataPrepareService) {
//    }
//
//    override fun onServiceConnectionDestroy(service: DataPrepareService) {
//        if (isRealTime()) {
//            enableDetectSensorInfoDynamicValueUpdate = false
//            enableDetectMeasurementDynamicValueUpdate = false
//        } else {
//            enableDetectSensorInfoHistoryValueUpdate = false
//            enableDetectMeasurementHistoryValueUpdate = false
//        }
//        service.dataTransferStation.payNoAttentionToLogicalSensor(sensor)
//        super.onServiceConnectionDestroy(service)
//    }

    override fun enableUpdateMeasurementValue(measurementId: Long): Boolean {
        return measurementId == sensor.info.id.id || measurementId == sensor.practicalMeasurement.id.id
    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
        if (isRealTime()) {
            return
        }
        super.onSensorInfoHistoryValueUpdate(info, valuePosition)
        if (!viewPrepared) {
            return
        }
        //tableAdapter.updateMeasurementValue(getMainMeasurement(), info, valuePosition)
        updateMeasurementTableValue(info, valuePosition)
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
        //tableAdapter.updateMainValue(info, valuePosition)
        updateMeasurementTableValue(info, valuePosition)
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
        //tableAdapter.updateMeasurementValue(sensor.info, measurement, valuePosition)
        updateMainMeasurementTableValue(measurement, valuePosition)
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (isRealTime()) {
            return
        }
        super.onMeasurementHistoryValueUpdate(measurement, valuePosition)
        if (!viewPrepared) {
            return
        }
        //tableAdapter.updateMeasurementValue(sensor.info, measurement, valuePosition)
        updateMainMeasurementTableValue(measurement, valuePosition)
    }
}