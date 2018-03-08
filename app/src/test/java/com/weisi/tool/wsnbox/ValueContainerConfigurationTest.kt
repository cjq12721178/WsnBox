package com.weisi.tool.wsnbox

import com.weisi.tool.wsnbox.bean.decorator.CommonSensorDecorator
import org.junit.Test

/**
 * Created by CJQ on 2018/2/13.
 */
class ValueContainerConfigurationTest {

    @Test
    fun testDecorator() {
        var sensorDecorator = CommonSensorDecorator("hehe")
        sensorDecorator.customName
    }
}