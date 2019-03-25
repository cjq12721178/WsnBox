package com.weisi.tool.wsnbox.bean.configuration;

import android.content.Context;

public class SettingsImpl extends Settings {
    public SettingsImpl(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        mDefaultTcpEnable = true;
        mDefaultRemoteServerIp = "122.225.88.90";
        mDefaultRemoteServerPort = 5001;
        mDefaultTcpDataRequestCycle = 2000;

        mDefaultSensorDataGatherEnable = true;
        mDefaultSensorDataGatherCycle = 60;

        mDefaultDataWarnEnable = true;
        mDefaultDataWarnNotifyEnable = true;
        mDefaultDataWarnNotifySoundEnable = true;
        mDefaultDataWarnNotifyVibrateEnable = true;
        mDefaultDataWarnToastEnable = true;
    }
}
