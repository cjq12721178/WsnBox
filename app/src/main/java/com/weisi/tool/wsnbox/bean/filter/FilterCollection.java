package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.sensor.Filter;
import com.cjq.lib.weisi.sensor.Sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/9/14.
 */

public class FilterCollection implements Filter {

    private List<Filter> mFilters = new ArrayList<>();

    public FilterCollection add(Filter filter) {
        if (filter != null) {
            mFilters.add(filter);
        }
        return this;
    }

    public void clear() {
        mFilters.clear();
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
