package com.weisi.tool.wsnbox.processor.transfer

import android.os.Handler
import android.os.Message
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import com.cjq.lib.weisi.iot.*
import com.weisi.tool.wsnbox.bean.data.SensorData


/**
 * Created by CJQ on 2018/3/29.
 */

class DataTransferStation {

//    companion object {
//        const val MODE_NO_CHECK = 0
//        const val MODE_CHECK_ALL = 1
//        const val MODE_CHECK_ATTENTION = 2
//    }

    private val MSG_LOGICAL_SENSOR_NET_IN = 1
    //private val MSG_LOGICAL_SENSOR_VALUE_UPDATE = 2
    private val MSG_PHYSICAL_SENSOR_NET_IN = 3
    private val MSG_SENSOR_DYNAMIC_DATA_ACCESS = 4
    //private val MSG_PHYSICAL_SENSOR_VALUE_UPDATE = 4
    private val MSG_MEASUREMENT_HISTORY_VALUE_UPDATE = 5
    private val MSG_SENSOR_INFO_HISTORY_VALUE_UPDATE = 6
    //private val MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS = 7
    //private val MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS = 8
    private val MSG_MEASUREMENT_HISTORY_DATA_ACCESS = 9
    private val MSG_SENSOR_INFO_HISTORY_DATA_ACCESS = 10
    private val MSG_MEASUREMENT_DYNAMIC_VALUE_UPDATE = 11
    private val MSG_SENSOR_INFO_DYNAMIC_VALUE_UPDATE = 12

    //private var lastNetInTimestamp: Long = 0
    //private val LAST_NET_IN_TIME_LOCKER = Any()
    private val detectors = mutableListOf<Detector>()
    private val focusedMeasurements = mutableMapOf<Long, Measurement<*, *>>()

    //动态传感器数据添加进内存的方式，true时采用物理传感器模式，false时采用逻辑传感器模式
    //var dynamicSensorDataImportMode = true

//    var enableDetectPhysicalSensorNetIn = false
//    var enableDetectPhysicalSensorValueUpdate = false
//    var enableDetectLogicalSensorNetIn = false
//    var enableDetectLogicalSensorValueUpdate = false
//    var enableDetectSensorInfoHistoryValueUpdate
//= false
//    var enableDetectMeasurementHistoryValueUpdate = false
//    var enableDetectMeasurementDynamicValueUpdate = false

