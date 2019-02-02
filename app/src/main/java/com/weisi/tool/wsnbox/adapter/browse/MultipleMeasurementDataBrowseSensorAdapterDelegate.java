package com.weisi.tool.wsnbox.adapter.browse;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.weisi.tool.wsnbox.R;

import java.util.List;

/**
 * Created by CJQ on 2017/9/21.
 */

public class MultipleMeasurementDataBrowseSensorAdapterDelegate extends BaseDataBrowseSensorAdapterDelegate {

    private final int mMeasurementSize;

    public MultipleMeasurementDataBrowseSensorAdapterDelegate(int viewType) {
        mMeasurementSize = viewType + 1;
    }

    @Override
    public int getItemViewType() {
        return mMeasurementSize - 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.li_multiple_measurement_sensor,
                        parent,
                        false),
                mMeasurementSize);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor sensor, int position) {
//        if (BuildConfig.APP_DEBUG) {
//            Log.d(Tag.LOG_TAG_D_TEST, "onBind pos: " + position + "addr: " + sensor.getRawAddress());
//        }
        ViewHolder holder = (ViewHolder) viewHolder;
        setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
        setTimestampText(holder.mTvTimestamp, sensor);
        for (int i = 0;i < mMeasurementSize;++i) {
            setMeasurementText(holder.mTvMeasurementNameTypes[i], holder.mTvMeasurementValues[i], sensor.getDisplayMeasurementByPosition(i));
        }
        setItemBackground(holder.itemView, sensor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor sensor, int position, List payloads) {
        ViewHolder holder = (ViewHolder) viewHolder;
        switch ((int)payloads.get(0)) {
            case UPDATE_TYPE_VALUE_CHANGED: {
//                if (BuildConfig.APP_DEBUG) {
//                    Log.d(Tag.LOG_TAG_D_TEST, "onBind payload pos: " + position + "addr: " + sensor.getRawAddress());
//                }
                setTimestampText(holder.mTvTimestamp, sensor);
                for (int i = 0;i < mMeasurementSize;++i) {
                    setMeasurementValueText(holder.mTvMeasurementValues[i], sensor.getDisplayMeasurementByPosition(i));
                }
            } break;
            case UPDATE_TYPE_SENSOR_LABEL_CHANGED: {
                setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
            } break;
            case UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED: {
                for (int i = 0;i < mMeasurementSize;++i) {
                    setMeasurementNameTypeText(holder.mTvMeasurementNameTypes[i], sensor.getDisplayMeasurementByPosition(i));
                }
            } break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvSensorNameAddress;
        private final TextView mTvTimestamp;
        private final TextView[] mTvMeasurementNameTypes;
        private final TextView[] mTvMeasurementValues;

        public ViewHolder(View itemView, int measurementSize) {
            super(itemView);
            mTvSensorNameAddress = itemView.findViewById(R.id.tv_sensor_name_address);
            mTvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            mTvMeasurementNameTypes = new TextView[measurementSize];
            mTvMeasurementValues = new TextView[measurementSize];
            Context context = itemView.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            //Resources resources = context.getResources();
            ConstraintLayout clItem = (ConstraintLayout) itemView;
            //float textSize = resources.getDimensionPixelSize(R.dimen.size_text_activity);
            //int textHeight = resources.getDimensionPixelSize(R.dimen.size_sensor_browse_measurement_height);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(clItem);
            //LinearLayout llMeasurementNameTypes = (LinearLayout) itemView.findViewById(R.id.ll_measurement_name_types);
            //LinearLayout llMeasurementValues = (LinearLayout) itemView.findViewById(R.id.ll_measurement_values);
            int[] measurementIds = new int[measurementSize];
            for (int i = 0;i < measurementSize;++i) {
                LinearLayout llMeasurement = (LinearLayout) inflater.inflate(R.layout.group_measurement_name_value, null);
                if (llMeasurement.getId() == View.NO_ID) {
                    llMeasurement.setId(View.generateViewId());
                }
                clItem.addView(llMeasurement);
                int measurementId = llMeasurement.getId();
                constraintSet.constrainWidth(measurementId, ConstraintSet.MATCH_CONSTRAINT);
                constraintSet.constrainHeight(measurementId, ConstraintSet.WRAP_CONTENT);
                constraintSet.connect(measurementId, ConstraintSet.START, R.id.gl_middle_vertical, ConstraintSet.END);
                constraintSet.connect(measurementId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                measurementIds[i] = measurementId;
                mTvMeasurementNameTypes[i] = llMeasurement.findViewById(R.id.tv_measurement_name_type);
                mTvMeasurementValues[i] = llMeasurement.findViewById(R.id.tv_measurement_value);
//                setMeasurementView(context,
//                        llMeasurementNameTypes,
//                        mTvMeasurementNameTypes,
//                        i,
//                        textSize,
//                        textHeight);
//                setMeasurementView(context,
//                        llMeasurementValues,
//                        mTvMeasurementValues,
//                        i,
//                        textSize,
//                        textHeight);
            }
            constraintSet.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, measurementIds, null, ConstraintSet.CHAIN_SPREAD);
            constraintSet.applyTo(clItem);
        }

//        private LinearLayout createMeasurementNameAndValueView(Context context) {
//            LinearLayout llMeasurement = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.group_measurement_name_value, null);
//            if (llMeasurement.getId() == View.NO_ID) {
//                llMeasurement.setId(View.generateViewId());
//            }
//            return llMeasurement;
//        }

//        private void setMeasurementView(Context context,
//                                        LinearLayout llMeasurementsSrc,
//                                        TextView[] tvMeasurementsDst,
//                                        int index,
//                                        float textSize,
//                                        int textHeight) {
//            TextView tvMeasurementSrc = new TextView(context);
//            tvMeasurementSrc.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//            tvMeasurementSrc.setGravity(Gravity.CENTER);
//            //tvMeasurementSrc.setEllipsize(TextUtils.TruncateAt.END);
//            //tvMeasurementSrc.setMaxLines(1);
//            //tvMeasurementSrc.setSelected(true);
//            tvMeasurementSrc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    textHeight));
//            tvMeasurementsDst[index] = tvMeasurementSrc;
//            llMeasurementsSrc.addView(tvMeasurementSrc);
//        }
    }
}
