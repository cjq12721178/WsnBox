package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.iot.Sensor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public abstract class SensorSorter<S extends Sensor> implements Comparator<S> {

    public int add(List<S> sensors, S sensor) {
        int position = Collections.binarySearch(sensors, sensor, this);
        if (position < 0) {
            position = -position - 1;
            sensors.add(position, sensor);
            return position;
        } else {
            return -1;
        }
    }

    public void sort(List<S> sensors) {
        Collections.sort(sensors, this);
    }

    public int find(List<S> sensors, S sensor) {
        return Collections.binarySearch(sensors, sensor, this);
    }
}
