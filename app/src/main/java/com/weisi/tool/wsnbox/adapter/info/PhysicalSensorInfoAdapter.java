package com.weisi.tool.wsnbox.adapter.info;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.Measurement;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.lib.weisi.iot.Sensor;
import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.weisi.tool.wsnbox.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by CJQ on 2017/11/6.
 */

public class PhysicalSensorInfoAdapter extends SensorInfoAdapter<Sensor.Info.Value, PhysicalSensor, PhysicalSensorInfoAdapter.SensorInfo> {

    public static final int MAX_DISPLAY_COUNT = 3;
    public static final int HAS_NO_EXCESS_DISPLAY_ITEM = 0;
    public static final int ONLY_HAS_RIGHT_DISPLAY_ITEM = 1;
    public static final int HAS_BOTH_DISPLAY_ITEM = 2;
    public static final int ONLY_HAS_LEFT_DISPLAY_ITEM = 3;

    private final Drawable mSensorValueBackground;

    //这个属性的作用主要是将mSensor.getMeasurementCollections().size() + 1值固定，
    //以防unknown sensor在动态监听时发生变化出错，
    //所以SensorInformationFragment的onCreateView方法中须尽早生成SensorInfoAdapter类，
    //并在之后所有需用到mSensor.getMeasurementCollections().size() + 1的地方用
    //getDisplayCount方法代替
    private final int mDisplayCount;
    private int mDisplayStartIndex;
    private OnDisplayStateChangeListener mOnDisplayStateChangeListener;

