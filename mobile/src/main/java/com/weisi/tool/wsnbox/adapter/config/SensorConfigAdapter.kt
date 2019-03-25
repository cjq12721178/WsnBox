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
import com.cjq.lib.weisi.iot.container.Corrector
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.NoHolderRecyclerViewAdapter
import com.weisi.tool.wsnbox.bean.configuration.SensorConfiguration
import com.weisi.tool.wsnbox.bean.corrector.LinearFittingCorrector
import com.weisi.tool.wsnbox.bean.decorator.CommonMeasurementDecorator
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner
import kotlinx.android.synthetic.main.group_lfc_item.view.*
import kotlinx.android.synthetic.main.li_measurement_config.view.*

class SensorConfigAdapter : NoHolderRecyclerViewAdapter<SensorConfiguration.Measure>() {

    var sensorConfig: SensorConfiguration? = null
    private val warnerProcessors = arrayOf(WarnerProcessor(),
            SingleRangeWarnerProcessor(),
            SwitchWarnerProcessor())
    private val extraConfigProcessors = arrayOf(ExtraConfigProcessor(),
            RatchetWheelConfigProcessor())
    private val correctorProcessors = arrayOf(CorrectorProcessor(),
            LinearFittingCorrectorProcessor())

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
        val context = itemView.context
        val warnerLayoutRes = warnerProcessors[SensorConfiguration.Measure.getWarnerType(viewType)].getLayoutRes()
        val extraConfigLayoutRes = extraConfigProcessors[SensorConfiguration.Measure.getConfigType(viewType)].getExtraConfigLayoutRes()
        val correctorType = SensorConfiguration.Measure.getCorrectorType(viewType)
        if (warnerLayoutRes != 0 || extraConfigLayoutRes != 0 || correctorType != SensorConfiguration.Measure.CTT_NONE) {
            val clHull = itemView.cl_hull
            val inflater = LayoutInflater.from(context)
            val constraintSet = ConstraintSet()
            constraintSet.clone(clHull)
            if (extraConfigLayoutRes != 0) {
                val vExtraConfig = inflater.inflate(extraConfigLayoutRes, null)
                if (vExtraConfig.id == View.NO_ID) {
                    vExtraConfig.id = View.generateViewId()
                }
                val targetId = vExtraConfig.id
                clHull.addView(vExtraConfig)
                constraintSet.constrainWidth(targetId, 0)
                constraintSet.constrainHeight(targetId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                constraintSet.connect(targetId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(targetId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(targetId, ConstraintSet.TOP, R.id.tv_custom_name_value, ConstraintSet.BOTTOM)
                constraintSet.connect(R.id.tv_value_display_settings, ConstraintSet.TOP, targetId, ConstraintSet.BOTTOM)
            }
            if (warnerLayoutRes != 0) {
                val vWarnerConfig = inflater.inflate(warnerLayoutRes, null)
                if (vWarnerConfig.id == View.NO_ID) {
                    vWarnerConfig.id = View.generateViewId()
                }
                val warnerConfigId = vWarnerConfig.id
                clHull.addView(vWarnerConfig)
                //constraintSet.clear(R.id.tv_warner_type_value, ConstraintSet.BOTTOM)
                constraintSet.constrainWidth(warnerConfigId, 0)
                constraintSet.constrainHeight(warnerConfigId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                constraintSet.connect(warnerConfigId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.connect(warnerConfigId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(warnerConfigId, ConstraintSet.TOP, R.id.tv_warner_type_value, ConstraintSet.BOTTOM)
                constraintSet.connect(R.id.tv_corrector_settings, ConstraintSet.TOP, warnerConfigId, ConstraintSet.BOTTOM)
            }
            if (correctorType != SensorConfiguration.Measure.CTT_NONE) {
                constraintSet.clear(R.id.tv_corrector_type_value, ConstraintSet.BOTTOM)
                when (correctorType) {
                    SensorConfiguration.Measure.CTT_LINEAR_FITTING -> {
                        var previousId = R.id.tv_corrector_type_value
                        repeat(SensorConfiguration.Measure.getExtraPara(viewType) + 1) { i ->
                            val item = inflater.inflate(R.layout.group_lfc_item, null)
                            item.tv_lfc_correct_label.text = context.getString(R.string.correct_value, i + 1)
                            item.tv_lfc_sampling_label.text = context.getString(R.string.sampling_value, i + 1)
                            item.tv_lfc_correct_label.tag = "cl$i"
                            item.tv_lfc_correct_value.tag = "cv$i"
                            item.tv_lfc_sampling_label.tag = "sl$i"
                            item.tv_lfc_sampling_value.tag = "sv$i"
                            if (item.id == View.NO_ID) {
                                item.id = View.generateViewId()
                            }
                            clHull.addView(item)
                            val currentId = item.id
                            constraintSet.constrainWidth(currentId, 0)
                            constraintSet.constrainHeight(currentId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                            constraintSet.connect(currentId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                            constraintSet.connect(currentId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                            constraintSet.connect(currentId, ConstraintSet.TOP, previousId, ConstraintSet.BOTTOM)
                            previousId = currentId
//                            //校准值
//                            var item = inflater.inflate(R.layout.group_corrector_item, null)
//                            if (i == 0) {
//                                val margin = context.resources.getDimensionPixelOffset(R.dimen.divider_width_fix)
//                                val labelParams = item.tv_corrector_label.layoutParams
//                                if (labelParams is LinearLayout.LayoutParams) {
//                                    labelParams.setMargins(margin, margin, 0, margin)
//                                    item.tv_corrector_label.layoutParams = labelParams
//                                }
//                                val valueParams = item.tv_corrector_value.layoutParams
//                                if (valueParams is LinearLayout.LayoutParams) {
//                                    valueParams.setMargins(0, margin, margin, margin)
//                                    item.tv_corrector_value.layoutParams = valueParams
//                                }
//                            }
//                            item.tv_corrector_label.text = context.getString(R.string.correct_value, i + 1)
//                            item.tv_corrector_label.tag = "cl$i"
//                            item.tv_corrector_value.tag = "cv$i"
//                            if (item.id == View.NO_ID) {
//                                item.id = View.generateViewId()
//                            }
//                            clHull.addView(item)
//                            var currentId = item.id
//                            constraintSet.constrainWidth(currentId, 0)
//                            constraintSet.constrainHeight(currentId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                            constraintSet.connect(currentId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//                            constraintSet.connect(currentId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//                            constraintSet.connect(currentId, ConstraintSet.TOP, previousId, ConstraintSet.BOTTOM)
//                            previousId = currentId
//                            //采样值（此处就不提取方法了，反正就俩，复用性不强）
//                            item = inflater.inflate(R.layout.group_corrector_item, null)
//                            item.tv_corrector_label.text = context.getString(R.string.sampling_value, i + 1)
//                            item.tv_corrector_label.tag = "sl$i"
//                            item.tv_corrector_value.tag = "sv$i"
//                            if (item.id == View.NO_ID) {
//                                item.id = View.generateViewId()
//                            }
//                            clHull.addView(item)
//                            currentId = item.id
//                            constraintSet.constrainWidth(currentId, 0)
//                            constraintSet.constrainHeight(currentId, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//                            constraintSet.connect(currentId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//                            constraintSet.connect(currentId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//                            constraintSet.connect(currentId, ConstraintSet.TOP, previousId, ConstraintSet.BOTTOM)
//                            previousId = currentId
                        }
                        constraintSet.connect(previousId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                    }
                }
            }
            constraintSet.applyTo(clHull)
        }
        return itemView
    }

    override fun onBindViewHolder(holder: ViewHolder, item: SensorConfiguration.Measure, position: Int) {
//        holder.findView<TextView>(R.id.tv_measurement_id_value).text = ID.getFormattedDataTypeValue(item.id)
//        holder.findView<TextView>(R.id.tv_default_name_value).text = item.defaultName
//        holder.findView<TextView>(R.id.tv_custom_name_value).text = item.configuration.decorator?.decorateName(null)
        holder.itemView.tv_measurement_id_value.text = ID.getFormattedDataTypeValue(item.id)
        holder.itemView.tv_default_name_value.text = item.defaultName
        val decorator = item.configuration.decorator
        if (decorator is CommonMeasurementDecorator) {
            holder.itemView.tv_custom_name_value.text = decorator.customName
            holder.itemView.tv_custom_unit_value.text = decorator.customUnit
            holder.itemView.tv_decimals_value.text = decorator.getOriginDecimalsLabel()
        } else {
            holder.itemView.tv_custom_name_value.text = null
            holder.itemView.tv_custom_unit_value.text = null
            holder.itemView.tv_decimals_value.text = null
        }
        extraConfigProcessors[item.getConfigType()].bindView(holder, item.configuration)
        val correctorProcessor = correctorProcessors[item.getCorrectorType()]
        holder.itemView.tv_corrector_type_value.setText(correctorProcessor.getTypeStringRes())
        correctorProcessor.bindView(holder, item.configuration.corrector)
        val warnerProcessor = warnerProcessors[item.getWarnerType()]
        //holder.findView<TextView>(R.id.tv_warner_type_value).setText(warnerProcessor.getTypeStringRes())
        holder.itemView.tv_warner_type_value.setText(warnerProcessor.getTypeStringRes())
        warnerProcessor.bindView(holder, item.configuration.warner)
    }

    private open class WarnerProcessor {

        open fun getLayoutRes(): Int {
            return 0
        }

        open fun getTypeStringRes(): Int {
            return R.string.type_none
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

    private open class CorrectorProcessor {

        open fun getTypeStringRes(): Int {
            return R.string.type_none
        }

        fun bindView(holder: ViewHolder, corrector: Corrector?) {
            corrector ?: return
            onBindView(holder, corrector)
        }

        open fun onBindView(holder: ViewHolder, corrector: Corrector) {
        }
    }

    private class LinearFittingCorrectorProcessor : CorrectorProcessor() {

        override fun getTypeStringRes(): Int {
            return R.string.corrector_type
        }

        override fun onBindView(holder: ViewHolder, corrector: Corrector) {
            repeat((corrector as LinearFittingCorrector).groupCount()) { i ->
                holder.itemView.findViewWithTag<TextView>("cv$i").text = corrector.getFormatCorrectedValue(i)
                holder.itemView.findViewWithTag<TextView>("sv$i").text = corrector.getFormatSamplingValue(i)
            }
        }
    }
}