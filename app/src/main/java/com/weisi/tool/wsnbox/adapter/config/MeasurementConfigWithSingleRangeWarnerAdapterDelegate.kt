package com.weisi.tool.wsnbox.adapter.config

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner

open class MeasurementConfigWithSingleRangeWarnerAdapterDelegate protected constructor(configType: Int)
    : MeasurementConfigAdapterDelegate(SensorConfiguration.Measure.WT_SINGLE_RANGE, configType) {

    constructor() : this(SensorConfiguration.Measure.CT_NORMAL)

    override fun getWarnerLayoutRes(): Int {
        return R.layout.group_single_range_warner
    }

    override fun getWarnerTypeStringRes(): Int {
        return R.string.warner_type_single_range
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, item: SensorConfiguration.Measure?, position: Int) {
        super.onBindViewHolder(viewHolder, item, position)
        val holder = viewHolder as ViewHolder
        val warner = item!!.configuration.warner as CommonSingleRangeWarner
        holder.tvHighLimit.text = warner.highLimit.toString()
        holder.tvLowLimit.text = warner.lowLimit.toString()
    }

    open class ViewHolder(itemView: View, warnerLayoutRes: Int, extraConfigLayoutRes: Int) : MeasurementConfigAdapterDelegate.ViewHolder(itemView, warnerLayoutRes, extraConfigLayoutRes) {
        val tvHighLimit: TextView
        val tvLowLimit: TextView

        init {
            tvHighLimit = itemView.findViewById(R.id.tv_high_limit_value)
            tvLowLimit = itemView.findViewById(R.id.tv_low_limit_value)
        }
    }
}