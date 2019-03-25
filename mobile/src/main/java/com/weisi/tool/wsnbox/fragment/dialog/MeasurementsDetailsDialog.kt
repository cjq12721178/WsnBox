package com.weisi.tool.wsnbox.fragment.dialog

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.DatePicker
import com.cjq.lib.weisi.iot.*
import com.google.android.flexbox.FlexboxLayout
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.info.MeasurementCurvePage
import com.weisi.tool.wsnbox.adapter.info.MeasurementCurvePageAdapter
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.processor.transfer.DataTransferStation
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.NullHelper
import kotlinx.android.synthetic.main.dialog_sensor_details.*
import kotlinx.android.synthetic.main.dialog_sensor_details.view.*
import java.util.*
import java.util.concurrent.TimeUnit

abstract class MeasurementsDetailsDialog : BaseServiceDialog(), DataTransferStation.Detector, CompoundButton.OnCheckedChangeListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, MeasurementCurvePage.OnMeasurementValueSelectedListener {

    override var enableDetectPhysicalSensorNetIn = false
    override var enableDetectLogicalSensorNetIn = false
    override var enableDetectMeasurementHistoryValueUpdate = false
    override var enableDetectMeasurementDynamicValueUpdate = false
    override var enableDetectSensorInfoHistoryValueUpdate = false
    override var enableDetectSensorInfoDynamicValueUpdate = false

    private val ARGUMENT_KEY_MEASUREMENTS = "mms"
    private val ARGUMENT_KEY_SELECTED_MEASUREMENT_INDEXES = "selected_mm_indexes"
    private val ARGUMENT_KEY_CURRENT_DATE = "curr_date"
    private var currentDate = -1L
    protected var measurements: List<Measurement<*, *>> by NullHelper.readonlyNotNull()
    private val measurementCurvePageAdapter = MeasurementCurvePageAdapter()

    init {
        measurementCurvePageAdapter.onMeasurementValueSelectedListener = this
    }

    protected fun initMeasurements(physicalSensor: PhysicalSensor) {
        measurements = Array<Measurement<*, *>>(physicalSensor.displayMeasurementSize + 1) {
            if (it < physicalSensor.displayMeasurementSize) {
                physicalSensor.getDisplayMeasurementByPosition(it)
            } else {
                physicalSensor.info
            }
        }.toList()
        //setMeasurementUnitValueContainer()
    }

    protected fun initMeasurements(logicalSensor: LogicalSensor) {
        measurements = listOf(logicalSensor.practicalMeasurement, logicalSensor.info)
        //setMeasurementUnitValueContainer()
    }

    protected fun initMeasurements(device: Device) {
        measurements = device.nodes.map { it.measurement }
        //setMeasurementUnitValueContainer()
    }

    private fun initMeasurements(measurementIds: LongArray) {
        measurements = measurementIds.map { SensorManager.getMeasurement(it) }
        //setMeasurementUnitValueContainer()
    }

    private fun setMeasurementUnitValueContainer() {
        if (isRealTime()) {
            measurements.forEach { it.setUniteValueContainer() }
        } else {
            val startTime = getStartTime(currentDate)
            val endTime = getEndTime(startTime)
            measurements.forEach { it.setUniteValueContainer(startTime, endTime) }
        }
    }

    private fun clearMeasurementUnitValueContainer() {
        measurements.forEach { it.clearUniteValueContainer() }
    }

    protected open fun getObjectLabel() = ""

    protected open fun getSensorAddressLabel() = ""

    protected open fun getSensorStateLabel() = ""

