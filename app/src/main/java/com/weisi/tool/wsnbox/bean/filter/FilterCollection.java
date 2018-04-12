package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.iot.Sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public class FilterCollection<S extends Sensor> implements Sensor.Filter<S> {

    private List<Sensor.Filter<S>> mFilters = new ArrayList<>();

    public FilterCollection add(Sensor.Filter<S> filter) {
        if (filter != null) {
            mFilters.add(filter);
        }
        return this;
    }

    public void clear() {
        mFilters.clear();
    }

    public void remove(Sensor.Filter<S> filter) {
        mFilters.remove(filter);
    }

    public int size() {
        return mFilters.size();
    }

    @Override
    public boolean isMatch(S sensor) {
        for (int i = 0, size = mFilters.size();i < size;++i) {
            if (!mFilters.get(i).isMatch(sensor)) {
                return false;
            }
        }
        return true;
    }
}
