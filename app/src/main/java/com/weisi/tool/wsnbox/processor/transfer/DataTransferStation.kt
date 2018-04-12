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

    private val MSG_LOGICAL_SENSOR_NET_IN = 1
    private val MSG_LOGICAL_SENSOR_VALUE_UPDATE = 2
    private val MSG_PHYSICAL_SENSOR_NET_IN = 3
    private val MSG_PHYSICAL_SENSOR_VALUE_UPDATE = 4
    private val MSG_LOGICAL_SENSOR_HISTORY_VALUE_RECEIVED = 5
    private val MSG_PHYSICAL_SENSOR_HISTORY_VALUE_RECEIVED = 6
    private val MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS = 7;
    private val MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS = 8;
    private val MSG_LOGICAL_SENSOR_HISTORY_DATA_ACCESS = 9;
    private val MSG_PHYSICAL_SENSOR_HISTORY_DATA_ACCESS = 10;
    private var lastNetInTimestamp: Long = 0
    private val LAST_NET_IN_TIME_LOCKER = Any()
    private val listeners = mutableListOf<OnEventListener>()
    private val focusedSensors = mutableMapOf<Long, Sensor<*, *>>()

    //以下4个属性须成对使用，交叉使用会出现意外情况
    var enableDetectPhysicalSensorNetIn = false
    var enableDetectPhysicalSensorValueUpdate = false
    var enableDetectLogicalSensorNetIn = false
    var enableDetectLogicalSensorValueUpdate = false

    var enableDetectPhysicalSensorHistoryValueReceive = false
    var enableDetectLogicalSensorHistoryValueReceive = false

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
                MSG_PHYSICAL_SENSOR_HISTORY_VALUE_RECEIVED ->
                    notifyPhysicalSensorHistoryValueUpdate(msg.obj as PhysicalSensor, msg.arg1)
                MSG_LOGICAL_SENSOR_HISTORY_VALUE_RECEIVED ->
                    notifyLogicalSensorHistoryValueUpdate(msg.obj as LogicalSensor, msg.arg1)
                MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS ->
                    processLogicalSensorDynamicDataAccess(msg.obj as SensorData)
                MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS ->
                    processPhysicalSensorDynamicDataAccess(msg.obj as SensorData)
                MSG_LOGICAL_SENSOR_HISTORY_DATA_ACCESS ->
                    processLogicalSensorHistoryDataAccess(msg.obj as SensorData)
                MSG_PHYSICAL_SENSOR_HISTORY_DATA_ACCESS ->
                    processPhysicalSensorHistoryDataAccess(msg.obj as SensorData)
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
            if (isInAttentionSensorList(address, dataTypeValue, dataTypeValueIndex)) {
                sendLogicalSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
            } else {
                val logicalSensor = SensorManager.getLogicalSensor(address, dataTypeValue, dataTypeValueIndex, true)
                val valueLogicalPosition = logicalSensor.addDynamicValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
                if (valueLogicalPosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
                    if (recordSensorNetIn(logicalSensor)) {
                        recordSensorNetIn(logicalSensor.physicalSensor)
                        if (enableDetectLogicalSensorNetIn) {
                            sendLogicalSensorNetInMessage(logicalSensor)
                        }
                    } else {
                        if (enableDetectLogicalSensorValueUpdate) {
                            sendLogicalSensorValueUpdateMessage(logicalSensor, valueLogicalPosition)
                        }
                    }
                }
            }
        } else {
            if (isInAttentionSensorList(address, dataTypeValue, dataTypeValueIndex)) {
                sendPhysicalSensorDynamicDataAccessMessage(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
            } else {
                val physicalSensor = SensorManager.getPhysicalSensor(address, true)
                val valueLogicalPosition = physicalSensor.addDynamicValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
                if (valueLogicalPosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
                    if (recordSensorNetIn(physicalSensor)) {
                        recordSensorNetIn(physicalSensor.getMeasurementByDataTypeValue(dataTypeValue, dataTypeValueIndex))
                        if (enableDetectPhysicalSensorNetIn) {
                            sendPhysicalSensorNetInMessage(physicalSensor)
                        }
                    } else {
                        if (enableDetectPhysicalSensorValueUpdate) {
                            sendPhysicalSensorValueUpdateMessage(physicalSensor, valueLogicalPosition)
                        }
                    }
                }
            }
        }
    }

    @UiThread
    private fun processLogicalSensorDynamicDataAccess(data: SensorData) {
        val logicalSensor = SensorManager.getLogicalSensor(data.id, true)
        val valueLogicalPosition = logicalSensor.addDynamicValue(data.dataTypeValue, data.dataTypeValueIndex, data.timestamp, data.batteryVoltage, data.rawValue)
        data.recycle()
        if (valueLogicalPosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
            if (recordSensorNetIn(logicalSensor)) {
                recordSensorNetIn(logicalSensor.physicalSensor)
                notifyLogicalSensorNetIn(logicalSensor)
            } else {
                notifyLogicalSensorValueUpdate(logicalSensor, valueLogicalPosition)
            }
        }
    }

    @UiThread
    private fun processPhysicalSensorDynamicDataAccess(data: SensorData) {
        val physicalSensor = SensorManager.getPhysicalSensor(data.address, true)
        val valueLogicalPosition = physicalSensor.addDynamicValue(data.dataTypeValue, data.dataTypeValueIndex, data.timestamp, data.batteryVoltage, data.rawValue)
        data.recycle()
        if (valueLogicalPosition != ValueContainer.ADD_FAILED_RETURN_VALUE) {
            if (recordSensorNetIn(physicalSensor)) {
                recordSensorNetIn(physicalSensor.getMeasurementByDataTypeValue(data.dataTypeValue, data.dataTypeValueIndex))
                notifyPhysicalSensorNetIn(physicalSensor)
            } else {
                notifyPhysicalSensorValueUpdate(physicalSensor, valueLogicalPosition)
            }
        }
    }

    private fun sendLogicalSensorDynamicDataAccessMessage(
            address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int,
            timestamp: Long, batteryVoltage: Float, rawValue: Double) {
        var sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        sendSensorDataAccessMessage(MSG_LOGICAL_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
    }

    private fun sendPhysicalSensorDynamicDataAccessMessage(
            address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int,
            timestamp: Long, batteryVoltage: Float, rawValue: Double) {
        var sensorData = SensorData.build(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue)
        sendSensorDataAccessMessage(MSG_PHYSICAL_SENSOR_DYNAMIC_DATA_ACCESS, sensorData)
    }

    private fun sendSensorDataAccessMessage(msg: Int, data: SensorData) {
        val message = Message.obtain()
        message.what = msg
        message.obj = data
        eventHandler.sendMessage(message)
    }

    @WorkerThread
    fun processPhysicalSensorHistoryDataAccess(address: Int, timestamp: Long, batteryVoltage: Float) {
        if (isInAttentionSensorList(address, 0, 0)) {
            sendPhysicalSensorHistoryDataAccessMessage(address, timestamp, batteryVoltage)
        } else {
            val sensor = SensorManager.getPhysicalSensor(address, true)
            val position = sensor.addPhysicalHistoryValue(timestamp, batteryVoltage)
            if (enableDetectPhysicalSensorHistoryValueReceive) {
                sendPhysicalSensorHistoryValueUpdateMessage(sensor, position)
            }
        }
    }

    @UiThread
    private fun processPhysicalSensorHistoryDataAccess(data: SensorData) {
        val sensor = SensorManager.getPhysicalSensor(data.address, true)
        val logicalPosition = sensor.addPhysicalHistoryValue(data.timestamp, data.batteryVoltage)
        data.recycle()
        notifyPhysicalSensorHistoryValueUpdate(sensor, logicalPosition)
    }

    private fun sendPhysicalSensorHistoryDataAccessMessage(address: Int, timestamp: Long, batteryVoltage: Float) {
        var sensorData = SensorData.build(address, 0, 0, timestamp, batteryVoltage, 0.0)
        sendSensorDataAccessMessage(MSG_PHYSICAL_SENSOR_HISTORY_DATA_ACCESS, sensorData)
    }

    @WorkerThread
    fun processLogicalSensorHistoryDataAccess(sensorId: Long, timestamp: Long, rawValue: Double) {
        if (isInAttentionSensorList(sensorId)) {
            sendLogicalSensorHistoryDataAccessMessage(sensorId, timestamp, rawValue)
        } else {
            val sensor = SensorManager.getLogicalSensor(sensorId, true)
            val position = sensor.addLogicalHistoryValue(timestamp, rawValue)
            if (enableDetectLogicalSensorHistoryValueReceive) {
                sendLogicalSensorHistoryValueUpdateMessage(sensor, position)
            }
        }
    }

    private fun sendLogicalSensorHistoryDataAccessMessage(sensorId: Long, timestamp: Long, rawValue: Double) {
        sendSensorDataAccessMessage(MSG_LOGICAL_SENSOR_HISTORY_DATA_ACCESS,
                SensorData.build(sensorId, timestamp, 0f, rawValue))
    }

    @UiThread
    private fun processLogicalSensorHistoryDataAccess(data: SensorData) {
        val sensor = SensorManager.getLogicalSensor(data.id, true)
        val position = sensor.addLogicalHistoryValue(data.timestamp, data.rawValue)
        data.recycle()
        notifyLogicalSensorHistoryValueUpdate(sensor, position)
    }

    private fun recordSensorNetIn(sensor: Sensor<*, *>): Boolean {
        if (sensor.getNetInTimestamp() == 0L) {
            synchronized(LAST_NET_IN_TIME_LOCKER) {
                var currentNetInTimestamp = System.currentTimeMillis()
                if (lastNetInTimestamp >= currentNetInTimestamp) {
                    currentNetInTimestamp = lastNetInTimestamp + 1
                }
                sensor.setNetInTimestamp(currentNetInTimestamp)
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
//        val message = Message.obtain()
//        message.what = MSG_LOGICAL_SENSOR_VALUE_UPDATE
//        message.obj = sensor
//        message.arg1 = valueLogicalPosition
//        eventHandler.sendMessage(message)
        sendSensorValueUpdateMessage(MSG_LOGICAL_SENSOR_VALUE_UPDATE, sensor, valueLogicalPosition)

    }

    private fun sendPhysicalSensorNetInMessage(sensor: PhysicalSensor) {
        val message = Message.obtain()
        message.what = MSG_PHYSICAL_SENSOR_NET_IN
        message.obj = sensor
        eventHandler.sendMessage(message)
    }

    private fun sendPhysicalSensorValueUpdateMessage(sensor: PhysicalSensor, valueLogicalPosition: Int) {
//        val message = Message.obtain()
//        message.what = MSG_PHYSICAL_SENSOR_VALUE_UPDATE
//        message.obj = sensor
//        message.arg1 = valueLogicalPosition
//        eventHandler.sendMessage(message)
        sendSensorValueUpdateMessage(MSG_PHYSICAL_SENSOR_VALUE_UPDATE, sensor, valueLogicalPosition)
    }

    private fun sendSensorValueUpdateMessage(messageType: Int, sensor: Sensor<*, *>, valuePosition: Int) {
        val message = Message.obtain()
        message.what = messageType
        message.obj = sensor
        message.arg1 = valuePosition
        eventHandler.sendMessage(message)
    }

    private fun sendLogicalSensorHistoryValueUpdateMessage(sensor: LogicalSensor, valuePosition: Int) {
        sendSensorValueUpdateMessage(MSG_LOGICAL_SENSOR_HISTORY_VALUE_RECEIVED, sensor, valuePosition)
    }

    private fun sendPhysicalSensorHistoryValueUpdateMessage(sensor: PhysicalSensor, valuePosition: Int) {
        sendSensorValueUpdateMessage(MSG_PHYSICAL_SENSOR_HISTORY_VALUE_RECEIVED, sensor, valuePosition)
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
    private fun notifyLogicalSensorHistoryValueUpdate(sensor: LogicalSensor, valuePosition: Int) {
        if (enableDetectLogicalSensorHistoryValueReceive) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onLogicalSensorHistoryValueUpdate(sensor, valuePosition)
            }
        }
    }

    @UiThread
    private fun notifyPhysicalSensorHistoryValueUpdate(sensor: PhysicalSensor, valuePosition: Int) {
        if (enableDetectPhysicalSensorHistoryValueReceive) {
            //为了提高效率
            var i = 0
            while (i < listeners.size) {
                listeners[i++].onPhysicalSensorHistoryValueUpdate(sensor, valuePosition)
            }
        }
    }

    @UiThread
    fun payAttentionToSensor(sensor: Sensor<*,*>) {
        focusedSensors[sensor.getId().id] = sensor
    }

    @UiThread
    fun payAttentionToSensor(physicalSensor: PhysicalSensor) {
        focusedSensors[physicalSensor.id.id] = physicalSensor
        physicalSensor.measurementCollections.forEach { focusedSensors[it.id.id] = it }
    }

    @UiThread
    fun payNoAttentionToSensor(sensor: Sensor<*, *>) {
        focusedSensors.remove(sensor.getId().id)
    }

    @UiThread
    fun payNoAttentionToSensor(physicalSensor: PhysicalSensor) {
        focusedSensors.remove(physicalSensor.id.id)
        physicalSensor.measurementCollections.forEach { focusedSensors.remove(it.id.id) }
    }

    private fun isInAttentionSensorList(sensorId: Long): Boolean {
        return focusedSensors[sensorId] != null
    }

    private fun isInAttentionSensorList(address: Int, dataTypeValue: Byte, dataTypeValueIndex: Int): Boolean {
        return isInAttentionSensorList(Sensor.ID.getId(address, dataTypeValue, dataTypeValueIndex))
    }

    fun release() {
        listeners.clear()
        focusedSensors.clear()
        eventHandler.removeCallbacksAndMessages(null);
    }

    interface OnEventListener {
        fun onPhysicalSensorNetIn(sensor: PhysicalSensor)
        fun onPhysicalSensorDynamicValueUpdate(sensor: PhysicalSensor, logicalPosition: Int)
        fun onLogicalSensorNetIn(sensor: LogicalSensor)
        fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, logicalPosition: Int)
        fun onPhysicalSensorHistoryValueUpdate(sensor: PhysicalSensor, logicalPosition: Int)
        fun onLogicalSensorHistoryValueUpdate(sensor: LogicalSensor, logicalPosition: Int)
    }
}
