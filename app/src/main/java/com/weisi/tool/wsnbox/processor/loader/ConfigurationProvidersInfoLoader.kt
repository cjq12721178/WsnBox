package com.weisi.tool.wsnbox.processor.loader

import android.content.Context
import android.database.Cursor
import com.cjq.tool.qbox.ui.loader.SimpleCursorLoader
import com.weisi.tool.wsnbox.io.database.SensorDatabase

class ConfigurationProvidersInfoLoader(context: Context) : SimpleCursorLoader(context) {

    override fun loadInBackground(): Cursor? {
        return SensorDatabase.importValueContainerConfigurationProviders()
    }
}