    private val eventHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                MSG_PHYSICAL_SENSOR_NET_IN ->
                    notifyPhysicalSensorNetIn(msg.obj as PhysicalSensor)
//                MSG_PHYSICAL_SENSOR_VALUE_UPDATE ->
//                    notifyPhysicalSensorValueUpdate(msg.obj as PhysicalSensor, msg.arg1)
                MSG_LOGICAL_SENSOR_NET_IN ->
                    notifyLogicalSensorNetIn(msg.obj as LogicalSensor)
//                MSG_LOGICAL_SENSOR_VALUE_UPDATE ->
//                    notifyLogicalSensorValueUpdate(msg.obj as LogicalSensor, msg.arg1)
                MSG_SENSOR_INFO_HISTORY_VALUE_UPDATE ->
                    notifySensorInfoHistoryValueUpdate(msg.obj as Sensor.Info, msg.arg1)
                MSG_MEASUREMENT_HISTORY_VALUE_UPDATE ->
                    notifyMeasurementHistoryValueUpdate(msg.obj as PracticalMeasurement, msg.arg1)
                MSG_SENSOR_DYNAMIC_DATA_ACCESS ->
                    processSensorDynamicDataAccess(msg.obj as SensorData)
//                MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS ->
//                    processLogicalSensorDynamicDataAccess(msg.obj as SensorData)
//                MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS ->
//                    processPhysicalSensorDynamicDataAccess(msg.obj as SensorData)
                MSG_MEASUREMENT_HISTORY_DATA_ACCESS ->
                    processMeasurementHistoryDataAccess(msg.obj as SensorData)
                MSG_SENSOR_INFO_HISTORY_DATA_ACCESS ->
                    processSensorInfoHistoryDataAccess(msg.obj as SensorData)
                MSG_SENSOR_INFO_DYNAMIC_VALUE_UPDATE ->
                    notifySensorInfoDynamicValueUpdate(msg.obj as Sensor.Info, msg.arg1)
                MSG_MEASUREMENT_DYNAMIC_VALUE_UPDATE ->
                    notifyMeasurementDynamicValueUpdate(msg.obj as PracticalMeasurement, msg.arg1)
            }
        }
    }

    private val sensorValueNotifierOnWorkThread = object : Sensor.OnValueAchievedListener {

        override fun onDynamicMeasurementValueAchieved(sensor: Sensor, measurement: PracticalMeasurement, measurementValuePosition: Int) {
            if (sensor.id.isPhysicalSensor) {
                if (measurement.dynamicValueContainer.size() > 1) { //提高运行效率
                    processMeasurementDynamicValueAchieved(measurement, measurementValuePosition)
                } else {
                    val logicalSensor = SensorManager.getLogicalSensor(measurement.id.id) ?: return
                    if (recordSensorNetIn(logicalSensor) && enableDetectLogicalSensorNetIn()) {
                        sendLogicalSensorNetInMessage(logicalSensor)
                    } else {
                        processMeasurementDynamicValueAchieved(measurement, measurementValuePosition)
                    }
                }
            } else {
                if (recordSensorNetIn(sensor) && enableDetectLogicalSensorNetIn()) {
                    sendLogicalSensorNetInMessage(sensor as LogicalSensor)
                } else {
                    processMeasurementDynamicValueAchieved(measurement, measurementValuePosition)
                }
            }
        }

        override fun onHistoryMeasurementValueAchieved(sensor: Sensor, measurement: PracticalMeasurement, measurementValuePosition: Int) {
            if (enableDetectMeasurementHistoryValueUpdate()) {
                sendMeasurementHistoryValueUpdateMessage(measurement, measurementValuePosition)
            }
        }

        override fun onDynamicSensorInfoAchieved(sensor: Sensor, infoValuePosition: Int) {
            if (sensor.id.isPhysicalSensor) {
                if (recordSensorNetIn(sensor) && enableDetectPhysicalSensorNetIn()) {
                    sendPhysicalSensorNetInMessage(sensor as PhysicalSensor)
                } else {
                    processSensorInfoDynamicValueAchieved(sensor.info, infoValuePosition)
                }
            } else {
                if (sensor.info.dynamicValueContainer.size() > 1) { //提高运行效率
                    processSensorInfoDynamicValueAchieved(sensor.info, infoValuePosition)
                } else {
                    val physicalSensor = SensorManager.getPhysicalSensor(sensor.id)
                    if (recordSensorNetIn(physicalSensor) && enableDetectPhysicalSensorNetIn()) {
                        sendPhysicalSensorNetInMessage(physicalSensor)
                    } else {
                        processSensorInfoDynamicValueAchieved(sensor.info, infoValuePosition)
                    }
                }
            }
        }

        override fun onHistorySensorInfoAchieved(sensor: Sensor, infoValuePosition: Int) {
            if (enableDetectMeasurementHistoryValueUpdate()) {
                sendSensorInfoHistoryValueUpdateMessage(sensor.info, infoValuePosition)
            }
        }
    }

    private val sensorValueNotifierOnUiThread = object : Sensor.OnValueAchievedListener {

        override fun onDynamicMeasurementValueAchieved(sensor: Sensor, measurement: PracticalMeasurement, measurementValuePosition: Int) {
            if (sensor.id.isPhysicalSensor) {
                if (measurement.dynamicValueContainer.size() > 1) { //提高运行效率
                    notifyMeasurementDynamicValueUpdate(measurement, measurementValuePosition)
                } else {
                    val logicalSensor = SensorManager.getLogicalSensor(measurement.id.id) ?: return
                    if (recordSensorNetIn(logicalSensor)) {
                        notifyLogicalSensorNetIn(logicalSensor)
                    } else {
                        notifyMeasurementDynamicValueUpdate(measurement, measurementValuePosition)
                    }
                }
            } else {
                if (recordSensorNetIn(sensor)) {
                    notifyLogicalSensorNetIn(sensor as LogicalSensor)
                } else {
                    notifyMeasurementDynamicValueUpdate(measurement, measurementValuePosition)
                }
            }
        }

        override fun onHistoryMeasurementValueAchieved(sensor: Sensor, measurement: PracticalMeasurement, measurementValuePosition: Int) {
            notifyMeasurementHistoryValueUpdate(measurement, measurementValuePosition)
        }

        override fun onDynamicSensorInfoAchieved(sensor: Sensor, infoValuePosition: Int) {
            if (sensor.id.isPhysicalSensor) {
                if (recordSensorNetIn(sensor)) {
                    notifyPhysicalSensorNetIn(sensor as PhysicalSensor)
                } else {
                    notifySensorInfoDynamicValueUpdate(sensor.info, infoValuePosition)
                }
            } else {
                if (sensor.info.dynamicValueContainer.size() > 1) { //提高运行效率
                    notifySensorInfoDynamicValueUpdate(sensor.info, infoValuePosition)
                } else {
                    val physicalSensor = SensorManager.getPhysicalSensor(sensor.id)
                    if (recordSensorNetIn(physicalSensor)) {
                        notifyPhysicalSensorNetIn(physicalSensor)
                    } else {
                        notifySensorInfoDynamicValueUpdate(sensor.info, infoValuePosition)
                    }
                }
            }
        }

        override fun onHistorySensorInfoAchieved(sensor: Sensor, infoValuePosition: Int) {
            notifySensorInfoHistoryValueUpdate(sensor.info, infoValuePosition)
        }
    }

    fun register(listener: Detector) {
        if (!detectors.contains(listener)) {
            detectors.add(listener)
        }
    }

    fun unregister(listener: Detector) {
        detectors.remove(listener)
    }

    @WorkerThread
    fun processSensorDynamicDataAccess(address: Int,
                                       dataTypeValue: Byte,
                                       dataTypeValueIndex: Int,
                                       timestamp: Long,
                                       batteryVoltage: Float,
                                       rawValue: Double) {
        if (isInAttentionList(address, dataTypeValue, dataTypeValueIndex)) {
            sendSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        } else {
            SensorManager.addDynamicSensorValue(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        }
    }

    @WorkerThread
    fun processSensorInfoHistoryDataAccess(address: Int, timestamp: Long, batteryVoltage: Float) {
        if (enableDetectMeasurementHistoryValueUpdate()
                && isInAttentionList(address, 0, 0)) {
            sendSensorInfoHistoryDataAccessMessage(address, timestamp, batteryVoltage)
        } else {
            SensorManager.addHistorySensorInfoValue(address, timestamp, batteryVoltage)
//            val info = SensorManager.getSensorInfo(address)
//            val position = info.addHistoryValue(timestamp, batteryVoltage)
//            if (enableDetectSensorInfoHistoryValueUpdate()) {
//                sendSensorInfoHistoryValueUpdateMessage(info, position)
//            }
        }
    }

    @WorkerThread
    fun processMeasurementHistoryDataAccess(id: Long, timestamp: Long, rawValue: Double) {
        if (enableDetectMeasurementHistoryValueUpdate()
                && isInAttentionList(id)) {
            sendMeasurementHistoryDataAccessMessage(id, timestamp, rawValue)
        } else {
            SensorManager.addHistoryMeasurementValue(id, timestamp, rawValue)
//            val measurement = SensorManager.getPracticalMeasurement(id) ?: return
//            val position = measurement.addHistoryValue(timestamp, rawValue)
//            if (enableDetectMeasurementHistoryValueUpdate()) {
//                sendMeasurementHistoryValueUpdateMessage(measurement, position)
//            }
        }
    }

//    @WorkerThread
//    fun processSensorDynamicDataAccess(address: Int,
//                                       dataTypeValue: Byte,
//                                       dataTypeValueIndex: Int,
//                                       timestamp: Long,
//                                       batteryVoltage: Float,
//                                       rawValue: Double) {
//        if (dynamicSensorDataImportMode) {
//            if ((enableDetectPhysicalSensorNetIn()
//                    || enableDetectPhysicalSensorValueUpdate())
//                    && isInAttentionList(address, 0, 0)) {
//                sendPhysicalSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
//            } else {
//                val physicalSensor = SensorManager.getPhysicalSensor(address)
//                val infoValuePosition = physicalSensor.addDynamicValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
//                if (infoValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//                    val id = ID.getId(address, dataTypeValue, dataTypeValueIndex)
//                    if (recordSensorNetIn(physicalSensor)) {
//                        recordSensorNetIn(SensorManager.getLogicalSensor(id))
//                        if (enableDetectPhysicalSensorNetIn()) {
//                            sendPhysicalSensorNetInMessage(physicalSensor)
//                        } else {
//                            processPhysicalSensorDetectEvent(physicalSensor, infoValuePosition, id)
//                        }
//                    } else {
//                        recordSensorNetIn(SensorManager.getLogicalSensor(address, dataTypeValue, dataTypeValueIndex))
//                        processPhysicalSensorDetectEvent(physicalSensor, infoValuePosition, id)
//                    }
//                }
//            }
//        } else {
//            if ((enableDetectLogicalSensorNetIn()
//                    || enableDetectLogicalSensorValueUpdate())
//                    && isInAttentionList(address, dataTypeValue, dataTypeValueIndex)) {
//                sendLogicalSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
//            } else {
//                val logicalSensor = SensorManager.getLogicalSensor(address, dataTypeValue, dataTypeValueIndex) ?: return
//                val measurementValuePosition = logicalSensor.addDynamicValue(timestamp, batteryVoltage, rawValue)
//                if (measurementValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//                    if (recordSensorNetIn(logicalSensor)) {
//                        recordSensorNetIn(SensorManager.getPhysicalSensor(address))
//                        if (enableDetectLogicalSensorNetIn()) {
//                            sendLogicalSensorNetInMessage(logicalSensor)
//                        } else {
//                            processLogicalSensorDetectEvent(logicalSensor, measurementValuePosition)
//                        }
//                    } else {
//                        processLogicalSensorDetectEvent(logicalSensor, measurementValuePosition)
//                    }
//                }
//            }
//        }
//    }

    private fun enableDetectPhysicalSensorNetIn(): Boolean {
        for (i in detectors.indices) {
            if (detectors[i].enableDetectPhysicalSensorNetIn) {
                return true
            }
        }
        return false
    }

//    private fun enableDetectPhysicalSensorValueUpdate(): Boolean {
//        for (i in detectors.indices) {
//            if (detectors[i].enableDetectPhysicalSensorValueUpdate) {
//                return true
//            }
//        }
//        return false
//    }

    private fun enableDetectLogicalSensorNetIn(): Boolean {
        for (i in detectors.indices) {
            if (detectors[i].enableDetectLogicalSensorNetIn) {
                return true
            }
        }
        return false
    }

//    private fun enableDetectLogicalSensorValueUpdate(): Boolean {
//        for (i in detectors.indices) {
//            if (detectors[i].enableDetectLogicalSensorValueUpdate) {
//                return true
//            }
//        }
//        return false
//    }

//    private fun processPhysicalSensorDetectEvent(physicalSensor: PhysicalSensor, infoValuePosition: Int, measurementId: Long) {
//        if (enableDetectPhysicalSensorValueUpdate()) {
//            sendPhysicalSensorValueUpdateMessage(physicalSensor, infoValuePosition)
//        } else {
//            processSensorInfoDynamicValueAchieved(measurementId)
//        }
//    }

    private fun processSensorInfoDynamicValueAchieved(info: Sensor.Info, measurementValuePosition: Int) {
        var listener: Detector
        for (i in detectors.indices) {
            listener = detectors[i]
            if (listener.enableDetectMeasurementDynamicValueUpdate
                    && listener.enableUpdateMeasurementValue(info.id.id)) {
                sendSenorInfoDynamicValueUpdateMessage(info, measurementValuePosition)
                break
            }
        }
    }

    private fun processMeasurementDynamicValueAchieved(measurement: PracticalMeasurement, measurementValuePosition: Int) {
        var listener: Detector
        for (i in detectors.indices) {
            listener = detectors[i]
            if (listener.enableDetectMeasurementDynamicValueUpdate
                    && listener.enableUpdateMeasurementValue(measurement.id.id)) {
                sendMeasurementDynamicValueUpdateMessage(measurement, measurementValuePosition)
                break
            }
        }
    }

    private fun sendSenorInfoDynamicValueUpdateMessage(info: Sensor.Info, infoValuePosition: Int) {
        sendValueUpdateMessage(MSG_SENSOR_INFO_DYNAMIC_VALUE_UPDATE, info, infoValuePosition)
    }

    private fun sendMeasurementDynamicValueUpdateMessage(measurement: Measurement<*, *>, measurementValuePosition: Int) {
        sendValueUpdateMessage(MSG_MEASUREMENT_DYNAMIC_VALUE_UPDATE, measurement, measurementValuePosition)
    }

//    private fun processLogicalSensorDetectEvent(logicalSensor: LogicalSensor, measurementValuePosition: Int) {
//        if (enableDetectLogicalSensorValueUpdate()) {
//            sendLogicalSensorValueUpdateMessage(logicalSensor, measurementValuePosition)
//        } else {
//            processSensorInfoDynamicValueAchieved(logicalSensor.practicalMeasurement.id.id)
//        }
//    }

//    @UiThread
//    private fun processLogicalSensorDynamicDataAccess(data: SensorData) {
//        val logicalSensor = SensorManager.getLogicalSensor(data.id) ?: return
//        val measurementValuePosition = logicalSensor.addDynamicValue(data.timestamp, data.batteryVoltage, data.rawValue)
//        data.recycle()
//        if (measurementValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//            if (recordSensorNetIn(logicalSensor)) {
//                recordSensorNetIn(SensorManager.getPhysicalSensor(data.address))
//                notifyLogicalSensorNetIn(logicalSensor)
//            } else {
//                notifyLogicalSensorValueUpdate(logicalSensor, measurementValuePosition)
//            }
//        }
//    }

    @UiThread
    private fun processSensorDynamicDataAccess(data: SensorData) {
        SensorManager.addDynamicSensorValue(data.address,
                data.dataTypeValue,
                data.dataTypeValueIndex,
                data.timestamp,
                data.batteryVoltage,
                data.rawValue,
                sensorValueNotifierOnUiThread)
        //val physicalSensor = SensorManager.getPhysicalSensor(data.address)
        //val infoValuePosition = physicalSensor.addDynamicValue(data.dataTypeValue, data.dataTypeValueIndex, data.timestamp, data.batteryVoltage, data.rawValue)
        data.recycle()
//        if (infoValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//            if (recordSensorNetIn(physicalSensor)) {
//                recordSensorNetIn(SensorManager.getLogicalSensor(data.address, data.dataTypeValue, data.dataTypeValueIndex))
//                notifyPhysicalSensorNetIn(physicalSensor)
//            } else {
//                notifyPhysicalSensorValueUpdate(physicalSensor, infoValuePosition)
//            }
//        }
    }

//    private fun sendLogicalSensorDynamicDataAccessMessage(
//            address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int,
//            timestamp: Long, batteryVoltage: Float, rawValue: Double) {
//        val sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
//        sendDataAccessMessage(MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
//    }

//    private fun sendPhysicalSensorDynamicDataAccessMessage(
//            address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int,
//            timestamp: Long, batteryVoltage: Float, rawValue: Double) {
//        val sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
//        sendDataAccessMessage(MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
//    }

    private fun sendSensorDynamicDataAccessMessage(address: Int,
                                                   dataTypeValue: Byte, dataTypeValueIndex: Int,
                                                   timestamp: Long,
                                                   batteryVoltage: Float,
                                                   rawValue: Double) {
        val sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        sendDataAccessMessage(MSG_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
    }

    private fun sendDataAccessMessage(msg: Int, data: SensorData) {
        val message = Message.obtain()
        message.what = msg
        message.obj = data
        eventHandler.sendMessage(message)
    }

//    private fun enableDetectSensorInfoHistoryValueUpdate(): Boolean {
//        for (i in detectors.indices) {
//            if (detectors[i].enableDetectSensorInfoHistoryValueUpdate) {
//                return true
//            }
//        }
//        return false
//    }

    @UiThread
    private fun processSensorInfoHistoryDataAccess(data: SensorData) {
        SensorManager.addHistorySensorInfoValue(data.address,
                data.timestamp,
                data.batteryVoltage,
                sensorValueNotifierOnUiThread)
        data.recycle()
//        val info = SensorManager.getSensorInfo(data.address)
//        val position = info.addHistoryValue(data.timestamp, data.batteryVoltage)
//        data.recycle()
//        notifySensorInfoHistoryValueUpdate(info, position)
    }

//    @UiThread
//    private fun processMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, measurementValuePosition: Int) {
//        notifyMeasurementDynamicValueUpdate(measurement, measurementValuePosition)
//    }

    private fun sendSensorInfoHistoryDataAccessMessage(address: Int, timestamp: Long, batteryVoltage: Float) {
        val sensorData = SensorData.build(address, 0, 0, timestamp, batteryVoltage, 0.0)
        sendDataAccessMessage(MSG_SENSOR_INFO_HISTORY_DATA_ACCESS, sensorData)
    }

    private fun enableDetectMeasurementHistoryValueUpdate(): Boolean {
        for (i in detectors.indices) {
            if (detectors[i].enableDetectMeasurementHistoryValueUpdate) {
                return true
            }
        }
        return false
    }

    private fun sendMeasurementHistoryDataAccessMessage(sensorId: Long, timestamp: Long, rawValue: Double) {
        sendDataAccessMessage(MSG_MEASUREMENT_HISTORY_DATA_ACCESS,
                SensorData.build(sensorId, timestamp, 0f, rawValue))
    }

    @UiThread
    private fun processMeasurementHistoryDataAccess(data: SensorData) {
        SensorManager.addHistoryMeasurementValue(data.id,
                data.timestamp,
                data.rawValue,
                sensorValueNotifierOnUiThread)
        data.recycle()
//        val measurement = SensorManager.getPracticalMeasurement(data.id) ?: return
//        val position = measurement.addHistoryValue(data.timestamp, data.rawValue)
//        data.recycle()
//        notifyMeasurementHistoryValueUpdate(measurement, position)
    }

    private fun recordSensorNetIn(sensor: Sensor): Boolean {
        return if (sensor.netInTimestamp == 0L) {
            sensor.netInTimestamp = sensor.info.dynamicValueContainer.earliestValue.timestamp
            true
        } else {
            false
        }
    }

    private fun sendLogicalSensorNetInMessage(sensor: LogicalSensor) {
        val message = Message.obtain()
        message.what = MSG_LOGICAL_SENSOR_NET_IN
        message.obj = sensor
        eventHandler.sendMessage(message)
    }

//    private fun sendLogicalSensorValueUpdateMessage(sensor: LogicalSensor, valueLogicalPosition: Int) {
//        sendValueUpdateMessage(MSG_LOGICAL_SENSOR_VALUE_UPDATE, sensor, valueLogicalPosition)
//    }

    private fun sendPhysicalSensorNetInMessage(sensor: PhysicalSensor) {
        val message = Message.obtain()
        message.what = MSG_PHYSICAL_SENSOR_NET_IN
        message.obj = sensor
        eventHandler.sendMessage(message)
    }

//    private fun sendPhysicalSensorValueUpdateMessage(sensor: PhysicalSensor, valueLogicalPosition: Int) {
//        sendValueUpdateMessage(MSG_PHYSICAL_SENSOR_VALUE_UPDATE, sensor, valueLogicalPosition)
//    }

//    private fun sendMeasurementValueUpdateMessage(measurement: Measurement<*, *>, measurementValuePosition: Int) {
//        sendValueUpdateMessage(MSG_MEASUREMENT_DYNAMIC_VALUE_UPDATE, measurement, measurementValuePosition)
//    }

    private fun sendValueUpdateMessage(messageType: Int, target: Any, valuePosition: Int) {
        val message = Message.obtain()
        message.what = messageType
        message.obj = target
        message.arg1 = valuePosition
        eventHandler.sendMessage(message)
    }

    private fun sendMeasurementHistoryValueUpdateMessage(measurement: PracticalMeasurement, valuePosition: Int) {
        sendValueUpdateMessage(MSG_MEASUREMENT_HISTORY_VALUE_UPDATE, measurement, valuePosition)
    }

    private fun sendSensorInfoHistoryValueUpdateMessage(info: Sensor.Info, valuePosition: Int) {
        sendValueUpdateMessage(MSG_SENSOR_INFO_HISTORY_VALUE_UPDATE, info, valuePosition)
    }

    @UiThread
    private fun notifyLogicalSensorNetIn(sensor: LogicalSensor) {
        for (i in detectors.indices) {
            if (detectors[i].enableDetectLogicalSensorNetIn) {
                detectors[i].onLogicalSensorNetIn(sensor)
            }
        }
    }

//    @UiThread
//    private fun notifyLogicalSensorValueUpdate(sensor: LogicalSensor, valuePosition: Int) {
//        for (i in detectors.indices) {
//            if (detectors[i].enableDetectLogicalSensorValueUpdate) {
//                detectors[i].onLogicalSensorDynamicValueUpdate(sensor, valuePosition)
//            }
//        }
//    }

    @UiThread
    private fun notifyPhysicalSensorNetIn(sensor: PhysicalSensor) {
        for (i in detectors.indices) {
            if (detectors[i].enableDetectPhysicalSensorNetIn) {
                detectors[i].onPhysicalSensorNetIn(sensor)
            }
        }
    }

//    @UiThread
//    private fun notifyPhysicalSensorValueUpdate(sensor: PhysicalSensor, valuePosition: Int) {
//        for (i in detectors.indices) {
//            if (detectors[i].enableDetectPhysicalSensorValueUpdate) {
//                detectors[i].onPhysicalSensorDynamicValueUpdate(sensor, valuePosition)
//            }
//        }
//    }

    @UiThread
    private fun notifyMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        //为了提高效率
        var detector: Detector
        for (i in detectors.indices) {
            detector = detectors[i]
            if (detector.enableDetectMeasurementHistoryValueUpdate
                    && detector.enableUpdateMeasurementValue(measurement.id.id)) {
                detector.onMeasurementHistoryValueUpdate(measurement, valuePosition)
            }
        }
    }

    @UiThread
    private fun notifySensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
        //为了提高效率
        var detector: Detector
        for (i in detectors.indices) {
            detector = detectors[i]
            if (detector.enableDetectSensorInfoHistoryValueUpdate
                    && detector.enableUpdateMeasurementValue(info.id.id)) {
                detector.onSensorInfoHistoryValueUpdate(info, valuePosition)
            }
        }
    }

    @UiThread
    private fun notifyMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, measurementValuePosition: Int) {
        //为了提高效率
        var listener: Detector
        for (i in detectors.indices) {
            listener = detectors[i]
            if (listener.enableDetectMeasurementDynamicValueUpdate
                    && listener.enableUpdateMeasurementValue(measurement.id.id)) {
                listener.onMeasurementDynamicValueUpdate(measurement, measurementValuePosition)
            }
        }
    }

    @UiThread
    private fun notifySensorInfoDynamicValueUpdate(info: Sensor.Info, measurementValuePosition: Int) {
        //为了提高效率
        var listener: Detector
        for (i in detectors.indices) {
            listener = detectors[i]
            if (listener.enableDetectSensorInfoDynamicValueUpdate
                    && listener.enableUpdateMeasurementValue(info.id.id)) {
                listener.onSensorInfoDynamicValueUpdate(info, measurementValuePosition)
            }
        }
    }

