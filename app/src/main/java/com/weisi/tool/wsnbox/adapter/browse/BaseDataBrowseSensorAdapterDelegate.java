package com.weisi.tool.wsnbox.adapter.browse;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.Measurement;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.lib.weisi.iot.Sensor;
import com.cjq.lib.weisi.iot.container.Value;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;
import com.weisi.tool.wsnbox.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * Created by CJQ on 2017/9/22.
 */

public abstract class BaseDataBrowseSensorAdapterDelegate implements AdapterDelegate<PhysicalSensor> {

    public static final int UPDATE_TYPE_VALUE_CHANGED = 1;
    public static final int UPDATE_TYPE_SENSOR_LABEL_CHANGED = 2;
    public static final int UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED = 3;

    private static final Date TIMESTAMP_SETTER = new Date();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static boolean showSensorNameOrAddress = true;
    private static boolean showMeasurementNameOrType = true;

    private static @ColorInt int warnerHighLimitColor;
    private static @ColorInt int warnerLowLimitColor;
    private static @ColorInt int warnerNormalColor;
    private static @ColorInt int realTimeDataColor;
    private static @ColorInt int historyDataColor;
    //private static boolean realTime = true;
    //private static CommonWarnProcessor<View> warnProcessor;

    public static void init(@NonNull Context context) {
        warnerHighLimitColor = ContextCompat.getColor(context, R.color.warner_high_limit);
        warnerLowLimitColor = ContextCompat.getColor(context, R.color.warner_low_limit);
        warnerNormalColor = ContextCompat.getColor(context, android.R.color.transparent);
        realTimeDataColor = ContextCompat.getColor(context, R.color.bg_real_time_sensor_data);
        historyDataColor = ContextCompat.getColor(context, R.color.bg_history_sensor_data);
    }

    public static void setShowSensorNameOrAddress(boolean showName) {
        showSensorNameOrAddress = showName;
    }

    public static void setShowMeasurementNameOrType(boolean showName) {
        showMeasurementNameOrType = showName;
    }

//    public static void setRealTime(boolean isRealTime) {
//        realTime = isRealTime;
//    }

//    public static void setWarnProcessor(CommonWarnProcessor commonWarnProcessor) {
//        warnProcessor = commonWarnProcessor;
//    }

    static void setSensorNameAddressText(@NonNull TextView tvSensorNameAddress, @NonNull Sensor sensor) {
        tvSensorNameAddress.setText(showSensorNameOrAddress
                ? sensor.getMainMeasurement().getName()
                : sensor.getMainMeasurement().getId().getFormatAddress());
    }

    void setTimestampText(@NonNull TextView tvTimestamp, @NonNull PhysicalSensor sensor) {
        setTimestampText(tvTimestamp, getValue(sensor.getInfo()));
    }

    static void setTimestampText(@NonNull TextView tvTimestamp, Value value) {
        if (value == null) {
            tvTimestamp.setText(null);
        } else {
            TIMESTAMP_SETTER.setTime(value.getTimestamp());
            tvTimestamp.setText(DATE_FORMAT.format(TIMESTAMP_SETTER));
        }
    }

    static void setMeasurementTimestampAndValueText(@NonNull TextView tvTimestamp, @NonNull TextView tvMeasurementValue, @NonNull DisplayMeasurement<?> measurement) {
        DisplayMeasurement.Value value = getValue(measurement);
        setTimestampText(tvTimestamp, value);
        setMeasurementValueText(tvMeasurementValue, measurement, value);
    }

    void setMeasurementText(@NonNull TextView tvMeasurementNameType, @NonNull TextView tvMeasurementValue, @NonNull DisplayMeasurement measurement) {
        setMeasurementNameTypeText(tvMeasurementNameType, measurement);
        setMeasurementValueText(tvMeasurementValue, measurement);
    }

    void setMeasurementNameTypeText(@NonNull TextView tvMeasurementNameType, @NonNull DisplayMeasurement measurement) {
        tvMeasurementNameType.setText(showMeasurementNameOrType
                ? measurement.getName()
                : measurement.getId().getFormattedDataTypeValue());
    }

    static void setMeasurementValueText(@NonNull TextView tvMeasurementValue, @NonNull DisplayMeasurement<?> measurement) {
        setMeasurementValueText(tvMeasurementValue, measurement, getValue(measurement));
    }

    static void setMeasurementValueText(@NonNull TextView tvMeasurementValue, @NonNull DisplayMeasurement<?> measurement, DisplayMeasurement.Value value) {
        if (value != null) {
            tvMeasurementValue.setText(measurement.formatValue(value));
            int warnResult = measurement.testValue(value);
            if (warnResult == DisplayMeasurement.SingleRangeWarner.RESULT_ABOVE_HIGH_LIMIT) {
                tvMeasurementValue.setBackgroundColor(warnerHighLimitColor);
            } else if (warnResult == DisplayMeasurement.SingleRangeWarner.RESULT_BELOW_LOW_LIMIT) {
                tvMeasurementValue.setBackgroundColor(warnerLowLimitColor);
            } else {
                tvMeasurementValue.setBackgroundColor(warnerNormalColor);
            }
//            if (warnProcessor != null) {
//                warnProcessor.process(value, measurement.getConfiguration().getWarner(), tvMeasurementValue);
//            }
        } else {
            tvMeasurementValue.setText(null);
            tvMeasurementValue.setBackgroundColor(warnerNormalColor);
        }
    }

    static void setItemBackground(@NonNull View view, @NonNull Sensor sensor) {
        setItemBackground(view, sensor.getMainMeasurement());
//        view.setBackgroundColor(sensor.getMainMeasurement().hasRealTimeValue()
//                ? realTimeDataColor
//                : historyDataColor);
    }

    static void setItemBackground(@NonNull View view, @NonNull Measurement measurement) {
        view.setBackgroundColor(measurement.hasRealTimeValue()
                ? realTimeDataColor
                : historyDataColor);
    }

//    private Sensor.Info.Value getValue(Sensor.Info info) {
//        Sensor.Info.Value result = info.getRealTimeValue();
//        return result != null
//                ? result
//                : info.getHistoryValueContainer().getEarliestValue();
//    }
//
//    private DisplayMeasurement.Value getValue(DisplayMeasurement<?> measurement) {
//        DisplayMeasurement.Value result = measurement.getRealTimeValue();
//        return result != null
//                ? result
//                : measurement.getHistoryValueContainer().getEarliestValue();
//    }

    static <V extends Value> V getValue(@NonNull Measurement<V, ?> measurement) {
        V result = measurement.getRealTimeValue();
        return result != null
                ? result
                : measurement.getHistoryValueContainer().getEarliestValue();
    }
}
