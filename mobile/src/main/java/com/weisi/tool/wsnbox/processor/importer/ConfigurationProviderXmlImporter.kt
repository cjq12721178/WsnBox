package com.weisi.tool.wsnbox.processor.importer

import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class ConfigurationProviderXmlImporter(achiever: ResultAchiever<Int, Void>) : SafeAsyncTask<String, Void, Int>(achiever) {
    override fun doInBackground(vararg params: String?): Int {
        return SensorDatabase.insertValueContainerConfigurationProviderFromXml(params[0])
    }
}