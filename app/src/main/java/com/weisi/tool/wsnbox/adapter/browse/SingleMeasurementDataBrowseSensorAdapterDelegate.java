package com.weisi.tool.wsnbox.adapter.browse;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.weisi.tool.wsnbox.R;

import java.util.List;

/**
 * Created by CJQ on 2017/9/22.
 */

public class SingleMeasurementDataBrowseSensorAdapterDelegate extends BaseDataBrowseSensorAdapterDelegate {

    @Override
    public int getItemViewType() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.li_single_measurement_sensor,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor sensor, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
        setTimestampText(holder.mTvTimestamp, sensor);
        //LogicalSensor measurement = measurement.getMeasurementCollections().get(0);
        setMeasurementText(holder.mTvMeasurementNameType, holder.mTvMeasurementValue, sensor.getDisplayMeasurementByPosition(0));
        setItemBackground(holder.itemView, sensor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, PhysicalSensor sensor, int position, List payloads) {
        ViewHolder holder = (ViewHolder) viewHolder;
        switch ((int)payloads.get(0)) {
            case UPDATE_TYPE_VALUE_CHANGED: {
                setTimestampText(holder.mTvTimestamp, sensor);
                setMeasurementValueText(holder.mTvMeasurementValue, sensor.getDisplayMeasurementByPosition(0));
            } break;
            case UPDATE_TYPE_SENSOR_LABEL_CHANGED: {
                setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
            } break;
            case UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED: {
                setMeasurementNameTypeText(holder.mTvMeasurementNameType, sensor.getDisplayMeasurementByPosition(0));
            } break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvSensorNameAddress;
        private TextView mTvTimestamp;
        private TextView mTvMeasurementNameType;
        private TextView mTvMeasurementValue;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvSensorNameAddress = itemView.findViewById(R.id.tv_sensor_name_address);
            mTvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            mTvMeasurementNameType = itemView.findViewById(R.id.tv_measurement_name_type);
            mTvMeasurementValue = itemView.findViewById(R.id.tv_measurement_value);
        }
    }
}
