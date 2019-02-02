package com.weisi.tool.wsnbox.bean.sorter

import com.cjq.lib.weisi.data.Sorter
import com.cjq.lib.weisi.iot.Sensor

/**
 * Created by CJQ on 2018/6/6.
 */
open class SensorIdSorter<S : Sensor> : Sorter<S>() {

    override fun compare(s1: S, s2: S): Int {
        return s1.id.compareTo(s2.id)
    }
}