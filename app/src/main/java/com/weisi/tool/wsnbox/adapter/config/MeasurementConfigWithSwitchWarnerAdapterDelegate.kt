package com.weisi.tool.wsnbox.adapter.config

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner

open class MeasurementConfigWithSwitchWarnerAdapterDelegate protected constructor(configType: Int)
    : MeasurementConfigAdapterDelegate(SensorConfiguration.Measure.WT_SWITCH, configType) {

    constructor() : this(SensorConfiguration.Measure.CT_NORMAL)

    override fun getWarnerLayoutRes(): Int {
        return R.layout.group_switch_warner
    }

    override fun getWarnerTypeStringRes(): Int {
        return R.string.warner_type_switch
    }

    override fun onCreateViewHolder(itemView: View, warnerLayoutRes: Int, extraConfigLayoutRes: Int): RecyclerView.ViewHolder {
        return ViewHolder(itemView, warnerLayoutRes, extraConfigLayoutRes)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, item: SensorConfiguration.Measure?, position: Int) {
        super.onBindViewHolder(viewHolder, item, position)
        val holder = viewHolder as ViewHolder
        val warner = item!!.configuration.warner as CommonSwitchWarner
        holder.tvAbnormal.text = warner.abnormalValue.toString()
    }

    open class ViewHolder(itemView: View, warnerLayoutRes: Int, extraConfigLayoutRes: Int) : MeasurementConfigAdapterDelegate.ViewHolder(itemView, warnerLayoutRes, extraConfigLayoutRes) {
        val tvAbnormal: TextView = itemView.findViewById(R.id.tv_abnormal_value)
    }
}