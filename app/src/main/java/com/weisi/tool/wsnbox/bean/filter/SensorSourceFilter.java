package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.sensor.ConfigurationManager;
import com.cjq.lib.weisi.sensor.Filter;
import com.cjq.lib.weisi.sensor.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */
public class SensorSourceFilter implements Filter {

    private boolean mIsBle;

    public void setSensorSource(boolean isBle) {
        mIsBle = isBle;
    }

    public boolean isSensorSourceBle() {
        return mIsBle;
    }

    @Override
    public boolean isMatch(Sensor sensor) {
        return sensor.isBleProtocolFamily();
    }
}
