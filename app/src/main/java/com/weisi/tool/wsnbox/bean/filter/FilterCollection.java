package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.node.Sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public class FilterCollection implements Sensor.Filter {

    private List<Sensor.Filter> mFilters = new ArrayList<>();

    public FilterCollection add(Sensor.Filter filter) {
        if (filter != null) {
            mFilters.add(filter);
        }
        return this;
    }

    public void clear() {
        mFilters.clear();
    }

    public void remove(Sensor.Filter filter) {
        mFilters.remove(filter);
    }

    public int size() {
        return mFilters.size();
    }

    @Override
    public boolean isMatch(Sensor sensor) {
        for (int i = 0, size = mFilters.size();i < size;++i) {
            if (!mFilters.get(i).isMatch(sensor)) {
                return false;
            }
        }
        return true;
    }
}
