package com.weisi.tool.wsnbox.application;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.bean.configuration.Settings;
import com.weisi.tool.wsnbox.handler.CrashHandler;
import com.weisi.tool.wsnbox.util.FlavorClassBuilder;
import com.weisi.tool.wsnbox.version.VersionChecker;

import java.io.File;

/**
 * Created by CJQ on 2017/12/5.
 */

public class BaseApplication extends MultiDexApplication {

    //private UserInfo mUserInfo;
    private Settings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        //mUserInfo = new UserInfo(this);
        //Log.d(Tag.LOG_TAG_D_TEST, "application onCreate");
        ExceptionLog.initialize(this, getSDCardDirectoryName());
        ClosableLog.setEnablePrint(true);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        if (VersionChecker.amend(getApplicationContext())) {
            mSettings = FlavorClassBuilder.buildImplementation(Settings.class, Context.class, getApplicationContext());
            //mSettings = new SettingsImpl(getApplicationContext());
            mSettings.notificationParameterCorrect();
//            SettingsImporter importer = new SettingsImporter();
//            if (importer.leadIn(this)) {
//                mSettings = importer.getSettings();
//            }
        }
    }

    public Settings getSettings() {
        return mSettings;
    }

    public boolean isConfigurationPrepared() {
        return mSettings != null;
    }

    public String getSDCardDirectoryName() {
        return getApplicationContext().getString(R.string.sdcard_directory);
    }

    public String getSDCardDirectoryAbsolutePath() {
        return Environment.getExternalStorageDirectory()
                + File.separator
                + getSDCardDirectoryName();
    }

    public String getConfigurationDirectoryName() {
        return getApplicationContext().getString(R.string.configuration_directory);
    }

    public String getConfigurationDirectoryAbsolutePath() {
        return getSDCardDirectoryAbsolutePath()
                + File.separator
                + getConfigurationDirectoryName();
    }

    public String getConfigurationFileAbsolutePath(String fileName) {
        return getConfigurationDirectoryAbsolutePath()
                + File.separator
                + fileName
                + ".xml";
    }
}
