package com.weisi.tool.wsnbox.bean.filter

import com.cjq.lib.weisi.data.Filter
import com.weisi.tool.wsnbox.bean.data.Device


/**
 * Created by CJQ on 2018/6/11.
 */
class DeviceNodeInfoFilter(var keyWord: String = "") : Filter<Device> {

    override fun match(device: Device): Boolean {
        if (device.name.contains(keyWord)) {
            return true
        }
        var i = 0
        val size = device.nodes.size
        while (i < size) {
            if ((device.nodes[i].name ?: device.nodes[i].measurement.defaultName).contains(keyWord)) {
                return true
            }
            ++i
        }
        return false
    }
}