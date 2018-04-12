package com.weisi.tool.wsnbox.processor.accessor

interface OnSensorHistoryDataAccessListener {
    fun onPhysicalSensorHistoryDataAccess(address: Int, timestamp: Long, batteryVoltage: Float)
    fun onLogicalSensorHistoryDataAccess(sensorId: Long, timestamp: Long, rawValue: Double)
}