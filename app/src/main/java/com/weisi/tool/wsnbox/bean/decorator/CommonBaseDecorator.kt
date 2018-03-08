package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.node.ValueContainer

/**
 * Created by CJQ on 2018/2/13.
 */
abstract class CommonBaseDecorator<V : ValueContainer.Value>(var customName: String)
    : ValueContainer.Decorator<V> {

    override fun decorateName(p0: String?) = customName
}