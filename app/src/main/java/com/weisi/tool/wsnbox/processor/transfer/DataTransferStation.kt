package com.weisi.tool.wsnbox.processor.transfer

import android.os.Handler
import android.os.Message
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import com.cjq.lib.weisi.iot.*
import com.cjq.lib.weisi.iot.container.ValueContainer
import com.weisi.tool.wsnbox.bean.data.SensorData


/**
 * Created by CJQ on 2018/3/29.
 */

class DataTransferStation {

    private val MSG_LOGICAL_SENSOR_NET_IN = 1
    private val MSG_LOGICAL_SENSOR_VALUE_UPDATE = 2
    private val MSG_PHYSICAL_SENSOR_NET_IN = 3
    private val MSG_PHYSICAL_SENSOR_VALUE_UPDATE = 4
    private val MSG_MEASUREMENT_HISTORY_VALUE_RECEIVED = 5
    private val MSG_SENSOR_INFO_HISTORY_VALUE_RECEIVED = 6
    private val MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS = 7;
    private val MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS = 8;
    private val MSG_MEASUREMENT_HISTORY_DATA_ACCESS = 9;
    private val MSG_SENSOR_INFO_HISTORY_DATA_ACCESS = 10;
    private var lastNetInTimestamp: Long = 0
    private val LAST_NET_IN_TIME_LOCKER = Any()
    private val listeners = mutableListOf<OnEventListener>()
    private val focusedMeasurements = mutableMapOf<Long, Measurement<*, *>>()

    //以下4个属性须成对使用，交叉使用会出现意外情况
    var enableDetectPhysicalSensorNetIn = false
    var enableDetectPhysicalSensorValueUpdate = false
    var enableDetectLogicalSensorNetIn = false
    var enableDetectLogicalSensorValueUpdate = false

    var enableDetectSensorInfoHistoryValueReceive = false
    var enableDetectMeasurementHistoryValueReceive = false

