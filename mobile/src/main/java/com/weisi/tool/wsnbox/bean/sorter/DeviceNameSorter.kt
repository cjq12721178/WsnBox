package com.weisi.tool.wsnbox.bean.sorter

import com.cjq.lib.weisi.data.Sorter
import com.weisi.tool.wsnbox.bean.data.Device

/**
 * Created by CJQ on 2018/6/8.
 */
class DeviceNameSorter : Sorter<Device>() {

    override fun compare(d1: Device, d2: Device): Int {
        return d1.name.compareTo(d2.name)
    }
}