package com.weisi.tool.wsnbox.adapter.config

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.iot.ID
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration

open class MeasurementConfigAdapterDelegate protected constructor(warnerType: Int, configType: Int) : AdapterDelegate<SensorConfiguration.Measure> {

    private val type: Int = SensorConfiguration.Measure.buildType(warnerType, configType)

    constructor() : this(SensorConfiguration.Measure.CT_NORMAL)
    protected constructor(configType: Int) : this(SensorConfiguration.Measure.WT_NONE, configType)

    protected open fun getWarnerLayoutRes(): Int {
        return 0
    }

    protected open fun getExtraConfigLayoutRes(): Int {
        return 0
    }

    protected open fun getWarnerTypeStringRes(): Int {
        return R.string.warner_type_none
    }

    protected open fun onCreateViewHolder(itemView: View, warnerLayoutRes: Int, extraConfigLayoutRes: Int): RecyclerView.ViewHolder {
        return ViewHolder(itemView, warnerLayoutRes, extraConfigLayoutRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return onCreateViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.li_measurement_config, parent, false),
                getWarnerLayoutRes(),
                getExtraConfigLayoutRes())
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, item: SensorConfiguration.Measure?, position: Int) {
        val holder = viewHolder as ViewHolder
        holder.tvDataType.text = ID.getFormattedDataTypeValue(item!!.id)
        holder.tvDefaultName.text = item.defaultName
        holder.tvCustomName.text = item.configuration.decorator.decorateName(null)
        holder.tvWarnerType.setText(getWarnerTypeStringRes())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, item: SensorConfiguration.Measure?, position: Int, payloads: MutableList<Any?>?) {
        return onBindViewHolder(holder, item, position)
    }

    override fun getItemViewType(): Int {
        return type
    }

    open class ViewHolder(itemView: View, warnerLayoutRes: Int, extraConfigLayoutRes: Int) : RecyclerView.ViewHolder(itemView) {
        val tvDataType: TextView
        val tvDefaultName: TextView
        val tvCustomName: TextView
        val tvWarnerType: TextView

        init {
            tvDataType = itemView.findViewById(R.id.tv_measurement_id_value)
            tvDefaultName = itemView.findViewById(R.id.tv_default_name_value)
            tvCustomName = itemView.findViewById(R.id.tv_custom_name_value)
            tvWarnerType = itemView.findViewById(R.id.tv_warner_type_value)
            if ((warnerLayoutRes != 0) or (extraConfigLayoutRes != 0)) {
                val cl_hull = itemView as ConstraintLayout
                val inflater = LayoutInflater.from(itemView.context)
                val constraintSet = ConstraintSet()
                constraintSet.clone(cl_hull)
                if (extraConfigLayoutRes != 0) {
                    val vExtraConfig = inflater.inflate(extraConfigLayoutRes, null)
                    if (vExtraConfig.id == 0) {
                        vExtraConfig.id = View.generateViewId()
                    }
                    val targetId = vExtraConfig.id
                    cl_hull.addView(vExtraConfig)
                    constraintSet.connect(targetId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    constraintSet.connect(targetId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    constraintSet.connect(targetId, ConstraintSet.TOP, R.id.tv_custom_name_value, ConstraintSet.BOTTOM)
                    constraintSet.connect(R.id.tv_warner_settings, ConstraintSet.TOP, targetId, ConstraintSet.BOTTOM)
                }
                if (warnerLayoutRes != 0) {
                    val vWarnerConfig = inflater.inflate(warnerLayoutRes, null)
                    if (vWarnerConfig.id != 0) {
                        vWarnerConfig.id = View.generateViewId()
                    }
                    val targetId = vWarnerConfig.id
                    cl_hull.addView(vWarnerConfig)
                    constraintSet.clear(R.id.tv_warner_type_value, ConstraintSet.BOTTOM)
                    constraintSet.connect(targetId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                    constraintSet.connect(targetId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                    constraintSet.connect(targetId, ConstraintSet.TOP, R.id.tv_warner_type_value, ConstraintSet.BOTTOM)
                    constraintSet.connect(targetId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                }
                constraintSet.applyTo(cl_hull)
            }
        }
    }
}