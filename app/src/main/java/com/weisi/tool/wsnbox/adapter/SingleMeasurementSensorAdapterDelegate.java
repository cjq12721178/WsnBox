package com.weisi.tool.wsnbox.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.lib.weisi.node.Sensor;
import com.weisi.tool.wsnbox.R;

import java.util.List;

/**
 * Created by CJQ on 2017/9/22.
 */

public class SingleMeasurementSensorAdapterDelegate extends BaseSensorAdapterDelegate {

    @Override
    public int getItemViewType() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_single_measurement_sensor,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Sensor sensor, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
        setTimestampText(holder.mTvTimestamp, sensor);
        Sensor.Measurement measurement = sensor.getMeasurementCollections().get(0);
        setMeasurementText(holder.mTvMeasurementNameType, holder.mTvMeasurementValue, measurement);
//        setMeasurementNameTypeText(holder.mTvMeasurementNameType, measurement);
//        setMeasurementValueText(holder.mTvMeasurementValue, measurement);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Sensor sensor, int position, List payloads) {
        ViewHolder holder = (ViewHolder) viewHolder;
        switch ((int)payloads.get(0)) {
            case UPDATE_TYPE_VALUE_CHANGED: {
                setTimestampText(holder.mTvTimestamp, sensor);
                setMeasurementValueText(holder.mTvMeasurementValue, sensor.getMeasurementCollections().get(0));
            } break;
            case UPDATE_TYPE_SENSOR_LABEL_CHANGED: {
                setSensorNameAddressText(holder.mTvSensorNameAddress, sensor);
            } break;
            case UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED: {
                setMeasurementNameTypeText(holder.mTvMeasurementNameType, sensor.getMeasurementCollections().get(0));
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
            mTvSensorNameAddress = (TextView) itemView.findViewById(R.id.tv_sensor_name_address);
            mTvTimestamp = (TextView) itemView.findViewById(R.id.tv_timestamp);
            mTvMeasurementNameType = (TextView) itemView.findViewById(R.id.tv_measurement_name_type);
            mTvMeasurementValue = (TextView) itemView.findViewById(R.id.tv_measurement_value);
        }
    }
}
