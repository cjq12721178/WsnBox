package com.weisi.tool.wsnbox.application;

import android.app.Application;

import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.bean.configuration.SettingsImporter;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.handler.CrashHandler;
import com.weisi.tool.wsnbox.version.VersionChecker;

/**
 * Created by CJQ on 2017/12/5.
 */

public class BaseApplication extends Application {

    //private UserInfo mUserInfo;
    private Settings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        //mUserInfo = new UserInfo(this);
        ExceptionLog.initialize(this, "WsnBox");
        ClosableLog.setEnablePrint(true);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        if (VersionChecker.amend(getApplicationContext())) {
            SettingsImporter importer = new SettingsImporter();
            if (importer.leadIn(this)) {
                mSettings = importer.getSettings();
            }
        }
    }

    public Settings getSettings() {
        return mSettings;
    }

    public boolean isConfigurationPrepared() {
        return mSettings != null;
    }
}
