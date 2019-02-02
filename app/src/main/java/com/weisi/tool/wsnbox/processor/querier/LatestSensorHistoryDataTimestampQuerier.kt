package com.weisi.tool.wsnbox.processor.querier

import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class LatestSensorHistoryDataTimestampQuerier(achiever: ResultAchiever<Long, Void>) : SafeAsyncTask<Sensor, Void, Long>(achiever) {
    override fun doInBackground(vararg params: Sensor?): Long {
        val s = if (params.isNotEmpty()) {
            params[0]
        } else {
            return -1L
        }
        return if (s is Sensor) {
            SensorDatabase.getLatestSensorHistoryDataTimestamp(s)
        } else {
            -1L
        }
    }
}