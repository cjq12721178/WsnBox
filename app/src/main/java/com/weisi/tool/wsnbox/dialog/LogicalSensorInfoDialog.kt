package com.weisi.tool.wsnbox.dialog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.info.LogicalSensorInfoAdapter
import kotlinx.android.synthetic.main.fragment_logical_sensor_info.view.*

/**
 * Created by CJQ on 2018/6/5.
 */
class LogicalSensorInfoDialog : SensorInfoDialog<LogicalSensor, LogicalSensorInfoAdapter>() {

    override fun onCreateAdapter(savedInstanceState: Bundle?): LogicalSensorInfoAdapter {
        return LogicalSensorInfoAdapter(sensor, realTime)
    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_logical_sensor_info, container, false)
        view.tv_sensor_info_label.text = getString(R.string.sensor_info_title, sensor.practicalMeasurement.name)
        view.tv_sensor_id.text = getString(R.string.sensor_info_id, sensor.id.toString())
        view.tv_sensor_state.setText(if (sensor.state == Sensor.ON_LINE) {
            R.string.sensor_info_state_on
        } else {
            R.string.sensor_info_state_off
        })
        return view
    }

    override fun onBindRecyclerView(view: View): RecyclerView {
        val rv = view.findViewById<View>(R.id.rv_logical_sensor_info) as RecyclerView
        rv.layoutManager = LinearLayoutManager(context)
        return rv
    }

    fun notifyRealTimeValueChanged(logicalSensor: LogicalSensor, valuePosition: Int) {
        notifySensorValueChanged(logicalSensor, valuePosition)
    }

    fun notifyMeasurementHistoryValueChanged(measurement: PracticalMeasurement, valuePosition: Int) {
        if (measurement === sensor.practicalMeasurement) {
            notifySensorValueChanged(sensor, valuePosition)
        }
    }

    fun notifySensorInfoHistoryValueChanged(info: Sensor.Info, valuePosition: Int) {
        if (info === sensor.info) {
            val value = info.getValueByContainerAddMethodReturnValue(info.historyValueContainer, valuePosition) ?: return
            val position = sensor.practicalMeasurement.historyValueContainer.findValuePosition(valuePosition, value.timestamp)
            if (position >= 0) {
                adapter.notifySensorValueUpdate(- position - 1, rvSensorInfo)
            }
        }
    }
}