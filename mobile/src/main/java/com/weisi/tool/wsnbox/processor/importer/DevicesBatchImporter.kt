package com.weisi.tool.wsnbox.processor.importer

import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class DevicesBatchImporter(achiever: ResultAchiever<List<Device>?, Void>) : SafeAsyncTask<Long, Void, List<Device>?>(achiever) {
    override fun doInBackground(vararg params: Long?): List<Device>? {
        if (params.isEmpty()) {
            return null
        }
        val providerId = params[0] ?: return null
        return SensorDatabase.importDevicesWithNodes(providerId)
    }
}