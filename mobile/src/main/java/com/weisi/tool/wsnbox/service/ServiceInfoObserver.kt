package com.weisi.tool.wsnbox.service

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor

interface ServiceInfoObserver {
    fun onSensorConfigurationChanged()
    fun onValueTestResult(info: Sensor.Info, measurement: PracticalMeasurement, value: DisplayMeasurement.Value, warnResult: Int): Boolean
}