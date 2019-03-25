package com.weisi.tool.wsnbox.adapter.browse

import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.tool.qbox.ui.adapter.AdapterDelegateManager
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter

/**
 * Created by CJQ on 2018/5/29.
 */
class DataBrowsePhysicalSensorAdapter(m: AdapterDelegateManager<PhysicalSensor>,
                                      s: Storage<PhysicalSensor>) : RecyclerViewBaseAdapter<PhysicalSensor>(m) {

    private val storage = s

    override fun getItemByPosition(position: Int): PhysicalSensor {
        return storage.get(position)
    }

    override fun getItemCount(): Int {
        return storage.size()
    }

    override fun getItemViewType(position: Int): Int {
        return getItemByPosition(position)
                .displayMeasurementSize - 1
    }
}