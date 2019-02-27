package com.weisi.tool.wsnbox.processor

import com.cjq.lib.weisi.iot.SensorManager
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class SensorConfigurationsChanger(achiever: ResultAchiever<SensorManager.MeasurementConfigurationProvider?, SensorManager.MeasurementConfigurationProvider>) : SafeAsyncTask<Long, SensorManager.MeasurementConfigurationProvider, SensorManager.MeasurementConfigurationProvider?>(achiever) {
    override fun doInBackground(vararg params: Long?): SensorManager.MeasurementConfigurationProvider? {
        if (params.isEmpty()) {
            return null
        }
        val providerId = params[0] ?: 0L
        val provider = SensorDatabase.importMeasurementConfigurationProvider(providerId)
        SensorManager.setValueContainerConfigurationProvider(provider, true)
        return provider
    }
}