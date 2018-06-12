package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */
public class BleProtocolFilter<S extends Sensor> implements Filter<S> {

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
    public boolean match(S sensor) {
        return sensor.getId().isBleProtocolFamily();
    }
}
