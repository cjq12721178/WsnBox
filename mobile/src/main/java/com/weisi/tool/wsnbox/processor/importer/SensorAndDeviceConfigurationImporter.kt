package com.weisi.tool.wsnbox.processor.importer

import com.cjq.lib.weisi.iot.SensorManager
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class SensorAndDeviceConfigurationImporter(achiever: ResultAchiever<List<Device>, Void>) : SafeAsyncTask<Long, Void, List<Device>>(achiever) {

    override fun doInBackground(vararg params: Long?): List<Device>? {
        if (params.isEmpty()) {
            return null
        }
        val providerId = params[0] ?: return null
        val provider = SensorDatabase.importMeasurementConfigurationProvider(providerId) ?: return null
        SensorManager.setValueContainerConfigurationProvider(provider, true)
        return SensorDatabase.importDevicesWithNodes(providerId)
    }

//    override fun onPostExecute(result: List<Device>?, activity: DemonstrationActivity) {
//        activity.displayDemo(result)
//    }
}