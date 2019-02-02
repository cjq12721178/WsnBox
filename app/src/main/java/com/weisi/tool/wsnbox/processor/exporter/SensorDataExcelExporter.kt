package com.weisi.tool.wsnbox.processor.exporter

import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.util.SafeAsyncTask

class SensorDataExcelExporter(achiever: ResultAchiever<Boolean, Unit>) : SafeAsyncTask<String, Unit, Boolean>(achiever) {
    override fun doInBackground(vararg params: String?): Boolean {
        return SensorDatabase.exportSensorDataToExcel(params[0])
    }
}