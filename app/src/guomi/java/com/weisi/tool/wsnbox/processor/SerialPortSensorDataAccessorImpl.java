package com.weisi.tool.wsnbox.processor;

import android.content.Context;

import com.cjq.tool.qbox.util.ExceptionLog;

import java.io.IOException;

/**
 * Created by CJQ on 2017/12/27.
 */

public class SerialPortSensorDataAccessorImpl extends SerialPortSensorDataAccessor {

    @Override
    protected boolean onPreLaunchCommunicator(Context context) {
        return powerOnSerialPort();
    }

    @Override
    protected void onPostShutdownCommunicator() {
        powerOffSerialPort();
    }

    private static boolean powerOnSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 1 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_gpio_112"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
            return true;
        } catch (IOException e) {
            ExceptionLog.record(e);
        }
        return false;
    }

    private static void powerOffSerialPort() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_dc_in_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_vbat_out_en"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_a"});
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo 0 > /sys/devices/soc.0/xt_dev.68/xt_uart_b"});
        } catch (IOException e) {
            ExceptionLog.record(e);
        }
    }
}
