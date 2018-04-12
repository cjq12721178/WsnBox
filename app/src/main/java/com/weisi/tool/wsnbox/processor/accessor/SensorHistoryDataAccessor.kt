package com.weisi.tool.wsnbox.processor.accessor

import android.os.AsyncTask
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.SensorManager
import com.weisi.tool.wsnbox.io.database.SensorDatabase

/**
 * Created by CJQ on 2018/4/2.
 */
class SensorHistoryDataAccessor {

    private var onSensorHistoryDataAccessListener: OnSensorHistoryDataAccessListener? = null

    fun setOnSensorHistoryDataAccessListener(listener: OnSensorHistoryDataAccessListener?) {
        onSensorHistoryDataAccessListener = listener
    }

    fun importSensorsWithEarliestValue(listener: OnMissionFinishedListener?) {
        object : AsyncTask<Unit, Unit, Boolean>(), SensorDatabase.SensorHistoryInfoReceiver {
            override fun doInBackground(vararg params: Unit?): Boolean {
                val actualCount = SensorDatabase.getSensorWithHistoryValueCount()
                if (actualCount == -1) {
                    return false
                }
                return if (actualCount == 0 || actualCount == SensorManager.getSensorWithHistoryValuesCount(PhysicalSensor::class.java)) {
                    true
                } else SensorDatabase.importSensorEarliestValue(this)
            }

            override fun onPostExecute(result: Boolean?) {
                listener?.onMissionFinished(result!!)
            }

            override fun onSensorDataReceived(address: Int, timestamp: Long, batteryVoltage: Float) {
                notifyPhysicalSensorHistoryDataAccess(address, timestamp, batteryVoltage)
            }

            override fun onMeasurementDataReceived(measurementValueId: Long, timestamp: Long, rawValue: Double) {
                notifyLogicalSensorHistoryDataAccess(measurementValueId, timestamp, rawValue)
            }
        }.execute()
    }

    fun importSensorHistoryValue(address: Int, startTime: Long, endTime: Long, listener: OnMissionFinishedListener?) {
        object : AsyncTask<Unit, Unit, Boolean>(), SensorDatabase.SensorHistoryInfoReceiver {
            override fun doInBackground(vararg params: Unit?): Boolean {
                return SensorDatabase.importSensorHistoryValues(
                        address, startTime, endTime,
                        0,
                        this)
            }

            override fun onSensorDataReceived(address: Int, timestamp: Long, batteryVoltage: Float) {
                notifyPhysicalSensorHistoryDataAccess(address, timestamp, batteryVoltage)
            }

            override fun onMeasurementDataReceived(measurementValueId: Long, timestamp: Long, rawValue: Double) {
                notifyLogicalSensorHistoryDataAccess(measurementValueId, timestamp, rawValue)
            }

            override fun onPostExecute(result: Boolean?) {
                listener?.onMissionFinished(result!!)
            }
        }.execute()
    }

    private fun notifyLogicalSensorHistoryDataAccess(measurementValueId: Long, timestamp: Long, rawValue: Double) {
        onSensorHistoryDataAccessListener?.onLogicalSensorHistoryDataAccess(measurementValueId, timestamp, rawValue)
    }

    private fun notifyPhysicalSensorHistoryDataAccess(address: Int, timestamp: Long, batteryVoltage: Float) {
        onSensorHistoryDataAccessListener?.onPhysicalSensorHistoryDataAccess(address, timestamp, batteryVoltage)
    }

    interface OnMissionFinishedListener {
        fun onMissionFinished(result: Boolean)
    }
}