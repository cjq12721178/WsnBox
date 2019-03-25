package com.weisi.tool.wsnbox.processor.accessor;

/**
 * Created by CJQ on 2018/4/2.
 */
public interface OnSensorDynamicDataAccessListener {
    void onSensorDynamicDataAccess(int sensorAddress,
                                   byte dataTypeValue,
                                   int dataTypeIndex,
                                   long timestamp,
                                   float batteryVoltage,
                                   double rawValue);
}
