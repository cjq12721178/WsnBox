package com.weisi.tool.wsnbox.adapter.browse

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.data.Storage
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate
import com.cjq.tool.qbox.ui.adapter.MapAdapterDelegateManager
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.data.Device
import kotlinx.android.synthetic.main.li_device_node.view.*
import kotlinx.android.synthetic.main.li_logical_sensor.view.*

/**
 * Created by CJQ on 2018/6/8.
 */
class DataBrowseDeviceNodeAdapter(private val storage: Storage<Device>) : RecyclerViewBaseAdapter<Device>(DelegateManager()) {

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
                            false), nodeCount)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Device, position: Int) {
            val h = holder as ViewHolder
            h.tvDeviceName.text = item.name
            var i = 0
            while (i < nodeCount) {
                setNodeInfoText(h, i++, item)
            }
        }

        private fun setNodeInfoText(h: ViewHolder, index: Int, item: Device) {
            val measurement = item.nodes[index].measurement
            h.tvNodeNames[index].text = item.nodes[index].getProperName()
            BaseDataBrowseSensorAdapterDelegate.setMeasurementTimestampAndValueText(h.tvTimestamps[index], h.tvValues[index], measurement)
            BaseDataBrowseSensorAdapterDelegate.setItemBackground(h.tvNodeNames[index].parent as View, measurement)
            //val value = getValue(measurement)
            //setTimestampText(h.tvTimestamps[index], value)
            //setMeasurementValueText(h.tvValues[index], measurement, value)
        }

//        private fun setMeasurementTimestampAndValueText(tvTimestamp: TextView, tvMeasurementValue: TextView, measurement: DisplayMeasurement<*>) {
//            val value = getValue(measurement)
//            if (value != null) {
//                TIMESTAMP_SETTER.time = value.timestamp
//                tvTimestamp.text = DATE_FORMAT.format(TIMESTAMP_SETTER)
//                tvMeasurementValue.text = measurement.formatValue(value)
//                warnProcessor?.process(value, measurement.configuration.warner, tvMeasurementValue)
//            } else {
//                tvTimestamp.text = null
//                tvMeasurementValue.text = null
//            }
//        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Device, position: Int, payloads: MutableList<Any?>?) {
            if (payloads != null && !payloads.isEmpty() && payloads[0] is Int) {
                val index: Int = payloads[0] as Int
                if (index in 0..(nodeCount - 1)) {
                    setNodeInfoText(holder as ViewHolder, index, item)
                    return
                }
            }
            onBindViewHolder(holder, item, position)
        }

        override fun getItemViewType(): Int {
            return nodeCount
        }

//        private fun getValue(measurement: DisplayMeasurement<*>): DisplayMeasurement.Value? {
//            return if (realTime) {
//                measurement.realTimeValue
//            } else {
//                measurement.historyValueContainer.earliestValue
//            }
//        }

//        private fun setTimestampText(tvTimestamp: TextView, value: DisplayMeasurement.Value?) {
//            if (value != null) {
//                TIMESTAMP_SETTER.time = value.timestamp
//                tvTimestamp.text = DATE_FORMAT.format(TIMESTAMP_SETTER)
//            } else {
//                tvTimestamp.text = null
//            }
//        }
//
//        private fun setMeasurementValueText(tvMeasurementValue: TextView, measurement: DisplayMeasurement<*>, value: DisplayMeasurement.Value?) {
//            if (value != null) {
//                tvMeasurementValue.text = measurement.formatValue(value)
//                warnProcessor?.process(value, measurement.configuration.warner, tvMeasurementValue)
//            } else {
//                tvMeasurementValue.text = null
//            }
//        }
    }

    class ViewHolder(itemView: View, nodeCount: Int) : RecyclerView.ViewHolder(itemView) {

        val tvDeviceName: TextView
        val tvNodeNames: Array<TextView>
        val tvTimestamps: Array<TextView>
        val tvValues: Array<TextView>

        init {
            val context = itemView.context
            tvDeviceName = itemView.tv_device_name
            val clDevice = itemView.cl_device_node
            val constraintSet = ConstraintSet()
            var topViewId = R.id.v_device_node_divider
            //val textBackground = ContextCompat.getColor(context, R.color.bg_node)
            val margin = context.resources.getDimensionPixelSize(R.dimen.margin_small)
            constraintSet.clone(clDevice)
            val inflater = LayoutInflater.from(context)
            val liNodes = Array<View>(nodeCount) {
                val view = inflater.inflate(R.layout.li_logical_sensor, null)
                //view.setBackgroundColor(textBackground)
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
            constraintSet.applyTo(clDevice)
        }
    }
}