package com.weisi.tool.wsnbox.adapter

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate
import com.cjq.tool.qbox.ui.adapter.MapAdapterDelegateManager
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import kotlinx.android.synthetic.main.li_device_node.view.*
import kotlinx.android.synthetic.main.li_logical_sensor.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CJQ on 2018/6/8.
 */
class DataBrowseDeviceNodeAdapter(private val storage: Storage<Device>) : RecyclerViewBaseAdapter<Device>(DelegateManager()) {

//    var warnProcessor: CommonWarnProcessor<View>?
//    get() {
//        return Delegate.warnProcessor
//    }
//    set(value) {
//        Delegate.warnProcessor = warnProcessor
//    }
//
//    var realTime: Boolean
//    get() {
//        return Delegate.realTime
//    }
//    set(value) {
//        Delegate.realTime = realTime
//    }
//
//    init {
//        Delegate.realTime = realTime
//    }

    override fun getItemByPosition(position: Int): Device {
        return storage.get(position)
    }

    override fun getItemCount(): Int {
        return storage.size()
    }

    override fun getItemViewType(position: Int): Int {
        return getItemByPosition(position).nodes.size
    }

    class DelegateManager : MapAdapterDelegateManager<Device>() {

        override fun onCreateAdapterDelegate(viewType: Int): AdapterDelegate<Device> {
            return Delegate(viewType)
        }
    }

    class Delegate(private val nodeCount: Int) : AdapterDelegate<Device> {

        companion object {
            internal var realTime = true
            internal var warnProcessor: CommonWarnProcessor<View>? = null
            private val TIMESTAMP_SETTER = Date()
            private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")
        }

        init {
            if (nodeCount < 0) {
                throw IllegalArgumentException("node count may not less than 0")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater
                    .from(parent?.context)
                    .inflate(R.layout.li_device_node,
                            parent,
                            false), nodeCount);
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: Device?, position: Int) {
            val h = holder as ViewHolder
            h.tvDeviceName.text = item!!.name
            var i = 0
            while (i < nodeCount) {
                setNodeInfoText(h, i++, item)
            }
        }

        private fun setNodeInfoText(h: ViewHolder, index: Int, item: Device) {
            val sensor = item.nodes[index].sensor
            h.tvNodeNames[index].text = item.nodes[index].name ?: sensor.defaultName
            val value = getValue(sensor)
            setTimestampText(h.tvTimestamps[index], value)
            setMeasurementValueText(h.tvValues[index], sensor, value)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: Device?, position: Int, payloads: MutableList<Any?>?) {
            if (payloads != null && !payloads.isEmpty() && payloads[0] is Int) {
                val index: Int = payloads[0] as Int
                if (index in 0..(nodeCount - 1)) {
                    setNodeInfoText(holder as ViewHolder, index, item!!)
                    return
                }
            }
            onBindViewHolder(holder, item, position)
        }

        override fun getItemViewType(): Int {
            return nodeCount
        }

        private fun getValue(sensor: LogicalSensor): LogicalSensor.Value? {
            return if (realTime) {
                sensor.realTimeValue
            } else {
                sensor.historyValueContainer.earliestValue
            }
        }

        private fun setTimestampText(tvTimestamp: TextView, value: LogicalSensor.Value?) {
            if (value != null) {
                TIMESTAMP_SETTER.time = value.timestamp
                tvTimestamp.text = DATE_FORMAT.format(TIMESTAMP_SETTER)
            } else {
                tvTimestamp.text = null
            }
        }

        private fun setMeasurementValueText(tvMeasurementValue: TextView, measurement: LogicalSensor, value: LogicalSensor.Value?) {
            if (value != null) {
                tvMeasurementValue.text = measurement.formatValueWithUnit(value)
                warnProcessor?.process(value, measurement.configuration.warner, tvMeasurementValue)
            } else {
                tvMeasurementValue.text = null
            }
        }
    }

    class ViewHolder(itemView: View?, nodeCount: Int) : RecyclerView.ViewHolder(itemView) {

        val tvDeviceName: TextView
        val tvNodeNames: Array<TextView>
        val tvTimestamps: Array<TextView>
        val tvValues: Array<TextView>

