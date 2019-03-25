package com.weisi.tool.wsnbox.bean.decorator

/**
 * Created by CJQ on 2018/2/13.
 */
class CommonSensorInfoDecorator(customName: String) : CommonBaseDecorator(customName) {

    override fun decorateValue(rawValue: Double, para: Int): String {
        return ""
    }
}