package com.weisi.tool.wsnbox.processor.importer

import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class DevicesOneByOneImporter(achiever: ResultAchiever<Boolean, Device>) : SafeAsyncTask<Long, Device, Boolean>(achiever) {

    override fun doInBackground(vararg params: Long?): Boolean {
        return SensorDatabase.importDevicesWithNodes(params[0] as Long) {
            publishProgress(it)
        }
    }
}