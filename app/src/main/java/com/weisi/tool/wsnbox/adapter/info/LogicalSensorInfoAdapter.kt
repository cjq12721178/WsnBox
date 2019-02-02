package com.weisi.tool.wsnbox.adapter.info

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.Measurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.container.ValueContainer
import com.weisi.tool.wsnbox.R
import kotlinx.android.synthetic.main.li_logical_sensor_info.view.*

/**
 * Created by CJQ on 2018/6/4.
 */
class LogicalSensorInfoAdapter(logicalSensor: LogicalSensor, realTime: Boolean) : SensorInfoAdapter<DisplayMeasurement.Value, LogicalSensor, LogicalSensorInfoAdapter.SensorInfo>(logicalSensor, realTime) {

    override fun onCreateSensorInfo(s: LogicalSensor, realTime: Boolean): SensorInfo {
        return SensorInfo(s, realTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater
                .from(parent!!.context)
                .inflate(R.layout.li_logical_sensor_info,
                        parent,
                        false))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, value: DisplayMeasurement.Value?, position: Int) {
        val holder = viewHolder as ViewHolder?
        holder!!.tvTimestamp.text = getTimeFormat(value!!.timestamp)
        holder.tvValue.text = sensorInfo.sensor.practicalMeasurement.formatValue(value)
        warnProcessor?.process(value, sensorInfo.sensor.practicalMeasurement.configuration.warner, holder.tvValue)
        holder.tvBattery.text = sensorInfo.sensorInfoValueContainer.findValue(position, value.timestamp)?.formattedBatteryVoltage
        holder.itemView.setBackgroundColor(if (position % 2 == 1) {
            R.color.bg_real_time_sensor_data
        } else {
            android.R.color.transparent
        })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: DisplayMeasurement.Value?, position: Int, payloads: MutableList<Any?>?) {
        if (payloads?.get(0) == UPDATE_TYPE_BACKGROUND_COLOR) {
            holder!!.itemView.setBackgroundColor(if (position % 2 == 1) {
                R.color.bg_real_time_sensor_data
            } else {
                android.R.color.transparent
            })
        }
    }

    class SensorInfo(s: LogicalSensor, realTime: Boolean) : SensorInfoAdapter.SensorInfo<DisplayMeasurement.Value, LogicalSensor>(s, realTime) {

        var sensorInfoValueContainer: ValueContainer<Sensor.Info.Value>

        init {
            if (realTime) {
                sensorInfoValueContainer = sensor.info.dynamicValueContainer
            } else {
                sensorInfoValueContainer = sensor.info.historyValueContainer
            }
        }

        override fun getMainMeasurement(): Measurement<DisplayMeasurement.Value, *> {
            return sensor.practicalMeasurement
        }

        override fun setValueContainers(startTime: Long, endTime: Long) {
            super.setValueContainers(startTime, endTime)
            sensorInfoValueContainer = sensor.info.historyValueContainer.applyForSubValueContainer(startTime, endTime)
        }

        override fun detachSubValueContainer() {
            super.detachSubValueContainer()
            sensor.info.historyValueContainer.detachSubValueContainer(sensorInfoValueContainer)
        }

    }

//    class SensorInfo(val logicalSensor: LogicalSensor, isRealTime: Boolean): SensorInfoAdapter.SensorInfo<DisplayMeasurement.Value, LogicalSensor>(logicalSensor, isRealTime) {
//
//        var sensorInfoValueContainer: ValueContainer<Sensor.Info.Value>
//
//        init {
//            if (isRealTime) {
//                sensorInfoValueContainer = sensor.info.dynamicValueContainer
//            } else {
//                sensorInfoValueContainer = sensor.info.historyValueContainer
//            }
//        }
//
//        override fun getMainMeasurement(): Measurement<DisplayMeasurement.Value, *> {
//            return sensor.practicalMeasurement
//        }
//
//        override fun setValueContainers(startTime: Long, endTime: Long) {
//            super.setValueContainers(startTime, endTime)
//            sensorInfoValueContainer = sensor.info.historyValueContainer.applyForSubValueContainer(startTime, endTime)
//        }
//
//        override fun detachSubValueContainer() {
//            super.detachSubValueContainer()
//            logicalSensor.info.historyValueContainer.detachSubValueContainer(sensorInfoValueContainer)
//        }
//    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTimestamp = itemView.tv_timestamp!!
        val tvValue = itemView.tv_value!!
        val tvBattery = itemView.tv_battery!!
    }
}