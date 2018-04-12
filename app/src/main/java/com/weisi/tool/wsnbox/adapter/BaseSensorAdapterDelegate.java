package com.weisi.tool.wsnbox.adapter;

import android.view.View;
import android.widget.TextView;

import com.cjq.lib.weisi.iot.LogicalSensor;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by CJQ on 2017/9/22.
 */

public abstract class BaseSensorAdapterDelegate implements AdapterDelegate<PhysicalSensor> {

    public static final int UPDATE_TYPE_VALUE_CHANGED = 1;
    public static final int UPDATE_TYPE_SENSOR_LABEL_CHANGED = 2;
    public static final int UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED = 3;

    private static final Date TIMESTAMP_SETTER = new Date();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static boolean showSensorNameOrAddress = true;
    private static boolean showMeasurementNameOrType = true;
    private static boolean realTime = true;
    private static CommonWarnProcessor<View> warnProcessor;

    public static void setShowSensorNameOrAddress(boolean showName) {
        showSensorNameOrAddress = showName;
    }

    public static void setShowMeasurementNameOrType(boolean showName) {
        showMeasurementNameOrType = showName;
    }

    public static void setRealTime(boolean isRealTime) {
        realTime = isRealTime;
    }

    public static void setWarnProcessor(CommonWarnProcessor commonWarnProcessor) {
        warnProcessor = commonWarnProcessor;
    }

    protected void setSensorNameAddressText(TextView tvSensorNameAddress, PhysicalSensor sensor) {
        tvSensorNameAddress.setText(showSensorNameOrAddress
                ? sensor.getName()
                : sensor.getFormatAddress());
    }

    protected void setTimestampText(TextView tvTimestamp, PhysicalSensor sensor) {
        TIMESTAMP_SETTER.setTime(getValue(sensor).getTimestamp());
        tvTimestamp.setText(DATE_FORMAT.format(TIMESTAMP_SETTER));
    }

    private PhysicalSensor.Value getValue(PhysicalSensor sensor) {
        return realTime
                ? sensor.getRealTimeValue()
                : sensor.getHistoryValueContainer().getEarliestValue();
    }

    protected void setMeasurementText(TextView tvMeasurementNameType, TextView tvMeasurementValue, LogicalSensor measurement) {
        setMeasurementNameTypeText(tvMeasurementNameType, measurement);
        setMeasurementValueText(tvMeasurementValue, measurement);
    }

    protected void setMeasurementNameTypeText(TextView tvMeasurementNameType, LogicalSensor measurement) {
        tvMeasurementNameType.setText(showMeasurementNameOrType
                ? measurement.getName()
                : measurement.getDataType().getFormattedValue());
    }

    protected void setMeasurementValueText(TextView tvMeasurementValue, LogicalSensor measurement) {
        LogicalSensor.Value value = getValue(measurement);
        if (value != null) {
            tvMeasurementValue.setText(measurement.formatValueWithUnit(value));
            if (warnProcessor != null) {
                warnProcessor.process(value, measurement.getConfiguration().getWarner(), tvMeasurementValue);
            }
        } else {
            tvMeasurementValue.setText(null);
        }
    }

    private LogicalSensor.Value getValue(LogicalSensor measurement) {
        return realTime
                ? measurement.getRealTimeValue()
                : measurement.getHistoryValueContainer().getEarliestValue();
    }
}
