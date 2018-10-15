package com.weisi.tool.wsnbox.adapter.config

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.ID
import com.cjq.lib.weisi.iot.RatchetWheelMeasurement
import com.cjq.lib.weisi.iot.Warner
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.NoHolderRecyclerViewAdapter
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner
import kotlinx.android.synthetic.main.li_measurement_config.view.*

class SensorConfigAdapter : NoHolderRecyclerViewAdapter<SensorConfiguration.Measure>() {

    var sensorConfig: SensorConfiguration? = null
    private val warnerProcessors = arrayOf(WarnerProcessor(),
            SingleRangeWarnerProcessor(),
            SwitchWarnerProcessor())
    private val extraConfigProcessors = arrayOf(ExtraConfigProcessor(),
            RatchetWheelConfigProcessor())

    override fun getItemByPosition(position: Int): SensorConfiguration.Measure {
        return sensorConfig!!.getMeasure(position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemByPosition(position).type
    }

    override fun getItemCount(): Int {
        return sensorConfig?.measureSize() ?: 0
    }

    override fun onCreateView(parent: ViewGroup?, viewType: Int): View {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.li_measurement_config, parent, false)
        val warnerLayoutRes = warnerProcessors[SensorConfiguration.Measure.getWarnerType(viewType)].getLayoutRes()
        val extraConfigLayoutRes = extraConfigProcessors[SensorConfiguration.Measure.getConfigType(viewType)].getExtraConfigLayoutRes()
        if ((warnerLayoutRes != 0) || (extraConfigLayoutRes != 0)) {
            val cl_hull = itemView.cl_hull
            val inflater = LayoutInflater.from(itemView.context)
            val constraintSet = ConstraintSet()
            constraintSet.clone(cl_hull)
            if (extraConfigLayoutRes != 0) {
                val vExtraConfig = inflater.inflate(extraConfigLayoutRes, null)
                if (vExtraConfig.id == View.NO_ID) {
                    vExtraConfig.id = View.generateViewId()
                }
                val targetId = vExtraConfig.id
                cl_hull.addView(vExtraConfig)
                constraintSet.constrainWidth(targetId, 0)
                constraintSet.constrainHeight(targetId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                constraintSet.connect(targetId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(targetId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(targetId, ConstraintSet.TOP, R.id.tv_custom_name_value, ConstraintSet.BOTTOM)
                constraintSet.connect(R.id.tv_warner_settings, ConstraintSet.TOP, targetId, ConstraintSet.BOTTOM)
            }
            if (warnerLayoutRes != 0) {
                val vWarnerConfig = inflater.inflate(warnerLayoutRes, null)
                if (vWarnerConfig.id == View.NO_ID) {
                    vWarnerConfig.id = View.generateViewId()
                }
                val targetId = vWarnerConfig.id
                cl_hull.addView(vWarnerConfig)
                constraintSet.clear(R.id.tv_warner_type_value, ConstraintSet.BOTTOM)
                constraintSet.constrainWidth(targetId, 0)
                constraintSet.constrainHeight(targetId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                constraintSet.connect(targetId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(targetId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(targetId, ConstraintSet.TOP, R.id.tv_warner_type_value, ConstraintSet.BOTTOM)
                constraintSet.connect(targetId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            }
            constraintSet.applyTo(cl_hull)
        }
        return itemView
    }

    override fun onBindViewHolder(holder: ViewHolder, item: SensorConfiguration.Measure, position: Int) {
        holder.findView<TextView>(R.id.tv_measurement_id_value).text = ID.getFormattedDataTypeValue(item.id)
        holder.findView<TextView>(R.id.tv_default_name_value).text = item.defaultName
        holder.findView<TextView>(R.id.tv_custom_name_value).text = item.configuration.decorator?.decorateName(null)
        val warnerProcessor = warnerProcessors[item.getWarnerType()]
        holder.findView<TextView>(R.id.tv_warner_type_value).setText(warnerProcessor.getTypeStringRes())
        warnerProcessor.bindView(holder, item.configuration.warner)
        extraConfigProcessors[item.getConfigType()].bindView(holder, item.configuration)
    }

    private open class WarnerProcessor {

        open fun getLayoutRes(): Int {
            return 0
        }

        open fun getTypeStringRes(): Int {
            return R.string.warner_type_none
        }

        fun bindView(holder: ViewHolder, warner: Warner<DisplayMeasurement.Value>?) {
            warner ?: return
            onBindView(holder, warner)
        }

        protected open fun onBindView(holder: ViewHolder, warner: Warner<DisplayMeasurement.Value>) {
        }
    }

    private class SingleRangeWarnerProcessor : WarnerProcessor() {

        override fun getLayoutRes(): Int {
            return R.layout.group_single_range_warner
        }

        override fun getTypeStringRes(): Int {
            return R.string.warner_type_single_range
        }

        override fun onBindView(holder: ViewHolder, warner: Warner<DisplayMeasurement.Value>) {
            holder.findView<TextView>(R.id.tv_high_limit_value).text = (warner as CommonSingleRangeWarner).highLimit.toString()
            holder.findView<TextView>(R.id.tv_low_limit_value).text = warner.lowLimit.toString()
        }
    }

    private class SwitchWarnerProcessor : WarnerProcessor() {

        override fun getLayoutRes(): Int {
            return R.layout.group_switch_warner
        }

        override fun getTypeStringRes(): Int {
            return R.string.warner_type_switch
        }

        override fun onBindView(holder: ViewHolder, warner: Warner<DisplayMeasurement.Value>) {
            holder.findView<TextView>(R.id.tv_abnormal_value).text = (warner as CommonSwitchWarner).abnormalValue.toString()
        }
    }

    private open class ExtraConfigProcessor {

        open fun getExtraConfigLayoutRes(): Int {
            return 0
        }

        open fun bindView(holder: ViewHolder, configuration: DisplayMeasurement.Configuration) {

        }
    }

    private class RatchetWheelConfigProcessor : ExtraConfigProcessor() {

        override fun getExtraConfigLayoutRes(): Int {
            return R.layout.group_ratchet_wheel
        }

        override fun bindView(holder: ViewHolder, configuration: DisplayMeasurement.Configuration) {
            holder.findView<TextView>(R.id.tv_initial_value_value).text = (configuration as RatchetWheelMeasurement.Configuration).initialValue.toString()
            holder.findView<TextView>(R.id.tv_initial_distance_value).text = configuration.initialDistance.toString()
        }
    }
}