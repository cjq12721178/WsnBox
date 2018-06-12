package com.weisi.tool.wsnbox.dialog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.LogicalSensorInfoAdapter
import kotlinx.android.synthetic.main.fragment_logical_sensor_info.view.*

/**
 * Created by CJQ on 2018/6/5.
 */
class LogicalSensorInfoDialog : SensorInfoDialog<LogicalSensor, LogicalSensorInfoAdapter>() {

//    companion object {
//        @JvmStatic
//        val TAG = "logical_sensor_info"
//    }

    //var realTime = true
    //var sensor: LogicalSensor? = null
    //private lateinit var adapter: LogicalSensorInfoAdapter
    //private val dateOperator = Calendar.getInstance()
    //private var datePickerDialog: DatePickerDialog? = null
    //private lateinit var tvDate: TextView

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (savedInstanceState == null) {
//            setStyle(DialogFragment.STYLE_NO_TITLE, 0)
//        }
//    }

    override fun onCreateAdapter(savedInstanceState: Bundle?): LogicalSensorInfoAdapter {
        return LogicalSensorInfoAdapter(sensor, realTime)
    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_logical_sensor_info, container, false)
        view.tv_sensor_info_label.text = getString(R.string.sensor_info_title, sensor.name)
        view.tv_sensor_id.text = getString(R.string.sensor_info_id, sensor.id.toString())
        view.tv_sensor_state.setText(if (sensor.state == Sensor.ON_LINE) {
            R.string.sensor_info_state_on
        } else {
            R.string.sensor_info_state_off
        })
        return view
    }

    override fun onBindRecyclerView(view: View): RecyclerView {
        val rv = view.findViewById<View>(R.id.rv_logical_sensor_info) as RecyclerView
        rv.layoutManager = LinearLayoutManager(context)
        return rv
    }

    override fun notifyLogicalSensorRealTimeValueChanged(logicalSensor: LogicalSensor, logicalPosition: Int) {
        notifySensorValueChanged(logicalSensor, logicalPosition)
    }

    override fun notifyLogicalSensorHistoryValueChanged(logicalSensor: LogicalSensor, logicalPosition: Int) {
        notifySensorValueChanged(logicalSensor, logicalPosition)
    }

