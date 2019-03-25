package com.weisi.tool.wsnbox.bean.decorator

import com.cjq.lib.weisi.iot.Decorator


/**
 * Created by CJQ on 2018/2/13.
 */
abstract class CommonBaseDecorator(val customName: String)
    : Decorator {

    override fun decorateName(name: String?) = customName
}