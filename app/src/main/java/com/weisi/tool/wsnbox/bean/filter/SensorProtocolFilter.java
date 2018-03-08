package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.node.Sensor;

/**
 * Created by CJQ on 2018/2/1.
 */

public interface SensorProtocolFilter extends Sensor.Filter {
    int ALL_PROTOCOL = 0;
    int BLE_PROTOCOL = 1;
    int ESB_PROTOCOL = 2;
}