    override fun notifyPhysicalSensorHistoryValueChanged(physicalSensor: PhysicalSensor, logicalPosition: Int) {
        if (physicalSensor.rawAddress == sensor.id.address) {
            val value = physicalSensor.getValueByContainerAddMethodReturnValue(physicalSensor.historyValueContainer, logicalPosition) ?: return
            val position = sensor.historyValueContainer.findValuePosition(logicalPosition, value.timestamp)
            if (position >= 0) {
                adapter.notifySensorValueUpdate(- position - 1, rvSensorInfo)
            }
        }
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val logicalSensor = sensor!!
//        if (savedInstanceState == null) {
//            getBaseActivity().dataPrepareService.dataTransferStation.payAttentionToSensor(logicalSensor)
//        }
//        adapter = LogicalSensorInfoAdapter(logicalSensor, realTime)
//        val view = inflater.inflate(R.layout.fragment_logical_sensor_info, container, false)
//        view.tv_sensor_info_label.text = getString(R.string.sensor_info_title, logicalSensor.name)
//        view.tv_sensor_id.text = getString(R.string.sensor_info_id, logicalSensor.id.toString())
//        view.tv_sensor_state.setText(if (logicalSensor.state == Sensor.ON_LINE) {
//            R.string.sensor_info_state_on
//        } else {
//            R.string.sensor_info_state_off
//        })
//        tvDate = view.tv_date
//
//        //设置日期标签及选择面板（历史）
//        if (realTime) {
//            setDateLabel(System.currentTimeMillis())
//        } else {
//            chooseDate(0)
//            val vsDateChooser = view.findViewById<View>(R.id.vs_date_chooser) as ViewStub
//            val vDateChooser = vsDateChooser.inflate()
//            vDateChooser.findViewById<View>(R.id.btn_today).setOnClickListener(this)
//            vDateChooser.findViewById<View>(R.id.btn_previous_day).setOnClickListener(this)
//            vDateChooser.findViewById<View>(R.id.btn_next_day).setOnClickListener(this)
//            vDateChooser.findViewById<View>(R.id.btn_custom_day).setOnClickListener(this)
//        }
//
//        val rvSensorInfo = view.findViewById<View>(R.id.rv_logical_sensor_info) as RecyclerView
//        rvSensorInfo.layoutManager = LinearLayoutManager(context)
//        rvSensorInfo.adapter = adapter
//        return view
//    }

//    private fun setDateLabel(date: Long) {
//        tvDate.text = getString(R.string.sensor_info_date, date)
//    }

//    private fun chooseDate(date: Long) {
//        var isDateChanged = true
//        if (date == 0L) {
//            if (adapter.getIntraday() == 0L) {
//                adapter.setIntraday(System.currentTimeMillis())
//            } else {
//                isDateChanged = false
//            }
//        } else if (date > 0) {
//            if (adapter.isIntraday(date)) {
//                isDateChanged = false
//            } else {
//                val previousSize = adapter.itemCount
//                adapter.setIntraday(date)
//                adapter.notifyDataSetChanged(previousSize)
//            }
//        } else {
//            throw IllegalArgumentException("intraday start time may not be less than 0")
//        }
//        if (isDateChanged) {
//            val dateTime = adapter.getIntraday()
//            setDateLabel(dateTime)
//            if (adapter.itemCount <= 1) {
//                getBaseActivity()
//                        .dataPrepareService
//                        .sensorHistoryDataAccessor
//                        .importSensorHistoryValue(sensor!!.id.id,
//                                dateTime, getNextDayTime(dateTime), this)
//            }
//        } else if (TextUtils.isEmpty(tvDate.text)) {
//            setDateLabel(adapter.getIntraday())
//        }
//    }

//    private fun getPreviousDayTime(sourceDate: Long): Long {
//        dateOperator.timeInMillis = sourceDate
//        dateOperator.add(Calendar.DAY_OF_MONTH, -1)
//        return dateOperator.timeInMillis
//    }
//
//    private fun getNextDayTime(sourceDate: Long): Long {
//        dateOperator.timeInMillis = sourceDate
//        dateOperator.add(Calendar.DAY_OF_MONTH, 1)
//        return dateOperator.timeInMillis
//    }
//
//    override fun onMissionFinished(result: Boolean) {
//        if (!result) {
//            val dialog = ConfirmDialog()
//            dialog.setTitle(R.string.import_sensor_history_value_failed)
//            dialog.setDrawCancelButton(false)
//            dialog.show(childFragmentManager,
//                    "import_sensor_history_value_failed")
//        }
//    }

//    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.btn_today -> chooseDate(System.currentTimeMillis())
//            R.id.btn_previous_day -> chooseDate(getPreviousDayTime(adapter.getIntraday()))
//            R.id.btn_next_day -> chooseDate(getNextDayTime(adapter.getIntraday()))
//            R.id.btn_custom_day -> {
//                if (datePickerDialog == null) {
//                    dateOperator.timeInMillis = adapter.getIntraday()
//                    datePickerDialog = DatePickerDialog(context!!,
//                            this,
//                            dateOperator.get(Calendar.YEAR),
//                            dateOperator.get(Calendar.MONTH),
//                            dateOperator.get(Calendar.DAY_OF_MONTH))
//                }
//                datePickerDialog?.show()
//            }
//        }
//    }

//    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
//        dateOperator.set(Calendar.YEAR, year)
//        dateOperator.set(Calendar.MONTH, month)
//        dateOperator.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//        chooseDate(dateOperator.timeInMillis)
//    }

//    override fun onDestroy() {
//        adapter.detachSensorValueContainer()
//        sensor = null
//        super.onDestroy()
//    }
//
//    override fun onDismiss(dialog: DialogInterface?) {
//        getBaseActivity().dataPrepareService.dataTransferStation.payNoAttentionToSensor(sensor!!)
//        super.onDismiss(dialog)
//    }
//
//    fun show(transaction: FragmentTransaction, logicalSensor: LogicalSensor, isRealTime: Boolean): Int {
//        sensor = logicalSensor
//        realTime = isRealTime
//        return super.show(transaction, TAG)
//    }
//
//    fun show(manager: FragmentManager, logicalSensor: LogicalSensor, isRealTime: Boolean) {
//        sensor = logicalSensor
//        realTime = isRealTime
//        super.show(manager, TAG)
//    }
//
//    override fun show(transaction: FragmentTransaction, tag: String): Int {
//        throw UnsupportedOperationException("use show(FragmentTransaction transaction, PhysicalSensor sensor) for instead")
//    }
//
//    override fun show(manager: FragmentManager, tag: String) {
//        throw UnsupportedOperationException("use show(FragmentManager manager, PhysicalSensor sensor) for instead")
//    }
//
//    private fun canNotifyValueChanged(logicalSensor: LogicalSensor): Boolean {
//        return sensor === logicalSensor && isDialogShow()
//    }
//
//    private fun isDialogShow(): Boolean {
//        return dialog != null && dialog.isShowing
//    }

//    fun notifySensorValueChanged(logicalSensor: LogicalSensor, valuePosition: Int) {
//        if (!canNotifyValueChanged(logicalSensor)) {
//            return
//        }
//        adapter.notifySensorValueUpdate(valuePosition)
//    }
//
//    fun notifyWarnProcessorLoaded() {
//        if (isDialogShow()) {
//            adapter.notifyItemRangeChanged(0, adapter.itemCount)
//        }
//    }
}