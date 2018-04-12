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

import com.cjq.lib.weisi.iot.LogicalSensor;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.lib.weisi.iot.SubValueContainer;
import com.cjq.lib.weisi.iot.ValueContainer;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.cjq.lib.weisi.iot.ValueContainer.LOOP_VALUE_ADDED;
import static com.cjq.lib.weisi.iot.ValueContainer.NEW_VALUE_ADDED;
import static com.cjq.lib.weisi.iot.ValueContainer.VALUE_UPDATED;

/**
 * Created by CJQ on 2017/11/6.
 */

public class SensorInfoAdapter extends RecyclerViewBaseAdapter<PhysicalSensor.Value> {

    public static final int MAX_DISPLAY_COUNT = 3;
    public static final int HAS_NO_EXCESS_DISPLAY_ITEM = 0;
    public static final int ONLY_HAS_RIGHT_DISPLAY_ITEM = 1;
    public static final int HAS_BOTH_DISPLAY_ITEM = 2;
    public static final int ONLY_HAS_LEFT_DISPLAY_ITEM = 3;

    private static CommonWarnProcessor<View> mWarnProcessor;

    private final Drawable mSensorValueBackground;
    private final Date mTimeSetter = new Date();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");
    private final SensorInfo mSensorInfo;
    //private final PhysicalSensor mSensor;
    //private ValueContainer<PhysicalSensor.Value> mSensorValueContainer;
    //private final ValueContainer<LogicalSensor.Value>[] mMeasurementValueContainers;
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
                             PhysicalSensor sensor,
                             boolean isRealTime,
                             int displayStartIndex) {
        mSensorValueBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.bg_li_sensor_data));
        //mSensor = sensor;
        mIsRealTime = isRealTime;
        mDisplayCount = sensor.getMeasurementCollections().size() + 1;
        mDisplayStartIndex = displayStartIndex;
        mSensorInfo = new SensorInfo(sensor, isRealTime);