    public PhysicalSensorInfoAdapter(Context context,
                                     PhysicalSensor sensor,
                                     boolean isRealTime,
                                     int displayStartIndex) {
        super(sensor, isRealTime);
        mSensorValueBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.bg_li_sensor_data));
        mDisplayCount = sensor.getDisplayMeasurementSize() + 1;
        mDisplayStartIndex = displayStartIndex;
    }

    @NotNull
    @Override
    protected SensorInfo onCreateSensorInfo(@NotNull PhysicalSensor sensor, boolean realTime) {
        return new SensorInfo(sensor, realTime);
    }

    public int getDisplayStartIndex() {
        return mDisplayStartIndex;
    }

    public void setOnDisplayStateChangeListener(OnDisplayStateChangeListener listener) {
        mOnDisplayStateChangeListener = listener;
    }

    public int getScheduledDisplayCount() {
        return mDisplayCount;
    }
    
    public int getActualDisplayCount() {
        return Math.min(mDisplayCount, MAX_DISPLAY_COUNT);
    }

    //leftOrRight：true为显示左边隐藏的数据项，false为显示右边隐藏的数据项
    public void showNextItem(boolean leftOrRight) {
        int oldDisplayState = getInfoDisplayState();
        if (leftOrRight) {
            if (oldDisplayState == ONLY_HAS_LEFT_DISPLAY_ITEM
                    || oldDisplayState == HAS_BOTH_DISPLAY_ITEM) {
                --mDisplayStartIndex;
                notifyDisplayStartIndexChanged();
            } else {
                return;
            }
        } else {
            if (oldDisplayState == ONLY_HAS_RIGHT_DISPLAY_ITEM
                    || oldDisplayState == HAS_BOTH_DISPLAY_ITEM) {
                ++mDisplayStartIndex;
                notifyDisplayStartIndexChanged();
            } else {
                return;
            }
        }
        notifyItemRangeChanged(0, getItemCount(), true);
        if (mOnDisplayStateChangeListener != null) {
            int newDisplayState = getInfoDisplayState();
            if (oldDisplayState != newDisplayState) {
                mOnDisplayStateChangeListener.onInfoOrientationChanged(newDisplayState);
            }
        }
    }

    private void notifyDisplayStartIndexChanged() {
        if (mOnDisplayStateChangeListener != null) {
            mOnDisplayStateChangeListener.onDisplayStartIndexChanged(mDisplayStartIndex);
        }
    }

    public int getInfoDisplayState() {
        if (mDisplayCount <= MAX_DISPLAY_COUNT) {
            return HAS_NO_EXCESS_DISPLAY_ITEM;
        }
        if (mDisplayStartIndex == 0) {
            return ONLY_HAS_RIGHT_DISPLAY_ITEM;
        }
        if (mDisplayStartIndex == mDisplayCount - MAX_DISPLAY_COUNT) {
            return ONLY_HAS_LEFT_DISPLAY_ITEM;
        }
        return HAS_BOTH_DISPLAY_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.li_physical_sensor_info,
                        parent,
                        false),
                getActualDisplayCount());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Sensor.Info.Value value, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.mTvTimestamp.setText(getTimeFormat(value.getTimestamp()));
        setDisplayItems(value, position, holder);
    }

    private void setDisplayItems(Sensor.Info.Value value, int position, ViewHolder holder) {
        holder.itemView.setBackground(position % 2 == 1
                ? mSensorValueBackground
                : null);
        long timestamp = value.getTimestamp();
        //LogicalSensor[] measurements = getSensorInfo().mLogicalSensors;
        PhysicalSensor sensor = getSensorInfo().getSensor();
        ValueContainer<DisplayMeasurement.Value>[] valueContainers = getSensorInfo().mMeasurementValueContainers;
        DisplayMeasurement.Value measurementValue;
        TextView tvValueContent;
        DisplayMeasurement<?> measurement;
        ValueContainer<DisplayMeasurement.Value> valueContainer;
        for (int i = 0,
             valueContentSize = holder.mTvValueContents.length,
             measurementSize = mDisplayCount - 1;
             i < valueContentSize;
             ++i) {
            tvValueContent = holder.mTvValueContents[i];
            if (mDisplayStartIndex + i < measurementSize) {
                measurement = sensor.getDisplayMeasurementByPosition(mDisplayStartIndex + i);
                valueContainer = valueContainers[mDisplayStartIndex + i];
                measurementValue = valueContainer.findValue(position, timestamp);
                if (measurementValue != null) {
                    tvValueContent.setText(measurement.formatValue(measurementValue));
                    if (getWarnProcessor() != null) {
                        getWarnProcessor().process(measurementValue, measurement.getConfiguration().getWarner(), tvValueContent);
                    }
                } else {
                    tvValueContent.setText(null);
                }
            } else {
                tvValueContent.setText(value.getFormattedBatteryVoltage());
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Sensor.Info.Value value, int position, List payloads) {
        Object payload = payloads.get(0);
        if (payload instanceof Boolean) {
            if ((Boolean) payload) {
                setDisplayItems(value, position, (ViewHolder) holder);
            } else {
                onBindViewHolder(holder, value, position);
            }
        } else if (payload instanceof Integer && ((int)payload) == getUPDATE_TYPE_BACKGROUND_COLOR()) {
            holder.itemView.setBackground(position % 2 == 1
                    ? mSensorValueBackground
                    : null);
        }
    }

    public static class SensorInfo extends SensorInfoAdapter.SensorInfo<Sensor.Info.Value, PhysicalSensor> {

        //public final LogicalSensor[] mLogicalSensors;
        public final ValueContainer<DisplayMeasurement.Value>[] mMeasurementValueContainers;

        public SensorInfo(PhysicalSensor physicalSensor, boolean isRealTime) {
            super(physicalSensor, isRealTime);
            int size = physicalSensor.getDisplayMeasurementSize();
            mMeasurementValueContainers = new ValueContainer[size];
            if (isRealTime) {
                for (int i = 0;i < size;++i) {
                    mMeasurementValueContainers[i] = physicalSensor.getDisplayMeasurementByPosition(i).getDynamicValueContainer();
                }
            } else {
                for (int i = 0;i < size;++i) {
                    mMeasurementValueContainers[i] = physicalSensor.getDisplayMeasurementByPosition(i).getHistoryValueContainer();
                }
            }
        }

        public void setValueContainers(long startTime, long endTime) {
            super.setValueContainers(startTime, endTime);
            PhysicalSensor sensor = getSensor();
            for (int i = 0, size = sensor.getDisplayMeasurementSize();
                 i < size;++i) {
                mMeasurementValueContainers[i] = sensor
                        .getDisplayMeasurementByPosition(i)
                        .getHistoryValueContainer()
                        .applyForSubValueContainer(startTime, endTime);
            }
        }

        public void detachSubValueContainer() {
            super.detachSubValueContainer();
            PhysicalSensor sensor = getSensor();
            for (int i = 0, size = sensor.getDisplayMeasurementSize();
                 i < size;++i) {
                sensor.getDisplayMeasurementByPosition(i)
                        .getHistoryValueContainer()
                        .detachSubValueContainer(mMeasurementValueContainers[i]);
            }
        }

        @NotNull
        @Override
        public Measurement<Sensor.Info.Value, ?> getMainMeasurement() {
            return getSensor().getInfo();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvTimestamp;
        private final TextView[] mTvValueContents;

        public ViewHolder(View itemView, int valueContentSize) {
            super(itemView);
            mTvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            mTvValueContents = new TextView[valueContentSize];
            mTvValueContents[0] = itemView.findViewById(R.id.tv_measurement1);
            mTvValueContents[1] = itemView.findViewById(R.id.tv_measurement2);
            if (valueContentSize == MAX_DISPLAY_COUNT) {
                ViewStub vsMeasurement = (ViewStub) itemView.findViewById(R.id.vs_measurement);
                mTvValueContents[2] = (TextView) vsMeasurement.inflate();
            }
        }
    }

    public interface OnDisplayStateChangeListener {
        void onInfoOrientationChanged(int newDisplayState);
        void onDisplayStartIndexChanged(int newDisplayStartIndex);
    }
}
