package com.weisi.tool.wsnbox.processor.importer

import com.cjq.lib.weisi.iot.SensorManager
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class SensorConfigurationsImporter(achiever: ResultAchiever<SensorManager.MeasurementConfigurationProvider?, SensorManager.MeasurementConfigurationProvider>) : SafeAsyncTask<Long, SensorManager.MeasurementConfigurationProvider, SensorManager.MeasurementConfigurationProvider?>(achiever) {
    override fun doInBackground(vararg params: Long?): SensorManager.MeasurementConfigurationProvider? {
        if (params.isEmpty()) {
            return null
        }
        val provider = SensorDatabase.importMeasurementConfigurationProvider(params[0]!!)
        SensorManager.setValueContainerConfigurationProvider(provider, true)
        return provider
    }
}