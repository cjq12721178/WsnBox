package com.weisi.tool.wsnbox.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import kotlinx.android.synthetic.main.li_logical_sensor.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CJQ on 2018/6/5.
 */
class DataBrowseLogicalSensorAdapter(private val storage: Storage<LogicalSensor>, var realTime: Boolean) : RecyclerViewBaseAdapter<LogicalSensor>() {

    companion object {
        @JvmStatic
        val UPDATE_TYPE_VALUE_CHANGED = 1
        @JvmStatic
        val UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED = 2
    }

    private val TIMESTAMP_SETTER = Date()
    private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")
    var showMeasurementNameOrId = true
    var warnProcessor: CommonWarnProcessor<View>? = null

    override fun getItemByPosition(position: Int): LogicalSensor {
        return storage.get(position)
    }

    override fun getItemCount(): Int {
        return storage.size()
    }

    private fun getValue(sensor: LogicalSensor): LogicalSensor.Value {
        return if (realTime) {
            sensor.realTimeValue
        } else {
            sensor.historyValueContainer.earliestValue
        }
    }

    private fun setSensorNameIdText(tvSensorNameId: TextView, sensor: LogicalSensor) {
        tvSensorNameId.text = if (showMeasurementNameOrId)
            sensor.name
        else
            sensor.id.toString()
    }

    protected fun setTimestampText(tvTimestamp: TextView, sensor: LogicalSensor) {
        TIMESTAMP_SETTER.time = getValue(sensor).timestamp
        tvTimestamp.text = DATE_FORMAT.format(TIMESTAMP_SETTER)
    }

    protected fun setMeasurementValueText(tvMeasurementValue: TextView, measurement: LogicalSensor) {
        val value = getValue(measurement)
        if (value != null) {
            tvMeasurementValue.text = measurement.formatValueWithUnit(value)
            warnProcessor?.process(value, measurement.configuration.warner, tvMeasurementValue)
        } else {
            tvMeasurementValue.text = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater
                .from(parent?.context)
                .inflate(R.layout.li_logical_sensor,
                        parent,
                        false));
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: LogicalSensor?, position: Int) {
        val h = holder as ViewHolder
        setSensorNameIdText(h.tvSensorNameId, item!!)
        setTimestampText(holder.tvTimestamp, item)
        setMeasurementValueText(holder.tvValue, item)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: LogicalSensor?, position: Int, payloads: MutableList<Any?>?) {
        val h = holder as ViewHolder
        when (payloads?.get(0)) {
            UPDATE_TYPE_VALUE_CHANGED -> {
                setTimestampText(holder.tvTimestamp, item!!)
                setMeasurementValueText(holder.tvValue, item)
            }
            UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED -> {
                setSensorNameIdText(h.tvSensorNameId, item!!)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSensorNameId = itemView.tv_measurement_name_id!!
        val tvTimestamp = itemView.tv_timestamp!!
        val tvValue = itemView.tv_value!!
    }
}