package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.Value


/**
 * Created by CJQ on 2018/2/13.
 */
abstract class CommonBaseDecorator<V : Value>(var customName: String)
    : Decorator<V> {

    override fun decorateName(p0: String?) = customName
}