        init {
            val context = itemView!!.context
            tvDeviceName = itemView.tv_device_name
            //tvNodeNames = Array<TextView>(nodeCount, {tvDeviceName})
            //tvTimestamps = Array<TextView>(nodeCount, {tvDeviceName})
            //tvValues = Array<TextView>(nodeCount, {tvDeviceName})
            val clDevice = itemView.cl_device_node
//            val vDivider = itemView.v_device_node_divider
//            val gl1 = itemView.gl_node_vertical_one_third
//            val gl2 = itemView.gl_node_vertical_two_thirds
            val constraintSet = ConstraintSet()
            var topViewId = R.id.v_device_node_divider
            //val textSize = context.resources.getDimensionPixelSize(R.dimen.size_text_activity).toFloat()
            val textBackground = ContextCompat.getColor(context, R.color.bg_node)
            val margin = context.resources.getDimensionPixelSize(R.dimen.margin_small)
            constraintSet.clone(clDevice)
            val inflater = LayoutInflater.from(context)
            val liNodes = Array<View>(nodeCount) {
                val view = inflater.inflate(R.layout.li_logical_sensor, null)
                view.setBackgroundColor(textBackground)
                val id = View.generateViewId()
                view.id = id
                clDevice.addView(view)
                constraintSet.constrainHeight(id, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                constraintSet.setMargin(id, ConstraintSet.TOP, margin)
                constraintSet.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(id, ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM)
                topViewId = id
                view
            }
            tvNodeNames = Array<TextView>(nodeCount) {
                val result = liNodes[it].tv_measurement_name_id
                result.setTextColor(Color.WHITE)
                result.tag = it
                result
            }
            tvTimestamps = Array<TextView>(nodeCount) {
                val result = liNodes[it].tv_timestamp
                result.setTextColor(Color.WHITE)
                result.tag = it
                result
            }
            tvValues = Array<TextView>(nodeCount) {
                val result = liNodes[it].tv_value
                result.setTextColor(Color.WHITE)
                result.tag = it
                result
            }
//            var i = 0
//            while (i < nodeCount) {
//                //设置节点名称
//                val tvNodeName = TextView(context)
//                tvNodeName.setTextSize(COMPLEX_UNIT_PX, textSize)
//                tvNodeName.setBackgroundColor(textBackground)
//                tvNodeName.textAlignment = TEXT_ALIGNMENT_CENTER
//                tvNodeName.tag = i
//                var id = View.generateViewId()
//                tvNodeName.id = id
//                clDevice.addView(tvNodeName)
//                constraintSet.setMargin(id, ConstraintSet.TOP, margin)
//                //constraintSet.constrainWidth(id, 0)
//                constraintSet.constrainHeight(id, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                constraintSet.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//                constraintSet.connect(id, ConstraintSet.END, R.id.gl_node_vertical_one_third, ConstraintSet.END)
//                constraintSet.connect(id, ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM)
//                //设置时间戳
//                val tvTimestamp = TextView(context)
//                tvTimestamp.setTextSize(COMPLEX_UNIT_PX, textSize)
//                tvTimestamp.setBackgroundColor(textBackground)
//                tvTimestamp.textAlignment = TEXT_ALIGNMENT_CENTER
//                tvTimestamp.tag = i
//                id = View.generateViewId()
//                tvTimestamp.id = id
//                clDevice.addView(tvTimestamp)
//                constraintSet.setMargin(id, ConstraintSet.TOP, margin)
//                //constraintSet.constrainWidth(id, 0)
//                //constraintSet.constrainHeight(id, 0)
//                constraintSet.connect(id, ConstraintSet.START, R.id.gl_node_vertical_one_third, ConstraintSet.END)
//                constraintSet.connect(id, ConstraintSet.END, R.id.gl_node_vertical_two_thirds, ConstraintSet.START)
//                constraintSet.connect(id, ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM)
//                constraintSet.connect(id, ConstraintSet.BOTTOM, tvNodeName.id, ConstraintSet.BOTTOM)
//                //设置数值
//                val tvValue = TextView(context)
//                tvValue.setTextSize(COMPLEX_UNIT_PX, textSize)
//                tvValue.setBackgroundColor(textBackground)
//                tvValue.textAlignment = TEXT_ALIGNMENT_CENTER
//                tvValue.tag = i
//                id = View.generateViewId()
//                tvValue.id = id
//                clDevice.addView(tvValue)
//                constraintSet.setMargin(id, ConstraintSet.TOP, margin)
//                //constraintSet.constrainWidth(id, 0)
//                //constraintSet.constrainHeight(id, 0)
//                constraintSet.connect(id, ConstraintSet.START, R.id.gl_node_vertical_two_thirds, ConstraintSet.END)
//                constraintSet.connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//                constraintSet.connect(id, ConstraintSet.TOP, topViewId, ConstraintSet.BOTTOM)
//                constraintSet.connect(id, ConstraintSet.BOTTOM, tvNodeName.id, ConstraintSet.BOTTOM)
//                topViewId = tvNodeName.id
//                tvNodeNames[i] = tvNodeName
//                tvTimestamps[i] = tvTimestamp
//                tvValues[i] = tvValue
//                ++i
//            }
            constraintSet.applyTo(clDevice)
        }
    }
}