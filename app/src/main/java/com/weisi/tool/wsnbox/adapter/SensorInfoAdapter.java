package com.weisi.tool.wsnbox.adapter;

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

import com.cjq.lib.weisi.sensor.Measurement;
import com.cjq.lib.weisi.sensor.Sensor;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.weisi.tool.wsnbox.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by CJQ on 2017/11/6.
 */

public class SensorInfoAdapter extends RecyclerViewBaseAdapter<Sensor.Value> {

    public static final int MAX_DISPLAY_COUNT = 3;
    public static final int HAS_NO_EXCESS_DISPLAY_ITEM = 0;
    public static final int ONLY_HAS_RIGHT_DISPLAY_ITEM = 1;
    public static final int HAS_BOTH_DISPLAY_ITEM = 2;
    public static final int ONLY_HAS_LEFT_DISPLAY_ITEM = 3;
    private final Drawable mSensorValueBackground;
    private final Date mTimeSetter = new Date();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");
    private final Sensor mSensor;
    //这个属性的作用主要是将mSensor.getMeasurementCollections().size() + 1值固定，
    //以防unknown sensor在动态监听时发生变化出错，
    //所以SensorInformationFragment的onCreateView方法中须尽早生成SensorInfoAdapter类，
    //并在之后所有需用到mSensor.getMeasurementCollections().size() + 1的地方用
    //getDisplayCount方法代替
    private final int mDisplayCount;
    private final boolean mIsRealTime;
    private int mDisplayStartIndex;
    private OnDisplayStateChangeListener mOnDisplayStateChangeListener;

    public SensorInfoAdapter(Context context,
                             Sensor sensor,
                             boolean isRealTime,
                             int displayStartIndex) {
        mSensorValueBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.bg_li_sensor_data));
        mSensor = sensor;
        mIsRealTime = isRealTime;
        mDisplayCount = mSensor.getMeasurementCollections().size() + 1;
        mDisplayStartIndex = displayStartIndex;
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
    public Sensor.Value getItemByPosition(int position) {
        return mIsRealTime
                ? mSensor.getDynamicValue(position)
                : mSensor.getHistoryValue(position);
    }

    @Override
    public int getItemCount() {
        return mIsRealTime
                ? mSensor.getDynamicValueSize()
                : mSensor.getHistoryValueSize();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.li_sensor_info,
                        parent,
                        false),
                getActualDisplayCount());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Sensor.Value value, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        mTimeSetter.setTime(value.getTimeStamp());
        holder.mTvTimestamp.setText(mDateFormat.format(mTimeSetter));
        setDisplayItems(value, position, holder);
    }

    private void setDisplayItems(Sensor.Value value, int position, ViewHolder holder) {
        holder.itemView.setBackground(position % 2 == 1
                ? mSensorValueBackground
                : null);
        long timestamp = value.getTimeStamp();
        List<Measurement> measurements = mSensor.getMeasurementCollections();
        for (int i = 0,
             valueContentSize = holder.mTvValueContents.length,
             measurementSize = mDisplayCount - 1;
             i < valueContentSize;
             ++i) {
            if (mDisplayStartIndex + i < measurementSize) {
                holder.mTvValueContents[i].setText(getCorrespondDecoratedValueWithUnit(
                        measurements.get(mDisplayStartIndex + i),
                        timestamp,
                        position));
            } else {
                holder.mTvValueContents[i].setText(String.format("%.2fV", value.getBatteryVoltage()));
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Sensor.Value value, int position, List payloads) {
        Object payload = payloads.get(0);
        if (payload instanceof Boolean) {
            if ((Boolean) payload) {
                setDisplayItems(value, position, (ViewHolder) holder);
            } else {
                onBindViewHolder(holder, value, position);
            }
        }
    }

    private String getCorrespondDecoratedValueWithUnit(Measurement measurement, long timestamp, int position) {
        Measurement.Value value = measurement.findHistoryValue(position, timestamp);
        return value != null ? measurement.getDataType().getDecoratedValueWithUnit(value)
                : "";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvTimestamp;
        private final TextView[] mTvValueContents;

        public ViewHolder(View itemView, int valueContentSize) {
            super(itemView);
            mTvTimestamp = (TextView) itemView.findViewById(R.id.tv_timestamp);
            mTvValueContents = new TextView[valueContentSize];
            mTvValueContents[0] = (TextView) itemView.findViewById(R.id.tv_measurement1);
            mTvValueContents[1] = (TextView) itemView.findViewById(R.id.tv_measurement2);
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
