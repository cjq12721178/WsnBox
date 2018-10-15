package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.Decorator
import com.cjq.lib.weisi.iot.container.Value


/**
 * Created by CJQ on 2018/2/13.
 */
abstract class CommonBaseDecorator<V : Value>(private var customName: String)
    : Decorator<V> {

    override fun decorateName(name: String?) = customName
}