package com.weisi.tool.wsnbox.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.ValueContainer
import com.weisi.tool.wsnbox.R
import kotlinx.android.synthetic.main.li_logical_sensor_info.view.*

/**
 * Created by CJQ on 2018/6/4.
 */
class LogicalSensorInfoAdapter(logicalSensor: LogicalSensor, realTime: Boolean) : SensorInfoAdapter<LogicalSensor.Value, LogicalSensor, LogicalSensorInfoAdapter.SensorInfo>(logicalSensor, realTime) {

//    companion object {
//        var warnProcessor: CommonWarnProcessor<View>? = null
//    }

    //private val sensorInfo = SensorInfo(logicalSensor, realTime)
    //private val timeSetter = Date()
    //private val dateFormat = SimpleDateFormat("HH:mm:ss")

//    fun getIntraday(): Long {
//        return if (sensorInfo.logicalSensorValueContainer !is SubValueContainer<*>) {
//            0
//        } else {
//            (sensorInfo.logicalSensorValueContainer as SubValueContainer<*>).startTime
//        }
//    }

//    fun setIntraday(date: Long) {
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = date
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        val startTime = calendar.timeInMillis
//        calendar.add(Calendar.DATE, 1)
//        val endTime = calendar.timeInMillis
//        sensorInfo.setValueContainers(startTime, endTime)
//    }

//    fun detachSensorValueContainer() {
//        sensorInfo.detachSubValueContainer()
//    }

//    fun isIntraday(date: Long): Boolean {
//        if (sensorInfo.logicalSensorValueContainer !is SubValueContainer<*>) {
//            throw UnsupportedOperationException("do not invoke this method in real time")
//        }
//        return (sensorInfo.logicalSensorValueContainer as SubValueContainer<*>).contains(date)
//    }

//    fun notifySensorValueUpdate(valueLogicalPosition: Int) {
//        when (sensorInfo.logicalSensorValueContainer.interpretAddResult(valueLogicalPosition)) {
//            NEW_VALUE_ADDED -> notifyItemInserted(sensorInfo.logicalSensorValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition))
//            LOOP_VALUE_ADDED -> notifyItemRangeChanged(0, itemCount)
//            VALUE_UPDATED -> notifyItemChanged(sensorInfo.logicalSensorValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition))
//        }
//    }

//    override fun getItemByPosition(position: Int): LogicalSensor.Value {
//        return sensorInfo.logicalSensorValueContainer.getValue(position)
//    }

//    override fun getItemCount(): Int {
//        return sensorInfo.logicalSensorValueContainer.size()
//    }

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

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, value: LogicalSensor.Value?, position: Int) {
        val holder = viewHolder as ViewHolder?
        holder!!.tvTimestamp.text = getTimeFormat(value!!.timestamp)
        holder.tvValue.text = sensorInfo.sensor.formatValueWithUnit(value)
        warnProcessor?.process(value, sensorInfo.sensor.configuration.warner, holder.tvValue)
        holder.tvBattery.text = sensorInfo.physicalSensorValueContainer.findValue(position, value.timestamp)?.formattedBatteryVoltage
        holder.itemView.setBackgroundColor(if (position % 2 == 1) {
            R.color.bg_li_sensor_data
        } else {
            android.R.color.transparent
        })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: LogicalSensor.Value?, position: Int, payloads: MutableList<Any?>?) {
        if (payloads?.get(0) == UPDATE_TYPE_BACKGROUND_COLOR) {
            holder!!.itemView.setBackgroundColor(if (position % 2 == 1) {
                R.color.bg_li_sensor_data
            } else {
                android.R.color.transparent
            })
        }
    }

    class SensorInfo(val logicalSensor: LogicalSensor, isRealTime: Boolean): SensorInfoAdapter.SensorInfo<LogicalSensor.Value, LogicalSensor>(logicalSensor, isRealTime) {
        //val sensor = logicalSensor
        var physicalSensorValueContainer: ValueContainer<PhysicalSensor.Value>
        //var logicalSensorValueContainer: ValueContainer<LogicalSensor.Value>

        init {
            val physicalSensor = logicalSensor.physicalSensor
            if (isRealTime) {
                physicalSensorValueContainer = physicalSensor.dynamicValueContainer
                //logicalSensorValueContainer = logicalSensor.dynamicValueContainer
            } else {
                physicalSensorValueContainer = physicalSensor.historyValueContainer
                //logicalSensorValueContainer = logicalSensor.historyValueContainer
            }
        }

        override fun setValueContainers(startTime: Long, endTime: Long) {
            super.setValueContainers(startTime, endTime)
            physicalSensorValueContainer = sensor.physicalSensor.historyValueContainer.applyForSubValueContainer(startTime, endTime)
        }
//        fun setValueContainers(startTime: Long, endTime: Long) {
//            detachSubValueContainer()
//            physicalSensorValueContainer = sensor.physicalSensor.historyValueContainer.applyForSubValueContainer(startTime, endTime)
//            logicalSensorValueContainer = sensor.historyValueContainer.applyForSubValueContainer(startTime, endTime)
//        }

//        fun detachSubValueContainer() {
//            sensor.physicalSensor.historyValueContainer.detachSubValueContainer(physicalSensorValueContainer)
//            sensor.historyValueContainer.detachSubValueContainer(logicalSensorValueContainer)
//        }

        override fun detachSubValueContainer() {
            super.detachSubValueContainer()
            sensor.physicalSensor.historyValueContainer.detachSubValueContainer(physicalSensorValueContainer)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTimestamp = itemView.tv_timestamp!!
        val tvValue = itemView.tv_value!!
        val tvBattery = itemView.tv_battery!!
    }
}