package com.weisi.tool.wsnbox.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjq.lib.weisi.iot.LogicalSensor;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.weisi.tool.wsnbox.BuildConfig;
import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.util.Tag;

import java.util.List;

/**
 * Created by CJQ on 2017/9/21.
 */

public class MultipleMeasurementSensorAdapterDelegate extends BaseSensorAdapterDelegate {

    private final int mMeasurementSize;

    public MultipleMeasurementSensorAdapterDelegate(int viewType) {
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
        if (BuildConfig.APP_DEBUG) {
            Log.d(Tag.LOG_TAG_D_TEST, "onBind pos: " + position + "addr: " + sensor.getRawAddress());
        }
        ViewHolder holder = (ViewHolder) viewHolder;
        setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
        setTimestampText(holder.mTvTimestamp, sensor);
        List<LogicalSensor> measurements = sensor.getMeasurementCollections();
        //LogicalSensor measurement;
        for (int i = 0;i < mMeasurementSize;++i) {
            setMeasurementText(holder.mTvMeasurementNameTypes[i], holder.mTvMeasurementValues[i], measurements.get(i));
//            measurement = measurements.get(i);
//            setMeasurementNameTypeText(holder.mTvMeasurementNameTypes[i], measurement);
//            setMeasurementValueText(holder.mTvMeasurementValues[i], measurement);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor sensor, int position, List payloads) {
        ViewHolder holder = (ViewHolder) viewHolder;
        //setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
        List<LogicalSensor> measurements = sensor.getMeasurementCollections();
        LogicalSensor measurement;
        switch ((int)payloads.get(0)) {
            case UPDATE_TYPE_VALUE_CHANGED: {
                if (BuildConfig.APP_DEBUG) {
                    Log.d(Tag.LOG_TAG_D_TEST, "onBind payload pos: " + position + "addr: " + sensor.getRawAddress());
                }
                setTimestampText(holder.mTvTimestamp, sensor);
                for (int i = 0;i < mMeasurementSize;++i) {
                    measurement = measurements.get(i);
                    setMeasurementValueText(holder.mTvMeasurementValues[i], measurement);
                }
            } break;
            case UPDATE_TYPE_SENSOR_LABEL_CHANGED: {
                setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
            } break;
            case UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED: {
                for (int i = 0;i < mMeasurementSize;++i) {
                    measurement = measurements.get(i);
                    setMeasurementNameTypeText(holder.mTvMeasurementNameTypes[i], measurement);
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
            mTvSensorNameAddress = (TextView) itemView.findViewById(R.id.tv_sensor_name_address);
            mTvTimestamp = (TextView) itemView.findViewById(R.id.tv_timestamp);
            mTvMeasurementNameTypes = new TextView[measurementSize];
            mTvMeasurementValues = new TextView[measurementSize];
            Context context = itemView.getContext();
            Resources resources = context.getResources();
            float textSize = resources.getDimensionPixelSize(R.dimen.size_text_activity);
            int textHeight = resources.getDimensionPixelSize(R.dimen.size_sensor_browse_measurement_height);
            LinearLayout llMeasurementNameTypes = (LinearLayout) itemView.findViewById(R.id.ll_measurement_name_types);
            LinearLayout llMeasurementValues = (LinearLayout) itemView.findViewById(R.id.ll_measurement_values);
            for (int i = 0;i < measurementSize;++i) {
                setMeasurementView(context,
                        llMeasurementNameTypes,
                        mTvMeasurementNameTypes,
                        i,
                        textSize,
                        textHeight);
                setMeasurementView(context,
                        llMeasurementValues,
                        mTvMeasurementValues,
                        i,
                        textSize,
                        textHeight);
            }
        }

        private void setMeasurementView(Context context,
                                        LinearLayout llMeasurementsSrc,
                                        TextView[] tvMeasurementsDst,
                                        int index,
                                        float textSize,
                                        int textHeight) {
            TextView tvMeasurementSrc = new TextView(context);
            tvMeasurementSrc.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tvMeasurementSrc.setGravity(Gravity.CENTER);
            //tvMeasurementSrc.setEllipsize(TextUtils.TruncateAt.END);
            //tvMeasurementSrc.setMaxLines(1);
            //tvMeasurementSrc.setSelected(true);
            tvMeasurementSrc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    textHeight));
            tvMeasurementsDst[index] = tvMeasurementSrc;
            llMeasurementsSrc.addView(tvMeasurementSrc);
        }
    }
}
