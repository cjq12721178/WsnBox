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
import com.cjq.lib.weisi.iot.ValueContainer;
import com.weisi.tool.wsnbox.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by CJQ on 2017/11/6.
 */

public class PhysicalSensorInfoAdapter extends SensorInfoAdapter<PhysicalSensor.Value, PhysicalSensor, PhysicalSensorInfoAdapter.SensorInfo> {

    public static final int MAX_DISPLAY_COUNT = 3;
    public static final int HAS_NO_EXCESS_DISPLAY_ITEM = 0;
    public static final int ONLY_HAS_RIGHT_DISPLAY_ITEM = 1;
    public static final int HAS_BOTH_DISPLAY_ITEM = 2;
    public static final int ONLY_HAS_LEFT_DISPLAY_ITEM = 3;

    //private static CommonWarnProcessor<View> warnProcessor;

    private final Drawable mSensorValueBackground;
    //private final Date mTimeSetter = new Date();
    //private final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");
    //private final SensorInfo mSensorInfo;

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
        mDisplayCount = sensor.getMeasurementCollections().size() + 1;
        mDisplayStartIndex = displayStartIndex;
        //mSensorInfo = new SensorInfo(sensor, isRealTime);
    }

    @NotNull
    @Override
    protected SensorInfo onCreateSensorInfo(@NotNull PhysicalSensor sensor, boolean realTime) {
        return new SensorInfo(sensor, realTime);
    }

    //    public long getIntraday() {
//        if (!(mSensorInfo.mPhysicalSensorValueContainer instanceof SubValueContainer)) {
//            return 0;
//        }
//        return ((SubValueContainer) mSensorInfo.mPhysicalSensorValueContainer).getStartTime();
//    }

//    public void setIntraday(long date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(date);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        long startTime = calendar.getTimeInMillis();
//        calendar.add(Calendar.DATE, 1);
//        long endTime = calendar.getTimeInMillis();
//        mSensorInfo.setValueContainers(startTime, endTime);
//    }

//    public void detachSensorValueContainer() {
//        mSensorInfo.detachSubValueContainer();
//    }

//    public boolean isIntraday(long date) {
//        if (!(mSensorInfo.mPhysicalSensorValueContainer instanceof SubValueContainer)) {
//            throw new UnsupportedOperationException("do not invoke this method in real time");
//        }
//        return ((SubValueContainer) mSensorInfo.mPhysicalSensorValueContainer).contains(date);
//    }

//    public static void setWarnProcessor(CommonWarnProcessor warnProcessor) {
//        PhysicalSensorInfoAdapter.warnProcessor = warnProcessor;
//    }

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

//    public void notifySensorValueUpdate(int valueLogicalPosition) {
//        switch (mSensorInfo.mPhysicalSensorValueContainer.interpretAddResult(valueLogicalPosition)) {
//            case NEW_VALUE_ADDED:
//                notifyItemInserted(mSensorInfo.mPhysicalSensorValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition));
//                break;
//            case LOOP_VALUE_ADDED:
//                notifyItemRangeChanged(0, getItemCount());
//                break;
//            case VALUE_UPDATED:
//                notifyItemChanged(mSensorInfo.mPhysicalSensorValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition));
//                break;
//        }
//    }

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

//    @Override
//    public PhysicalSensor.Value getItemByPosition(int position) {
//        return mSensorInfo.mPhysicalSensorValueContainer.getValue(position);
//    }

//    @Override
//    public int getItemCount() {
//        return mSensorInfo.mPhysicalSensorValueContainer.size();
//    }

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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor.Value value, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        //mTimeSetter.setTime(value.getTimestamp());
        holder.mTvTimestamp.setText(getTimeFormat(value.getTimestamp()));
        setDisplayItems(value, position, holder);
    }

    private void setDisplayItems(PhysicalSensor.Value value, int position, ViewHolder holder) {
        holder.itemView.setBackground(position % 2 == 1
                ? mSensorValueBackground
                : null);
        long timestamp = value.getTimestamp();
        LogicalSensor[] measurements = getSensorInfo().mLogicalSensors;
        ValueContainer<LogicalSensor.Value>[] valueContainers = getSensorInfo().mLogicalSensorValueContainers;
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
                measurementValue = valueContainer.findValue(position, timestamp);
                if (measurementValue != null) {
                    tvValueContent.setText(measurement.formatValueWithUnit(measurementValue));
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, PhysicalSensor.Value value, int position, List payloads) {
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

    public static class SensorInfo extends SensorInfoAdapter.SensorInfo<PhysicalSensor.Value, PhysicalSensor> {
        //public final PhysicalSensor mPhysicalSensor;
        //public ValueContainer<PhysicalSensor.Value> mPhysicalSensorValueContainer;
        public final LogicalSensor[] mLogicalSensors;
        public final ValueContainer<LogicalSensor.Value>[] mLogicalSensorValueContainers;

        public SensorInfo(PhysicalSensor physicalSensor, boolean isRealTime) {
            super(physicalSensor, isRealTime);
            //mPhysicalSensor = physicalSensor;
            mLogicalSensors = new LogicalSensor[physicalSensor.getMeasurementCollections().size()];
            physicalSensor.getMeasurementCollections().toArray(mLogicalSensors);
//            if (isRealTime) {
//                mPhysicalSensorValueContainer = physicalSensor.getDynamicValueContainer();
//            } else {
//                mPhysicalSensorValueContainer = physicalSensor.getHistoryValueContainer();
//            }
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
            super.setValueContainers(startTime, endTime);
            //detachSubValueContainer();
            //mPhysicalSensorValueContainer = mPhysicalSensor.getHistoryValueContainer().applyForSubValueContainer(startTime, endTime);
            for (int i = 0;i < mLogicalSensors.length;++i) {
                mLogicalSensorValueContainers[i] = mLogicalSensors[i]
                        .getHistoryValueContainer()
                        .applyForSubValueContainer(startTime, endTime);
            }
        }

        public void detachSubValueContainer() {
            super.detachSubValueContainer();
            //mPhysicalSensor.getHistoryValueContainer().detachSubValueContainer(mPhysicalSensorValueContainer);
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