    private val eventHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                MSG_PHYSICAL_SENSOR_NET_IN ->
                    notifyPhysicalSensorNetIn(msg.obj as PhysicalSensor)
                MSG_PHYSICAL_SENSOR_VALUE_UPDATE ->
                    notifyPhysicalSensorValueUpdate(msg.obj as PhysicalSensor, msg.arg1)
                MSG_LOGICAL_SENSOR_NET_IN ->
                    notifyLogicalSensorNetIn(msg.obj as LogicalSensor)
                MSG_LOGICAL_SENSOR_VALUE_UPDATE ->
                    notifyLogicalSensorValueUpdate(msg.obj as LogicalSensor, msg.arg1)
                MSG_SENSOR_INFO_HISTORY_VALUE_RECEIVED ->
                    notifySensorInfoHistoryValueUpdate(msg.obj as Sensor.Info, msg.arg1)
                MSG_MEASUREMENT_HISTORY_VALUE_RECEIVED ->
                    notifyMeasurementHistoryValueUpdate(msg.obj as PracticalMeasurement, msg.arg1)
                MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS ->
                    processLogicalSensorDynamicDataAccess(msg.obj as SensorData)
                MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS ->
                    processPhysicalSensorDynamicDataAccess(msg.obj as SensorData)
                MSG_MEASUREMENT_HISTORY_DATA_ACCESS ->
                    processMeasurementHistoryDataAccess(msg.obj as SensorData)
                MSG_SENSOR_INFO_HISTORY_DATA_ACCESS ->
                    processSensorInfoHistoryDataAccess(msg.obj as SensorData)
            }
        }
    }

    fun register(listener: OnEventListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun unregister(listener: OnEventListener) {
        listeners.remove(listener)
    }

    @WorkerThread
    fun processSensorDynamicDataAccess(address: Int,
                                       dataTypeValue: Byte,
                                       dataTypeValueIndex: Int,
                                       timestamp: Long,
                                       batteryVoltage: Float,
                                       rawValue: Double) {
        if (enableDetectLogicalSensorNetIn || enableDetectLogicalSensorValueUpdate) {
            if (isInAttentionList(address, dataTypeValue, dataTypeValueIndex)) {
                sendLogicalSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
            } else {
                val logicalSensor = SensorManager.getLogicalSensor(address, dataTypeValue, dataTypeValueIndex) ?: return
                val measurementValuePosition = logicalSensor.addDynamicValue(timestamp, batteryVoltage, rawValue)
                if (measurementValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
                    if (recordSensorNetIn(logicalSensor)) {
                        recordSensorNetIn(SensorManager.getPhysicalSensor(address))
                        if (enableDetectLogicalSensorNetIn) {
                            sendLogicalSensorNetInMessage(logicalSensor)
                        } else if (enableDetectLogicalSensorValueUpdate) {
                            sendLogicalSensorValueUpdateMessage(logicalSensor, measurementValuePosition)
                        }
                    } else {
                        if (enableDetectLogicalSensorValueUpdate) {
                            sendLogicalSensorValueUpdateMessage(logicalSensor, measurementValuePosition)
                        }
                    }
                }
            }
        } else {
            if (isInAttentionList(address, dataTypeValue, dataTypeValueIndex)) {
                sendPhysicalSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
            } else {
                val physicalSensor = SensorManager.getPhysicalSensor(address)
                val infoValuePosition = physicalSensor.addDynamicValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
                if (infoValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
                    if (recordSensorNetIn(physicalSensor)) {
                        recordSensorNetIn(SensorManager.getLogicalSensor(address, dataTypeValue, dataTypeValueIndex))
                        if (enableDetectPhysicalSensorNetIn) {
                            sendPhysicalSensorNetInMessage(physicalSensor)
                        } else if (enableDetectPhysicalSensorValueUpdate) {
                            sendPhysicalSensorValueUpdateMessage(physicalSensor, infoValuePosition)
                        }
                    } else {
                        recordSensorNetIn(SensorManager.getLogicalSensor(address, dataTypeValue, dataTypeValueIndex))
                        if (enableDetectPhysicalSensorValueUpdate) {
                            sendPhysicalSensorValueUpdateMessage(physicalSensor, infoValuePosition)
                        }
                    }
                }
            }
        }
    }

    @UiThread
    private fun processLogicalSensorDynamicDataAccess(data: SensorData) {
        val logicalSensor = SensorManager.getLogicalSensor(data.id) ?: return
        val measurementValuePosition = logicalSensor.addDynamicValue(data.timestamp, data.batteryVoltage, data.rawValue)
        data.recycle()
        if (measurementValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
            if (recordSensorNetIn(logicalSensor)) {
                recordSensorNetIn(SensorManager.getPhysicalSensor(data.address))
                notifyLogicalSensorNetIn(logicalSensor)
            } else {
                notifyLogicalSensorValueUpdate(logicalSensor, measurementValuePosition)
            }
        }
    }

    @UiThread
    private fun processPhysicalSensorDynamicDataAccess(data: SensorData) {
        val physicalSensor = SensorManager.getPhysicalSensor(data.address)
        val infoValuePosition = physicalSensor.addDynamicValue(data.dataTypeValue, data.dataTypeValueIndex, data.timestamp, data.batteryVoltage, data.rawValue)
        data.recycle()
        if (infoValuePosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
            if (recordSensorNetIn(physicalSensor)) {
                recordSensorNetIn(SensorManager.getLogicalSensor(data.address, data.dataTypeValue, data.dataTypeValueIndex))
                notifyPhysicalSensorNetIn(physicalSensor)
            } else {
                notifyPhysicalSensorValueUpdate(physicalSensor, infoValuePosition)
            }
        }
    }

    private fun sendLogicalSensorDynamicDataAccessMessage(
            address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int,
            timestamp: Long, batteryVoltage: Float, rawValue: Double) {
        var sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        sendDataAccessMessage(MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
    }

    private fun sendPhysicalSensorDynamicDataAccessMessage(
            address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int,
            timestamp: Long, batteryVoltage: Float, rawValue: Double) {
        var sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        sendDataAccessMessage(MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
    }

    private fun sendDataAccessMessage(msg: Int, data: SensorData) {
        val message = Message.obtain()
        message.what = msg
        message.obj = data
        eventHandler.sendMessage(message)
    }

    @WorkerThread
    fun processSensorInfoHistoryDataAccess(address: Int, timestamp: Long, batteryVoltage: Float) {
        if (isInAttentionList(address, 0, 0)) {
            sendSensorInfoHistoryDataAccessMessage(address, timestamp, batteryVoltage)
        } else {
//            val sensor = SensorManager.getPhysicalSensor(address)
//            val position = sensor.addInfoHistoryValue(timestamp, batteryVoltage)
//            if (enableDetectSensorInfoHistoryValueReceive) {
//                sendSensorInfoHistoryValueUpdateMessage(sensor, position)
//            }
            val info = SensorManager.getSensorInfo(address)
            val position = info.addHistoryValue(timestamp, batteryVoltage)
            if (enableDetectSensorInfoHistoryValueReceive) {
                sendSensorInfoHistoryValueUpdateMessage(info, position)
            }
        }
    }

    @UiThread
    private fun processSensorInfoHistoryDataAccess(data: SensorData) {
//        val sensor = SensorManager.getPhysicalSensor(data.address)
//        val infoValuePosition = sensor.addInfoHistoryValue(data.timestamp, data.batteryVoltage)
//        data.recycle()
//        notifySensorInfoHistoryValueUpdate(sensor, infoValuePosition)
        val info = SensorManager.getSensorInfo(data.address)
        val position = info.addHistoryValue(data.timestamp, data.batteryVoltage)
        data.recycle()
        notifySensorInfoHistoryValueUpdate(info, position)
    }

    private fun sendSensorInfoHistoryDataAccessMessage(address: Int, timestamp: Long, batteryVoltage: Float) {
        var sensorData = SensorData.build(address, 0, 0, timestamp, batteryVoltage, 0.0)
        sendDataAccessMessage(MSG_SENSOR_INFO_HISTORY_DATA_ACCESS, sensorData)
    }

    @WorkerThread
    fun processMeasurementHistoryDataAccess(id: Long, timestamp: Long, rawValue: Double) {
        if (isInAttentionList(id)) {
            sendMeasurementHistoryDataAccessMessage(id, timestamp, rawValue)
        } else {
            //val sensor = SensorManager.getLogicalSensor(id, true)
//            val position = sensor.addLogicalHistoryValue(timestamp, rawValue)
//            if (enableDetectMeasurementHistoryValueReceive) {
//                sendMeasurementHistoryValueUpdateMessage(sensor, position)
//            }
            val measurement = SensorManager.getPracticalMeasurement(id) ?: return
            val position = measurement.addHistoryValue(timestamp, rawValue)
            if (enableDetectMeasurementHistoryValueReceive) {
                sendMeasurementHistoryValueUpdateMessage(measurement, position)
            }
        }
    }

    private fun sendMeasurementHistoryDataAccessMessage(sensorId: Long, timestamp: Long, rawValue: Double) {
        sendDataAccessMessage(MSG_MEASUREMENT_HISTORY_DATA_ACCESS,
                SensorData.build(sensorId, timestamp, 0f, rawValue))
    }

    @UiThread
    private fun processMeasurementHistoryDataAccess(data: SensorData) {
//        val sensor = SensorManager.getLogicalSensor(data.id, true)
//        val position = sensor.addLogicalHistoryValue(data.timestamp, data.rawValue)
//        data.recycle()
//        notifyMeasurementHistoryValueUpdate(sensor, position)
        val measurement = SensorManager.getPracticalMeasurement(data.id) ?: return
        val position = measurement.addHistoryValue(data.timestamp, data.rawValue)
        data.recycle()
        notifyMeasurementHistoryValueUpdate(measurement, position)
    }

    private fun recordSensorNetIn(sensor: Sensor?): Boolean {
        if (sensor?.netInTimestamp == 0L) {
            synchronized(LAST_NET_IN_TIME_LOCKER) {
                var currentNetInTimestamp = System.currentTimeMillis()
                if (lastNetInTimestamp >= currentNetInTimestamp) {
                    currentNetInTimestamp = lastNetInTimestamp + 1
                }
                sensor.netInTimestamp = currentNetInTimestamp
                lastNetInTimestamp = currentNetInTimestamp
            }
            return true
        }
        return false
    }

    private fun sendLogicalSensorNetInMessage(sensor: LogicalSensor) {
        val message = Message.obtain()
        message.what = MSG_LOGICAL_SENSOR_NET_IN
        message.obj = sensor
        eventHandler.sendMessage(message)
    }

    private fun sendLogicalSensorValueUpdateMessage(sensor: LogicalSensor, valueLogicalPosition: Int) {
        sendValueUpdateMessage(MSG_LOGICAL_SENSOR_VALUE_UPDATE, sensor, valueLogicalPosition)
    }

    private fun sendPhysicalSensorNetInMessage(sensor: PhysicalSensor) {
        val message = Message.obtain()
        message.what = MSG_PHYSICAL_SENSOR_NET_IN
        message.obj = sensor
        eventHandler.sendMessage(message)
    }

    private fun sendPhysicalSensorValueUpdateMessage(sensor: PhysicalSensor, valueLogicalPosition: Int) {
        sendValueUpdateMessage(MSG_PHYSICAL_SENSOR_VALUE_UPDATE, sensor, valueLogicalPosition)
    }

    private fun sendValueUpdateMessage(messageType: Int, target: Any, valuePosition: Int) {
        val message = Message.obtain()
        message.what = messageType
        message.obj = target
        message.arg1 = valuePosition
        eventHandler.sendMessage(message)
    }

    private fun sendMeasurementHistoryValueUpdateMessage(measurement: PracticalMeasurement, valuePosition: Int) {
        sendValueUpdateMessage(MSG_MEASUREMENT_HISTORY_VALUE_RECEIVED, measurement, valuePosition)
    }

    private fun sendSensorInfoHistoryValueUpdateMessage(info: Sensor.Info, valuePosition: Int) {
        sendValueUpdateMessage(MSG_SENSOR_INFO_HISTORY_VALUE_RECEIVED, info, valuePosition)
    }

    @UiThread
    private fun notifyLogicalSensorNetIn(sensor: LogicalSensor) {
        if (enableDetectLogicalSensorNetIn) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onLogicalSensorNetIn(sensor)
            }
        }
    }

    @UiThread
    private fun notifyLogicalSensorValueUpdate(sensor: LogicalSensor, valuePosition: Int) {
        if (enableDetectLogicalSensorValueUpdate) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onLogicalSensorDynamicValueUpdate(sensor, valuePosition)
            }
        }
    }

    @UiThread
    private fun notifyPhysicalSensorNetIn(sensor: PhysicalSensor) {
        if (enableDetectPhysicalSensorNetIn) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onPhysicalSensorNetIn(sensor)
            }
        }
    }

    @UiThread
    private fun notifyPhysicalSensorValueUpdate(sensor: PhysicalSensor, valuePosition: Int) {
        if (enableDetectPhysicalSensorValueUpdate) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onPhysicalSensorDynamicValueUpdate(sensor, valuePosition)
            }
        }
    }

    @UiThread
    private fun notifyMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (enableDetectMeasurementHistoryValueReceive) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onMeasurementHistoryValueUpdate(measurement, valuePosition)
            }
        }
    }

    @UiThread
    private fun notifySensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
        if (enableDetectSensorInfoHistoryValueReceive) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onSensorInfoHistoryValueUpdate(info, valuePosition)
            }
        }
    }

    @UiThread
    fun payAttentionToSensor(sensor: Sensor) {
        return if (sensor.id.isPhysicalSensor) {
            payAttentionToPhysicalSensor(sensor as PhysicalSensor)
        } else {
            payAttentionToLogicalSensor(sensor as LogicalSensor)
        }
    }

    @UiThread
    private fun payAttentionToMeasurement(measurement: Measurement<*, *>) {
        focusedMeasurements[measurement.getId().id] = measurement
    }

    @UiThread
    fun payAttentionToPhysicalSensor(sensor: PhysicalSensor) {
        payAttentionToMeasurement(sensor.info)
        //focusedMeasurements[sensor.id.id] = sensor
        //sensor.measurementCollections.forEach { focusedMeasurements[it.id.id] = it }
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
    private fun payNoAttentionToMeasurement(measurement: Measurement<*, *>) {
        focusedMeasurements.remove(measurement.getId().id)
    }

    @UiThread
    fun payNoAttentionToSensor(sensor: Sensor) {
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
        return focusedMeasurements[measurementId] != null
    }

    private fun isInAttentionList(address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int): Boolean {
        return isInAttentionList(ID.getId(address, dataTypeValue, dataTypeValueIndex))
    }

    fun release() {
        listeners.clear()
        focusedMeasurements.clear()
        eventHandler.removeCallbacksAndMessages(null);
    }

    interface OnEventListener {
        fun onPhysicalSensorNetIn(sensor: PhysicalSensor)
        fun onPhysicalSensorDynamicValueUpdate(sensor: PhysicalSensor, infoValuePosition: Int)
        fun onLogicalSensorNetIn(sensor: LogicalSensor)
        fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, measurementValuePosition: Int)
        fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int)
        fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int)
    }
}
