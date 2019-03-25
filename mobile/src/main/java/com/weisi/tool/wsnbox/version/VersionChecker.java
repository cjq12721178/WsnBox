package com.weisi.tool.wsnbox.version;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Created by CJQ on 2018/1/4.
 * 产生于V14(含)之后
 */

public class VersionChecker {

    //未知版本号
    private static final int VU = -1;
    //至此之后开始使用settings.xml记录设置信息，并在user_info.xml中记录是否首次运行APP
    private static final int V6 = 6;
    //至此之后将settings.xml中的所有信息移至默认设置文件中存储；
    //删除user_info.xml中是否首次运行APP的记录；
    //在默认设置文件中新增上一个运行版本号
    private static final int V14 = 14;

    private VersionChecker() {
    }

    public static boolean amend(Context context) {
        final String PREVIOUS_VERSION_CODE_KEY = "prev_version_code";
        int currentVersionCode = 0;
        try {
            currentVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        SharedPreferences userInfo = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        int previousVersionCode = userInfo.getInt(PREVIOUS_VERSION_CODE_KEY, VU);
        if (previousVersionCode == VU) {
            //检查版本号是否大于等于V6，小于V14
            if (userInfo.getBoolean("first_run", false)) {
                userInfo.edit().clear().commit();
                SharedPreferences oldSettings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor newSettingsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                newSettingsEditor.clear();
                for (Map.Entry<String, ?> entry
                        : oldSettings.getAll().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        newSettingsEditor.putString(key, (String) value);
                    } else if (value instanceof Boolean) {
                        newSettingsEditor.putBoolean(key, (Boolean) value);
                    } else if (value instanceof Long) {
                        newSettingsEditor.putLong(key, (Long) value);
                    } else if (value instanceof Integer) {
                        newSettingsEditor.putInt(key, (Integer) value);
                    } else if (value instanceof Float) {
                        newSettingsEditor.putFloat(key, (Float) value);
                    } else if (value instanceof Set) {
                        newSettingsEditor.putStringSet(key, (Set<String>) value);
                    }
                }
                newSettingsEditor.commit();
            }
            //删除旧的设置文件
            File oldSettingsFile = new File("/data/data/" + context.getPackageName() + "/shared_prefs","settings.xml");
            if (oldSettingsFile.exists()) {
                oldSettingsFile.delete();
            }
        }
        //更改版本号
        if (currentVersionCode != previousVersionCode) {
            userInfo.edit().putInt(PREVIOUS_VERSION_CODE_KEY, currentVersionCode).commit();
        }
        return true;
    }
}