//    private fun notifyEventOccurred(enable: Boolean, event: (listener: Detector) -> Unit) {
//        if (enable) {
//            var i = 0
//            while (i < detectors.size) {
//                event(detectors[i++])
//            }
//        }
//    }

    @UiThread
    fun payAttentionToSensor(sensor: Sensor) {
        //payAttentionToMeasurement(sensor.info)
        return if (sensor.id.isPhysicalSensor) {
            payAttentionToPhysicalSensor(sensor as PhysicalSensor)
        } else {
            payAttentionToLogicalSensor(sensor as LogicalSensor)
        }
    }

    @UiThread
    fun payAttentionToMeasurement(measurement: Measurement<*, *>) {
        focusedMeasurements[measurement.getId().id] = measurement
    }

    @UiThread
    fun payAttentionToPhysicalSensor(sensor: PhysicalSensor) {
        payAttentionToMeasurement(sensor.info)
        var measurement: DisplayMeasurement<*>
        for (i in 0 until sensor.displayMeasurementSize) {
            measurement = sensor.getDisplayMeasurementByPosition(i)
            if (measurement.id.isPracticalMeasurement) {
                payAttentionToMeasurement(measurement)
            }
        }
    }

    @UiThread
    fun payAttentionToLogicalSensor(sensor: LogicalSensor) {
        payAttentionToMeasurement(sensor.info)
        payAttentionToMeasurement(sensor.practicalMeasurement)
    }

    @UiThread
    fun payNoAttentionToMeasurement(measurement: Measurement<*, *>) {
        focusedMeasurements.remove(measurement.getId().id)
    }

    @UiThread
    fun payNoAttentionToSensor(sensor: Sensor) {
        //payNoAttentionToMeasurement(sensor.info)
        return if (sensor.id.isPhysicalSensor) {
            payNoAttentionToPhysicalSensor(sensor as PhysicalSensor)
        } else {
            payNoAttentionToLogicalSensor(sensor as LogicalSensor)
        }
    }

    @UiThread
    fun payNoAttentionToPhysicalSensor(sensor: PhysicalSensor) {
        payNoAttentionToMeasurement(sensor.info)
        var measurement: DisplayMeasurement<*>
        for (i in 0 until sensor.displayMeasurementSize) {
            measurement = sensor.getDisplayMeasurementByPosition(i)
            if (measurement.id.isPracticalMeasurement) {
                payNoAttentionToMeasurement(measurement)
            }
        }
    }

    @UiThread
    fun payNoAttentionToLogicalSensor(sensor: LogicalSensor) {
        payNoAttentionToMeasurement(sensor.info)
        payNoAttentionToMeasurement(sensor.practicalMeasurement)
    }

    private fun isInAttentionList(measurementId: Long): Boolean {
        return focusedMeasurements[measurementId] !== null
    }

    private fun isInAttentionList(address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int): Boolean {
        return isInAttentionList(ID.getId(address, dataTypeValue, dataTypeValueIndex))
    }

    fun init() {
        Sensor.setOnValueAchievedListener(sensorValueNotifierOnWorkThread)
    }

    fun release() {
        Sensor.setOnValueAchievedListener(null)
        detectors.clear()
        focusedMeasurements.clear()
        eventHandler.removeCallbacksAndMessages(null)
    }

    interface Detector {
        var enableDetectPhysicalSensorNetIn: Boolean
        //var enableDetectPhysicalSensorValueUpdate: Boolean
        var enableDetectLogicalSensorNetIn: Boolean
        //var enableDetectLogicalSensorValueUpdate: Boolean
        fun onPhysicalSensorNetIn(sensor: PhysicalSensor)
        //fun onPhysicalSensorDynamicValueUpdate(sensor: PhysicalSensor, infoValuePosition: Int)
        fun onLogicalSensorNetIn(sensor: LogicalSensor)
        //fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, measurementValuePosition: Int)

        var enableDetectSensorInfoHistoryValueUpdate: Boolean
        var enableDetectMeasurementHistoryValueUpdate: Boolean
        var enableDetectSensorInfoDynamicValueUpdate: Boolean
        var enableDetectMeasurementDynamicValueUpdate: Boolean
        fun enableUpdateMeasurementValue(measurementId: Long) = true
        fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int)
        fun onSensorInfoDynamicValueUpdate(info: Sensor.Info, valuePosition: Int)
        fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int)
        fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int)
    }
}
