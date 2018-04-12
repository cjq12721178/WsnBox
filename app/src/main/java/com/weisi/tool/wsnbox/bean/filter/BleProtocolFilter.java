package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.iot.PhysicalSensor;

/**
 * Created by CJQ on 2017/9/14.
 */
public class BleProtocolFilter implements SensorProtocolFilter {

//    private boolean mIsBle;
//
//    public void setSensorSource(boolean isBle) {
//        mIsBle = isBle;
//    }
//
//    public boolean isSensorSourceBle() {
//        return mIsBle;
//    }

    @Override
    public boolean isMatch(PhysicalSensor sensor) {
        return sensor.getId().isBleProtocolFamily();
    }
}
