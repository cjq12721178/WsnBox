package com.weisi.tool.wsnbox.bean.filter;

import com.cjq.lib.weisi.sensor.Filter;
import com.cjq.lib.weisi.sensor.Sensor;

/**
 * Created by CJQ on 2017/9/14.
 */
public class SearchFilter implements Filter {

    private String[] mSearchContents;

    public void setSearchContents(String[] searchContents) {
        mSearchContents = searchContents;
    }

    public String[] getSearchContents() {
        return mSearchContents;
    }

    @Override
    public boolean isMatch(Sensor sensor) {
        return false;
    }
}
