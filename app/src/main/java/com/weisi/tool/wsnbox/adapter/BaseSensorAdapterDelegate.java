package com.weisi.tool.wsnbox.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.cjq.lib.weisi.sensor.Measurement;
import com.cjq.lib.weisi.sensor.Sensor;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by CJQ on 2017/9/22.
 */

public abstract class BaseSensorAdapterDelegate implements AdapterDelegate<Sensor> {

    private static boolean showSensorNameOrAddress = true;
    private static boolean showMeasurementNameOrType = true;
    private static boolean realTime = true;
    private static final Date TIMESTAMP_SETTER = new Date();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final int UPDATE_TYPE_VALUE_CHANGED = 1;
    public static final int UPDATE_TYPE_SENSOR_LABEL_CHANGED = 2;
    public static final int UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED = 3;

    public static void setShowSensorNameOrAddress(boolean showName) {
        showSensorNameOrAddress = showName;
    }

    public static void setShowMeasurementNameOrType(boolean showName) {
        showMeasurementNameOrType = showName;
    }

    public static void setRealTime(boolean isRealTime) {
        realTime = isRealTime;
    }

    protected void setSensorNameAddressText(TextView tvSensorNameAddress, Sensor sensor) {
        tvSensorNameAddress.setText(showSensorNameOrAddress
                ? sensor.getGeneralName()
                : sensor.getFormatAddress());
    }

    protected void setTimestampText(TextView tvTimestamp, Sensor sensor) {
        TIMESTAMP_SETTER.setTime(getValue(sensor).getTimestamp());
        tvTimestamp.setText(DATE_FORMAT.format(TIMESTAMP_SETTER));
    }

    private Sensor.Value getValue(Sensor sensor) {
        return realTime
                ? sensor.getRealTimeValue()
                : sensor.getEarliestHistoryValue();
    }

    protected void setMeasurementText(TextView tvMeasurementNameType, TextView tvMeasurementValue, Measurement measurement) {
        setMeasurementNameTypeText(tvMeasurementNameType, measurement);
        setMeasurementValueText(tvMeasurementValue, measurement);
    }

    protected void setMeasurementNameTypeText(TextView tvMeasurementNameType, Measurement measurement) {
        tvMeasurementNameType.setText(showMeasurementNameOrType
                ? measurement.getGeneralName()
                : String.format("%02X", measurement.getDataType().getValue()));
    }

    protected void setMeasurementValueText(TextView tvMeasurementValue, Measurement measurement) {
        Measurement.Value value = getValue(measurement);
        tvMeasurementValue.setText(value != null
                ? measurement.getDataType().getDecoratedValueWithUnit(value)
                : null);
    }

    private Measurement.Value getValue(Measurement measurement) {
        return realTime
                ? measurement.getRealTimeValue()
                : measurement.getEarliestHistoryValue();
    }
}
