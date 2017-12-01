package com.weisi.tool.wsnbox.bean.sorter;

import com.cjq.lib.weisi.sensor.Sensor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public abstract class SensorSorter implements Comparator<Sensor> {

    public int add(List<Sensor> sensors, Sensor sensor) {
        int position = Collections.binarySearch(sensors, sensor, this);
        if (position < 0) {
            position = -position - 1;
            sensors.add(position, sensor);
            return position;
        } else {
            return -1;
        }
    }

    public void sort(List<Sensor> sensors) {
        Collections.sort(sensors, this);
    }

    public int find(List<Sensor> sensors, Sensor sensor) {
        return Collections.binarySearch(sensors, sensor, this);
    }
}
