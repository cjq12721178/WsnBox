package com.weisi.tool.wsnbox.bean.information;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by CJQ on 2017/12/12.
 */

public class UserInfo {

    private static final String PREFERENCE_FILE_NAME = "user_info";

    private final Context mContext;

    public UserInfo(Context context) {
        mContext = context;
    }

    //注意，该函数只有第一次调用时返回true
    public boolean isFirstRun() {
        boolean result = getSharedPreferences().getBoolean("first_run", true);
        if (result) {
            getSharedPreferences().edit().putBoolean("first_run", false).commit();
        }
        return result;
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }
}
