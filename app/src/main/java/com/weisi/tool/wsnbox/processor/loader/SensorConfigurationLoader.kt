package com.weisi.tool.wsnbox.processor.loader

import android.content.Context
import android.support.v4.content.AsyncTaskLoader
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.io.database.SensorDatabase

class SensorConfigurationLoader(context: Context, private val sensorConfigId: Long) : AsyncTaskLoader<SensorConfiguration>(context) {

    override fun loadInBackground(): SensorConfiguration? {
        return SensorDatabase.importSensorConfiguration(sensorConfigId)
    }

    override fun onStartLoading() {
        forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }
}