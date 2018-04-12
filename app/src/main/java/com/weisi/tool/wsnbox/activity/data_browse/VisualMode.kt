package com.weisi.tool.wsnbox.activity.data_browse

import android.os.Bundle
import android.view.View
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import com.weisi.tool.wsnbox.service.DataPrepareService

/**
 * Created by CJQ on 2018/3/15.
 */
interface VisualMode : View.OnClickListener {
    fun onCreate(activity: DataBrowseActivity, savedInstanceState: Bundle?)
    fun onDataSourceMenuItemClick(realTime: Boolean)
    fun onSaveInstanceState(outState: Bundle?)
    fun onDestroy()
    fun onServiceConnectionCreate(service: DataPrepareService)
    fun onServiceConnectionStart(service: DataPrepareService)
    fun onServiceConnectionStop(service: DataPrepareService)
    fun onServiceConnectionDestroy(service: DataPrepareService)
    fun onValueContainerConfigurationsImported(processor: CommonWarnProcessor<View>)
    fun onItemClick(v: View, position: Int)
}