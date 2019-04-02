package com.weisi.tool.wsnbox.processor.importer

import com.weisi.tool.wsnbox.bean.data.ParameterConfigurationProvider
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class ParameterConfigurationProviderImporter(achiever: ResultAchiever<ParameterConfigurationProvider?, Void>) : SafeAsyncTask<Long, Void, ParameterConfigurationProvider?>(achiever) {
    override fun doInBackground(vararg params: Long?): ParameterConfigurationProvider? {
        if (params.isEmpty()) {
            return null
        }
        val providerId = params[0] ?: return null
        val devices = SensorDatabase.importDevicesWithNodes(providerId) ?: return null
        val type = SensorDatabase.getParameterConfigurationProviderType(providerId)
        return ParameterConfigurationProvider(type, devices)
    }
}