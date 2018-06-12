package com.weisi.tool.wsnbox.bean.storage

import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.Sensor

/**
 * Created by CJQ on 2018/5/28.
 */
class SensorStorage<S : Sensor<*, *>>(provider: ElementsProvider<S>) : Storage<S>(provider) {


}