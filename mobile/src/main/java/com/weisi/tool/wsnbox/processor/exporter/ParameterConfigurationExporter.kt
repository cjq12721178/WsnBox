package com.weisi.tool.wsnbox.processor.exporter

import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.io.file.Xml
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class ParameterConfigurationExporter(achiever: ResultAchiever<Boolean, Void>) : SafeAsyncTask<Any, Void, Boolean>(achiever) {
    override fun doInBackground(vararg params: Any?): Boolean {
        if (params.isEmpty()) {
            return false
        }
        val providerId = params[0] as? Long ?: return false
        val filePath = params[1] as? String ?: return false
        val provider = SensorDatabase.importMeasurementConfigurationProvider(providerId) ?: return false
        val devices = SensorDatabase.importDevicesWithNodes(providerId) ?: return false
        val type = SensorDatabase.getParameterConfigurationProviderType(providerId)
        return Xml.exportParameterConfiguration(type, provider, devices, filePath)
    }
}