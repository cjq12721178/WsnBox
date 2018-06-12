package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.data.Sorter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2018/4/17.
 */

public class SensorNameSorter<S extends Sensor> extends Sorter<S> {

    @Override
    public int compare(S s1, S s2) {
        return s1.getName().compareTo(s2.getName());
    }
}