//        if (isRealTime) {
//            mSensorValueContainer = mSensor.getDynamicValueContainer();
//        } else {
//            mSensorValueContainer = mSensor.getHistoryValueContainer();
//        }
//        mMeasurementValueContainers = new ValueContainer[mSensor.getMeasurementCollections().size()];
    }

    public long getIntraday() {
        if (!(mSensorInfo.mPhysicalSensorValueContainer instanceof SubValueContainer)) {
            return 0;
        }
        return ((SubValueContainer) mSensorInfo.mPhysicalSensorValueContainer).getStartTime();
    }

    public void setIntraday(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, 1);
        long endTime = calendar.getTimeInMillis();
        mSensorInfo.setValueContainers(startTime, endTime);
        //mSensorValueContainer.detachSubValueContainer(mSensorValueContainer);
        //detachSensorValueContainer();
        //mSensorValueContainer = mSensorValueContainer.applyForSubValueContainer(startTime, endTime);
    }

    public void detachSensorValueContainer() {
        mSensorInfo.detachSubValueContainer();
//        mSensorValueContainer.detachSubValueContainer(mSensorValueContainer);
//        for (ValueContainer container :
//                mMeasurementValueContainers) {
//            container.detachSubValueContainer(container);
//        }
    }

    public boolean isIntraday(long date) {
        if (!(mSensorInfo.mPhysicalSensorValueContainer instanceof SubValueContainer)) {
            throw new UnsupportedOperationException("do not invoke this method in real time");
        }
        return ((SubValueContainer) mSensorInfo.mPhysicalSensorValueContainer).contains(date);
    }

    public static void setWarnProcessor(CommonWarnProcessor warnProcessor) {
        mWarnProcessor = warnProcessor;
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

    public void notifySensorValueUpdate(int valueLogicalPosition) {
        switch (mSensorInfo.mPhysicalSensorValueContainer.interpretAddResult(valueLogicalPosition)) {
            case NEW_VALUE_ADDED:
                notifyItemInserted(mSensorInfo.mPhysicalSensorValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition));
                break;
            case LOOP_VALUE_ADDED:
                notifyItemRangeChanged(0, getItemCount());
                break;
            case VALUE_UPDATED:
                notifyItemChanged(mSensorInfo.mPhysicalSensorValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition));
                break;
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
    public PhysicalSensor.Value getItemByPosition(int position) {
//        return mIsRealTime
//                ? mSensor.getDynamicValue(position)
//                : mSensor.getIntradayHistoryValue(position);
        return mSensorInfo.mPhysicalSensorValueContainer.getValue(position);
    }

    @Override
    public int getItemCount() {
//        return mIsRealTime
//                ? mSensor.getDynamicValueSize()
//                : mSensor.getIntradayHistoryValueSize();
        return mSensorInfo.mPhysicalSensorValueContainer.size();
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor.Value value, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        mTimeSetter.setTime(value.getTimestamp());
        holder.mTvTimestamp.setText(mDateFormat.format(mTimeSetter));
        setDisplayItems(value, position, holder);
    }

    private void setDisplayItems(PhysicalSensor.Value value, int position, ViewHolder holder) {
        holder.itemView.setBackground(position % 2 == 1
                ? mSensorValueBackground
                : null);
        long timestamp = value.getTimestamp();
        //List<LogicalSensor> measurements = mSensor.getMeasurementCollections();
        LogicalSensor[] measurements = mSensorInfo.mLogicalSensors;
        ValueContainer<LogicalSensor.Value>[] valueContainers = mSensorInfo.mLogicalSensorValueContainers;
        LogicalSensor.Value measurementValue;
        TextView tvValueContent;
        LogicalSensor measurement;
        ValueContainer<LogicalSensor.Value> valueContainer;
        for (int i = 0,
             valueContentSize = holder.mTvValueContents.length,
             measurementSize = mDisplayCount - 1;
             i < valueContentSize;
             ++i) {
            tvValueContent = holder.mTvValueContents[i];
            if (mDisplayStartIndex + i < measurementSize) {
                measurement = measurements[mDisplayStartIndex + i];
                valueContainer = valueContainers[mDisplayStartIndex + i];
                //measurementValue = getCorrespondValue(measurement, timestamp, position);
                measurementValue = valueContainer.findValue(position, timestamp);
                if (measurementValue != null) {
                    tvValueContent.setText(measurement.formatValueWithUnit(measurementValue));
                    if (mWarnProcessor != null) {
                        mWarnProcessor.process(measurementValue, measurement.getConfiguration().getWarner(), tvValueContent);
                    }
                } else {
                    tvValueContent.setText(null);
                }
//                holder.mTvValueContents[i].setText(getCorrespondFormattedValueWithUnit(
//                        measurements.get(mDisplayStartIndex + i),
//                        timestamp,
//                        position));
            } else {
                tvValueContent.setText(value.getFormattedBatteryVoltage());
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, PhysicalSensor.Value value, int position, List payloads) {
        Object payload = payloads.get(0);
        if (payload instanceof Boolean) {
            if ((Boolean) payload) {
                setDisplayItems(value, position, (ViewHolder) holder);
            } else {
                onBindViewHolder(holder, value, position);
            }
        }
    }

//    private LogicalSensor.Value getCorrespondValue(LogicalSensor measurement, long timestamp, int position) {
//        return mIsRealTime
//                ? measurement.getDynamicValueContainer().findValue(position, timestamp)
//                : measurement.getHistoryValueContainer().findValue(position, timestamp);
//    }

//    private String getCorrespondFormattedValueWithUnit(LogicalSensor measurement, long timestamp, int position) {
//        LogicalSensor.Value value = mIsRealTime
//                ? measurement.findDynamicValue(position, timestamp)
//                : measurement.findHistoryValue(position, timestamp);
//        return value != null ? measurement.formatValueWithUnit(value)
//                : "";
////        return value != null ? measurement.getDataType().getDecoratedValueWithUnit(value)
////                : "";
//    }

    private static class SensorInfo {
        public final PhysicalSensor mPhysicalSensor;
        public ValueContainer<PhysicalSensor.Value> mPhysicalSensorValueContainer;
        public final LogicalSensor[] mLogicalSensors;
        public final ValueContainer<LogicalSensor.Value>[] mLogicalSensorValueContainers;

        public SensorInfo(PhysicalSensor physicalSensor, boolean isRealTime) {
            mPhysicalSensor = physicalSensor;
            mLogicalSensors = new LogicalSensor[physicalSensor.getMeasurementCollections().size()];
            physicalSensor.getMeasurementCollections().toArray(mLogicalSensors);
            if (isRealTime) {
                mPhysicalSensorValueContainer = physicalSensor.getDynamicValueContainer();
            } else {
                mPhysicalSensorValueContainer = physicalSensor.getHistoryValueContainer();
            }
            mLogicalSensorValueContainers = new ValueContainer[mLogicalSensors.length];
            if (isRealTime) {
                for (int i = 0;i < mLogicalSensors.length;++i) {
                    mLogicalSensorValueContainers[i] = mLogicalSensors[i].getDynamicValueContainer();
                }
            } else {
                for (int i = 0;i < mLogicalSensors.length;++i) {
                    mLogicalSensorValueContainers[i] = mLogicalSensors[i].getHistoryValueContainer();
                }
            }
        }

        public void setValueContainers(long startTime, long endTime) {
            detachSubValueContainer();
            mPhysicalSensorValueContainer = mPhysicalSensor.getHistoryValueContainer().applyForSubValueContainer(startTime, endTime);
            for (int i = 0;i < mLogicalSensors.length;++i) {
                mLogicalSensorValueContainers[i] = mLogicalSensors[i]
                        .getHistoryValueContainer()
                        .applyForSubValueContainer(startTime, endTime);
            }
        }

        public void detachSubValueContainer() {
            mPhysicalSensor.getHistoryValueContainer().detachSubValueContainer(mPhysicalSensorValueContainer);
            for (int i = 0;i < mLogicalSensors.length;++i) {
                mLogicalSensors[i]
                        .getHistoryValueContainer()
                        .detachSubValueContainer(mLogicalSensorValueContainers[i]);
            }
        }
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
