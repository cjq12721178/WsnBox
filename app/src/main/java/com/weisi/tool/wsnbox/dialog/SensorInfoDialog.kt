package com.weisi.tool.wsnbox.dialog

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.DatePicker
import android.widget.TextView
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.SensorInfoAdapter
import com.weisi.tool.wsnbox.processor.accessor.SensorHistoryDataAccessor
import java.util.*

/**
 * Created by CJQ on 2018/6/7.
 */
open class SensorInfoDialog<S : Sensor<*, *>, A : SensorInfoAdapter<*, S, *>> : BaseDialog(), SensorHistoryDataAccessor.OnMissionFinishedListener, View.OnClickListener, DatePickerDialog.OnDateSetListener {

    companion object {
        @JvmStatic
        val TAG = "sensor_info"
        @JvmStatic
        private val ARGUMENT_KEY_REAL_TIME = "real_time"
        //@JvmStatic
        //private val ARGUMENT_KEY_SELECTED_SENSOR_INDEX = "selected_index"
        @JvmStatic
        private val ARGUMENT_KEY_SELECTED_SENSOR_ID = "selected_sensor_id"
    }

    protected var realTime = true
    lateinit var sensor: S
    protected lateinit var adapter: A
    protected lateinit var rvSensorInfo: RecyclerView
    private lateinit var tvDate: TextView
    private val dateOperator = Calendar.getInstance()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState == null) {
            getBaseActivity()?.dataPrepareService?.dataTransferStation?.payAttentionToSensor(sensor)
        } else {
            realTime = savedInstanceState.getBoolean(ARGUMENT_KEY_REAL_TIME)
            sensor = SensorManager.getSensor(savedInstanceState.getLong(ARGUMENT_KEY_SELECTED_SENSOR_ID), false) as S
        }

        adapter = onCreateAdapter(savedInstanceState)
        val view = onInitView(inflater, container, savedInstanceState)
        rvSensorInfo = onBindRecyclerView(view)
        tvDate = view.findViewById(R.id.tv_date)

        //设置日期标签及选择面板（历史）
        if (realTime) {
            setDateLabel(System.currentTimeMillis())
        } else {
            chooseDate(0)
            val vsDateChooser = view.findViewById<View>(R.id.vs_date_chooser) as ViewStub
            val vDateChooser = vsDateChooser.inflate()
            vDateChooser.findViewById<View>(R.id.btn_today).setOnClickListener(this)
            vDateChooser.findViewById<View>(R.id.btn_previous_day).setOnClickListener(this)
            vDateChooser.findViewById<View>(R.id.btn_next_day).setOnClickListener(this)
            vDateChooser.findViewById<View>(R.id.btn_custom_day).setOnClickListener(this)
        }

        rvSensorInfo.adapter = adapter
        return view
    }

    protected open fun onCreateAdapter(savedInstanceState: Bundle?): A {
        throw NullPointerException("onCreateAdapter may be implement")
    }

    protected open fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        throw NullPointerException("onInitView may be implement")
    }

    protected open fun onBindRecyclerView(view: View): RecyclerView {
        throw NullPointerException("onBindRecyclerView may be implement")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ARGUMENT_KEY_REAL_TIME, realTime)
        //outState.putInt(ARGUMENT_KEY_SELECTED_SENSOR_INDEX, adapter.getSelectedIndex())
        outState.putLong(ARGUMENT_KEY_SELECTED_SENSOR_ID, sensor.getId().id)
    }

    private fun setDateLabel(date: Long) {
        tvDate.text = getString(R.string.sensor_info_date, date)
    }

    private fun chooseDate(date: Long) {
        var isDateChanged = true
        if (date == 0L) {
            if (adapter.getIntraday() == 0L) {
                adapter.setIntraday(System.currentTimeMillis())
            } else {
                isDateChanged = false
            }
        } else if (date > 0) {
            if (adapter.isIntraday(date)) {
                isDateChanged = false
            } else {
                val previousSize = adapter.getItemCount()
                adapter.setIntraday(date)
                adapter.notifyDataSetChanged(previousSize)
            }
        } else {
            throw IllegalArgumentException("intraday start time may not be less than 0")
        }
        if (isDateChanged) {
            val dateTime = adapter.getIntraday()
            setDateLabel(dateTime)
            if (adapter.getItemCount() <= 1) {
                getBaseActivity()
                        ?.dataPrepareService
                        ?.sensorHistoryDataAccessor
                        ?.importSensorHistoryValue(sensor.getId().id,
                                dateTime, getNextDayTime(dateTime), this)
            }
        } else if (TextUtils.isEmpty(tvDate.text)) {
            setDateLabel(adapter.getIntraday())
        }
    }

    private fun getPreviousDayTime(sourceDate: Long): Long {
        dateOperator.timeInMillis = sourceDate
        dateOperator.add(Calendar.DAY_OF_MONTH, -1)
        return dateOperator.timeInMillis
    }

    private fun getNextDayTime(sourceDate: Long): Long {
        dateOperator.timeInMillis = sourceDate
        dateOperator.add(Calendar.DAY_OF_MONTH, 1)
        return dateOperator.timeInMillis
    }

    override fun onMissionFinished(result: Boolean) {
        if (!result) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.import_sensor_history_value_failed)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager,
                    "import_sensor_history_value_failed")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_today -> chooseDate(System.currentTimeMillis())
            R.id.btn_previous_day -> chooseDate(getPreviousDayTime(adapter.getIntraday()))
            R.id.btn_next_day -> chooseDate(getNextDayTime(adapter.getIntraday()))
            R.id.btn_custom_day -> {
                if (datePickerDialog == null) {
                    dateOperator.timeInMillis = adapter.getIntraday()
                    datePickerDialog = DatePickerDialog(context!!,
                            this,
                            dateOperator.get(Calendar.YEAR),
                            dateOperator.get(Calendar.MONTH),
                            dateOperator.get(Calendar.DAY_OF_MONTH))
                }
                datePickerDialog?.show()
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dateOperator.set(Calendar.YEAR, year)
        dateOperator.set(Calendar.MONTH, month)
        dateOperator.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        chooseDate(dateOperator.timeInMillis)
    }

    override fun onDestroy() {
        getBaseActivity()?.dataPrepareService?.dataTransferStation?.payNoAttentionToSensor(sensor)
        adapter.detachSensorValueContainer()
        super.onDestroy()
    }

    fun show(transaction: FragmentTransaction, sensor: S, isRealTime: Boolean): Int {
        this.sensor = sensor
        realTime = isRealTime
        return super.show(transaction, TAG)
    }

    fun show(manager: FragmentManager, sensor: S, isRealTime: Boolean) {
        this.sensor = sensor
        realTime = isRealTime
        super.show(manager, TAG)
    }

    override fun show(transaction: FragmentTransaction, tag: String): Int {
        throw UnsupportedOperationException("use show(FragmentTransaction transaction, PhysicalSensor sensor) for instead")
    }

    override fun show(manager: FragmentManager, tag: String) {
        throw UnsupportedOperationException("use show(FragmentManager manager, PhysicalSensor sensor) for instead")
    }

    private fun canNotifyValueChanged(s: S): Boolean {
        return sensor === s && isDialogShow()
    }

    private fun isDialogShow(): Boolean {
        return dialog != null && dialog.isShowing
    }

    protected fun notifySensorValueChanged(s: S, valuePosition: Int) {
        if (!canNotifyValueChanged(s)) {
            return
        }
        adapter.notifySensorValueUpdate(valuePosition, rvSensorInfo)
    }

    open fun notifyLogicalSensorRealTimeValueChanged(logicalSensor: LogicalSensor, logicalPosition: Int) {
    }

    open fun notifyLogicalSensorHistoryValueChanged(logicalSensor: LogicalSensor, logicalPosition: Int) {
    }

    open fun notifyPhysicalSensorRealTimeValueChanged(physicalSensor: PhysicalSensor, logicalPosition: Int) {
    }

    open fun notifyPhysicalSensorHistoryValueChanged(physicalSensor: PhysicalSensor, logicalPosition: Int) {
    }

    fun notifyWarnProcessorLoaded() {
        if (isDialogShow()) {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount())
        }
    }
}