    protected fun isRealTime() = currentDate == 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NullHelper.doNullOrNot(savedInstanceState, {
            setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light)
            chooseDate(0L)
        }, {
            initMeasurements(it.getLongArray(ARGUMENT_KEY_MEASUREMENTS))
            chooseDate(it.getLong(ARGUMENT_KEY_CURRENT_DATE))
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sensor_details, null) as ConstraintLayout
        //初始化基本信息
        view.tv_dialog_title.text = getString(R.string.sensor_info_title, getObjectLabel())
        if (getSensorAddressLabel().isEmpty() && getSensorStateLabel().isEmpty()) {
            view.tv_sensor_address.visibility = View.GONE
            view.tv_sensor_state.visibility = View.GONE
        } else {
            view.tv_sensor_address.text = getString(R.string.sensor_info_address, getSensorAddressLabel())
            //view.tv_sensor_state.text = getSensorStateLabel()
            setStateLabel(view)
        }
        setDateLabel(view)

        //设置传感器数据表格
        val selectedMeasurementIndexes = savedInstanceState?.getIntArray(ARGUMENT_KEY_SELECTED_MEASUREMENT_INDEXES)
        val vTable = initDataTableView(selectedMeasurementIndexes)
        vTable.setBackgroundResource(R.color.bg_shadow_header)
        val constraintSet = ConstraintSet()
        constraintSet.clone(view)
        if (vTable.id == View.NO_ID) {
            vTable.id = View.generateViewId()
        }
        val vTableId = vTable.id
        view.addView(vTable)
        constraintSet.constrainWidth(vTableId, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(vTableId, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.connect(vTableId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(vTableId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraintSet.createVerticalChain(R.id.fbl_info_filters, ConstraintSet.BOTTOM, R.id.btn_real_time, ConstraintSet.TOP, intArrayOf(R.id.vp_sensor_data_curve, vTableId), floatArrayOf(1f, 1f), ConstraintSet.CHAIN_SPREAD)
        constraintSet.applyTo(view)

        //初始化信息筛选界面
        initInfoFilters(view.fbl_info_filters, selectedMeasurementIndexes)
        view.chk_filtrate_info.setOnCheckedChangeListener { _, isChecked ->
            view.fbl_info_filters.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        //绑定切换数据来源事件
        view.btn_real_time.setOnClickListener(this)
        view.btn_today.setOnClickListener(this)
        view.btn_next_day.setOnClickListener(this)
        view.btn_previous_day.setOnClickListener(this)
        view.btn_custom_day.setOnClickListener(this)

        //设置传感器数据曲线适配器
        view.vp_sensor_data_curve.adapter = measurementCurvePageAdapter

        //若无实时数据，尝试切换至最近拥有历史数据的那一天
        if (savedInstanceState === null && !hasRealTimeValue()) {
            queryLatestHistroyDataTimestamp()
        }
        //Log.d(Tag.LOG_TAG_D_TEST, "onCreateView")
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        clearMeasurementUnitValueContainer()
    }

    private fun initInfoFilters(fblFilters: FlexboxLayout, selectedMeasurementIndexes: IntArray?) {
        val c = context ?: return
        val textSize = resources.getDimensionPixelSize(R.dimen.size_text_dialog)
        val paddingSize = resources.getDimensionPixelSize(R.dimen.padding_micro)
        val marginSize = resources.getDimensionPixelSize(R.dimen.margin_micro)
        val textColor = ContextCompat.getColorStateList(c, R.color.qbox_selector_text_color_check_box)
        val backgroundRes = R.drawable.qbox_selector_background_check_box
        val buttonDrawable = ContextCompat.getDrawable(c, android.R.color.transparent)
        measurements.forEachIndexed { index, _ ->
            fblFilters.addView(createInfoFilter(textSize,
                    textColor,
                    paddingSize,
                    marginSize,
                    backgroundRes,
                    buttonDrawable,
                    index,
                    selectedMeasurementIndexes))
        }
    }

    private fun createInfoFilter(textSize: Int,
                                 textColor: ColorStateList?,
                                 paddingSize: Int,
                                 marginSize: Int,
                                 backgroundRes: Int,
                                 buttonDrawable: Drawable?,
                                 measurementIndex: Int,
                                 selectedMeasurementsId: IntArray?): CheckBox {
        val measurement = measurements[measurementIndex]
        val chk = CheckBox(context)
        chk.text = measurement.getValueLabel()
        chk.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        chk.setPadding(paddingSize, paddingSize, paddingSize, paddingSize)
        val params = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(marginSize, marginSize, marginSize, marginSize)
        chk.layoutParams = params
        chk.setBackgroundResource(backgroundRes)
        chk.buttonDrawable = buttonDrawable
        chk.setTextColor(textColor)
        chk.tag = measurementIndex
        chk.setOnCheckedChangeListener(this)
        chk.isChecked = selectedMeasurementsId?.contains(measurementIndex) != false
        return chk
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val measurementIndex = (buttonView?.tag ?: return) as? Int ?: return
        val measurement = measurements[measurementIndex]
        if (isChecked) {
            measurementCurvePageAdapter.addPage(measurement, TimeUnit.SECONDS.toMillis(getBaseActivity()?.settings?.sensorDataGatherCycle ?: 60L) / 2)
        } else {
            measurementCurvePageAdapter.removePage(measurement)
        }
        onEnableMeasurementDisplay(measurement, isChecked)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_real_time -> chooseDate(0)
            R.id.btn_today -> chooseDate(System.currentTimeMillis())
            R.id.btn_previous_day -> chooseDate(getPreviousDayTime())
            R.id.btn_next_day -> chooseDate(getNextDayTime())
            R.id.btn_custom_day -> {
                val date = Calendar.getInstance()
                DatePickerDialog(context,
                        this,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH))
                        .show()
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = Calendar.getInstance()
        date.set(Calendar.YEAR, year)
        date.set(Calendar.MONTH, month)
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        chooseDate(date.timeInMillis)
    }

    private fun getCurrentDayTime(): Long {
        return if (currentDate == 0L) {
            System.currentTimeMillis()
        } else {
            currentDate
        }
    }

    private fun getPreviousDayTime(): Long {
        val date = Calendar.getInstance()
        date.timeInMillis = getCurrentDayTime()
        date.add(Calendar.DAY_OF_MONTH, -1)
        return date.timeInMillis
    }

    private fun getNextDayTime(): Long {
        val date = Calendar.getInstance()
        date.timeInMillis = getCurrentDayTime()
        date.add(Calendar.DAY_OF_MONTH, 1)
        return date.timeInMillis
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLongArray(ARGUMENT_KEY_MEASUREMENTS, measurements.map {
            it.getId().id
        }.toLongArray())
        outState.putIntArray(ARGUMENT_KEY_SELECTED_MEASUREMENT_INDEXES, getSelectedMeasurementIndexes())
        outState.putLong(ARGUMENT_KEY_CURRENT_DATE, currentDate)
        super.onSaveInstanceState(outState)
    }

    protected fun getSelectedMeasurementIndexes(): IntArray {
        val result = mutableListOf<Int>()
        for (i in 0 until fbl_info_filters.childCount) {
            fbl_info_filters.getChildAt(i).let {
                val tag = it.tag
                if (it is CheckBox && it.isChecked && tag is Int) {
                    result.add(tag)
                }
            }
        }
        return result.toIntArray()
    }

    protected fun chooseDate(date: Long) {
        if (currentDate != date) {
            if (date == 0L) {
                currentDate = date
                setRealTimeMode()
            } else {
                val startTime = getStartTime(date)
                if (startTime != currentDate) {
                    currentDate = startTime
                    setHistoryMode(startTime, getEndTime(startTime))
                }
            }
        }
    }

    private fun getStartTime(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndTime(startTime: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }

    private fun setRealTimeMode() {
        enableDetectSensorInfoHistoryValueUpdate = false
        enableDetectMeasurementHistoryValueUpdate = false
        onStopHistoryMode()
        setDateLabel()
        measurements.forEach { it.setUniteValueContainer() }
        measurementCurvePageAdapter.notifyDataTimeChanged()
        onStartRealTimeMode()
        enableDetectSensorInfoDynamicValueUpdate = true
        enableDetectMeasurementDynamicValueUpdate = true
    }

    private fun setHistoryMode(startTime: Long, endTime: Long) {
        enableDetectSensorInfoDynamicValueUpdate = false
        enableDetectMeasurementDynamicValueUpdate = false
        onStopRealTimeMode()
        setDateLabel()
        measurements.forEach { it.setUniteValueContainer(startTime, endTime) }
        //Log.d(Tag.LOG_TAG_D_TEST, "before notifyDataTimeChanged")
        measurementCurvePageAdapter.notifyDataTimeChanged()
        //Log.d(Tag.LOG_TAG_D_TEST, "before onStartHistoryMode")
        onStartHistoryMode(startTime, endTime)
        enableDetectSensorInfoHistoryValueUpdate = true
        enableDetectMeasurementHistoryValueUpdate = true
    }

    protected fun setStateLabel(view: View? = null) {
        (view?.tv_sensor_state ?: tv_sensor_state)?.text = getSensorStateLabel()
    }

    private fun setDateLabel(view: View? = null) {
        (view?.tv_date ?: tv_date)?.text =
                getString(if (isRealTime()) {
                    R.string.sensor_info_date_real_time
                } else {
                    R.string.sensor_info_date
                }, getCurrentDayTime())
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        service.dataTransferStation.register(this)
    }

    override fun onServiceConnectionStart(service: DataPrepareService) {
        if (isRealTime()) {
            //enableDetectPhysicalSensorValueUpdate = true
            enableDetectMeasurementDynamicValueUpdate = true
        } else {
            //enableDetectSensorInfoHistoryValueReceive = true
            //enableDetectMeasurementHistoryValueReceive = true
            enableDetectMeasurementHistoryValueUpdate = true
        }
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        if (isRealTime()) {
            //enableDetectPhysicalSensorValueUpdate = true
            enableDetectMeasurementDynamicValueUpdate = false
        } else {
            //enableDetectSensorInfoHistoryValueReceive = true
            //enableDetectMeasurementHistoryValueReceive = true
            enableDetectMeasurementHistoryValueUpdate = false
        }
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
        service.dataTransferStation.unregister(this)
    }

    override fun onPhysicalSensorNetIn(sensor: PhysicalSensor) {
    }

    override fun onLogicalSensorNetIn(sensor: LogicalSensor) {
    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
        measurementCurvePageAdapter.updateValue(info, valuePosition)
    }

    override fun onSensorInfoDynamicValueUpdate(info: Sensor.Info, valuePosition: Int) {
        measurementCurvePageAdapter.updateValue(info, valuePosition)
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        measurementCurvePageAdapter.updateValue(measurement, valuePosition)
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        measurementCurvePageAdapter.updateValue(measurement, valuePosition)
    }

    protected fun onMeasurementValueSelectedInTable(measurement: Measurement<*, *>, targetTimestamp: Long) {
        vp_sensor_data_curve?.let {
            measurementCurvePageAdapter.notifyDataHighlight(it, measurement, targetTimestamp)
        }
    }

    override fun onMeasurementValueSelectedInCurve(measurement: Measurement<*, *>, timestamp: Long) {

    }

    protected open fun onStopRealTimeMode() {
    }

    protected open fun onStopHistoryMode() {
    }

    protected abstract fun hasRealTimeValue(): Boolean
    protected abstract fun onStartRealTimeMode()
    protected abstract fun onStartHistoryMode(startTime: Long, endTime: Long)
    protected abstract fun initDataTableView(selectedMeasurementIndexes: IntArray?): View
    protected abstract fun onEnableMeasurementDisplay(measurement: Measurement<*, *>, enabled: Boolean)
    protected abstract fun queryLatestHistroyDataTimestamp()
}