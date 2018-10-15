package com.weisi.tool.wsnbox.adapter.info

import android.support.v7.widget.RecyclerView
import android.view.View
import com.cjq.lib.weisi.iot.Measurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.container.SubValueContainer
import com.cjq.lib.weisi.iot.container.Value
import com.cjq.lib.weisi.iot.container.ValueContainer
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import java.text.SimpleDateFormat
import java.util.*



/**
 * Created by CJQ on 2018/6/5.
 */
open class SensorInfoAdapter<V: Value, S : Sensor, out I: SensorInfoAdapter.SensorInfo<V, S>>(s: S, realTime: Boolean) : RecyclerViewBaseAdapter<V>() {

    companion object {
        @JvmStatic
        var warnProcessor: CommonWarnProcessor<View>? = null
        @JvmStatic
        val UPDATE_TYPE_BACKGROUND_COLOR = 1
    }

    protected val sensorInfo = onCreateSensorInfo(s, realTime)
    private val timeSetter = Date()
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    protected open fun onCreateSensorInfo(s: S, realTime: Boolean): I {
        throw NullPointerException("onCreateSensorInfo may be override")
    }

    fun getIntraday(): Long {
        return if (sensorInfo.mainValueContainer !is SubValueContainer) {
            0
        } else {
            (sensorInfo.mainValueContainer as SubValueContainer).startTime
        }
    }

    fun setIntraday(date: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis
        //val startTime = TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(date)) - TimeZone.getDefault().rawOffset
        //val endTime = startTime + TimeUnit.DAYS.toMillis(1)
        sensorInfo.setValueContainers(startTime, endTime)
    }

    fun detachSensorValueContainer() {
        sensorInfo.detachSubValueContainer()
    }

    fun isIntraday(date: Long): Boolean {
        if (sensorInfo.mainValueContainer !is SubValueContainer<*>) {
            throw UnsupportedOperationException("do not invoke this method in real time")
        }
        return (sensorInfo.mainValueContainer as SubValueContainer<*>).contains(date)
    }

    fun notifySensorValueUpdate(valueLogicalPosition: Int, rv: RecyclerView) {
        when (sensorInfo.mainValueContainer.interpretAddResult(valueLogicalPosition)) {
            ValueContainer.NEW_VALUE_ADDED -> {
                val insertedItemPosition = if (sensorInfo.realTime) {
                    itemCount - sensorInfo.mainValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition) - 1
                } else {
                    sensorInfo.mainValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition)
                }
                notifyItemInserted(insertedItemPosition)
                notifyItemRangeChanged(insertedItemPosition + 1, itemCount - insertedItemPosition - 1, UPDATE_TYPE_BACKGROUND_COLOR)
                correctItemLocation(rv)
            }
            ValueContainer.LOOP_VALUE_ADDED -> notifyItemRangeChanged(0, itemCount)
            ValueContainer.VALUE_UPDATED -> notifyItemChanged(if (sensorInfo.realTime) {
                itemCount - sensorInfo.mainValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition) - 1
            } else {
                sensorInfo.mainValueContainer.getPhysicalPositionByLogicalPosition(valueLogicalPosition)
            })
        }
    }

    private fun correctItemLocation(rv: RecyclerView) {
        if (rv.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rv.canScrollVertically(-1)) {
            rv.scrollToPosition(0)
        }
    }

    override fun getItemByPosition(position: Int): V {
        return sensorInfo.mainValueContainer.getValue(if (sensorInfo.realTime) {
            itemCount - position - 1
        } else {
            position
        })
    }

    override fun getItemCount(): Int {
        return sensorInfo.mainValueContainer.size()
    }

    protected fun getTimeFormat(milliInSeconds: Long): String {
        timeSetter.time = milliInSeconds
        return dateFormat.format(timeSetter)
    }

    abstract class SensorInfo<V: Value, out S : Sensor>(s: S, val realTime: Boolean) {

        val sensor = s
        var mainValueContainer = if (realTime) {
            getMainMeasurement().getDynamicValueContainer()
        } else {
            getMainMeasurement().getHistoryValueContainer()
        }

        abstract fun getMainMeasurement() : Measurement<V, *>

        open fun setValueContainers(startTime: Long, endTime: Long) {
            detachSubValueContainer()
            mainValueContainer = getMainMeasurement().getHistoryValueContainer().applyForSubValueContainer(startTime, endTime)
        }

        open fun detachSubValueContainer() {
            getMainMeasurement().getHistoryValueContainer().detachSubValueContainer(mainValueContainer)
        }
    }
}