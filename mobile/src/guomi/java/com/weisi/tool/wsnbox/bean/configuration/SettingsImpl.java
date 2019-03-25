package com.weisi.tool.wsnbox.bean.configuration;

import android.content.Context;

public class SettingsImpl extends Settings {
    public SettingsImpl(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        mDefaultSerialPortEnable = true;
        mDefaultSerialPortName = "/dev/ttyHSL1";
        mDefaultSerialPortBaudRate = 115200;
        mDefaultSerialPortDataRequestCycle = 2000;

        mDefaultSensorDataGatherEnable = true;
        mDefaultSensorDataGatherCycle = 60;

        mDefaultDataWarnEnable = true;
        mDefaultDataWarnNotifyEnable = true;
        mDefaultDataWarnNotifySoundEnable = true;
        mDefaultDataWarnNotifyVibrateEnable = true;
        mDefaultDataWarnToastEnable = true;
    }
}
