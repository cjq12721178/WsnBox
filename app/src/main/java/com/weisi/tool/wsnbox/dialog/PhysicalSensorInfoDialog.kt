package com.weisi.tool.wsnbox.dialog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.PhysicalSensorInfoAdapter
import kotlinx.android.synthetic.main.fragment_physical_sensor_info.view.*
import kotlinx.android.synthetic.main.li_physical_sensor_info.view.*


/**
 * Created by CJQ on 2017/11/3.
 */

class PhysicalSensorInfoDialog : SensorInfoDialog<PhysicalSensor, PhysicalSensorInfoAdapter>(),
        PhysicalSensorInfoAdapter.OnDisplayStateChangeListener,
        View.OnTouchListener {

    private val ARGUMENT_KEY_START_MEASUREMENT_INDEX = "smi"
    //private var mRealTime: Boolean = false
    //var sensor: PhysicalSensor? = null
    //private var mPhysicalSensorInfoAdapter: PhysicalSensorInfoAdapter? = null
    private var mIvInfoOrientation: ImageView? = null
    private val mTvValueLabels = arrayOfNulls<TextView>(PhysicalSensorInfoAdapter.MAX_DISPLAY_COUNT)
    private var mLastTouchX: Float = 0.toFloat()
    private var mLastTouchY: Float = 0.toFloat()
    //private var mTvDate: TextView? = null
    //private val mDateOperator = Calendar.getInstance()
    //private var mDatePickerDialog: DatePickerDialog? = null

//    private val isDialogShow: Boolean
//        get() = (mPhysicalSensorInfoAdapter != null
//                && dialog != null
//                && dialog.isShowing)

//    fun setRealTime(realTime: Boolean) {
//        mRealTime = realTime
//    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (savedInstanceState == null) {
//            setStyle(DialogFragment.STYLE_NO_TITLE, 0)
//        }
//    }

    override fun onCreateAdapter(savedInstanceState: Bundle?): PhysicalSensorInfoAdapter {
        return if (savedInstanceState == null) {
            PhysicalSensorInfoAdapter(context, sensor, realTime, 0)
        } else {
            PhysicalSensorInfoAdapter(context, sensor, realTime, savedInstanceState.getInt(ARGUMENT_KEY_START_MEASUREMENT_INDEX))
        }
    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_physical_sensor_info, container, false)
        val tvTitle = view.findViewById<TextView>(R.id.tv_sensor_info_label)
        tvTitle.text = getString(R.string.sensor_info_title, sensor.name)
        val tvAddress = view.findViewById<TextView>(R.id.tv_sensor_info_address)
        tvAddress.text = getString(R.string.sensor_info_address, sensor.formatAddress)
        val tvState = view.findViewById<TextView>(R.id.tv_sensor_state)
        tvState.setText(if (sensor.state == Sensor.ON_LINE)
            R.string.sensor_info_state_on
        else
            R.string.sensor_info_state_off)

        //设置RecyclerView header
        val measurements = sensor.measurementCollections
        mTvValueLabels[0] = view.tv_measurement1
        mTvValueLabels[1] = view.tv_measurement2
        if (adapter.scheduledDisplayCount >= PhysicalSensorInfoAdapter.MAX_DISPLAY_COUNT) {
            val vsMeasurement = view.findViewById<View>(R.id.vs_measurement) as ViewStub
            mTvValueLabels[2] = vsMeasurement.inflate() as TextView
        }
        setValueLabels(measurements)

        return view
    }

    override fun onBindRecyclerView(view: View): RecyclerView {
        val rv = view.rv_logical_sensor_info
        //设置info orientation
        val displayState = adapter.infoDisplayState
        if (displayState != PhysicalSensorInfoAdapter.HAS_NO_EXCESS_DISPLAY_ITEM) {
            val vsInfoOrientation = view.vs_info_orientation
            mIvInfoOrientation = vsInfoOrientation.inflate() as ImageView
            setInfoOrientation(displayState)
            rv.setOnTouchListener(this)
            adapter.setOnDisplayStateChangeListener(this)
        }
        rv.layoutManager = LinearLayoutManager(context)
        return rv
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        if (savedInstanceState == null) {
//            mPhysicalSensorInfoAdapter = PhysicalSensorInfoAdapter(context, sensor, mRealTime, 0)
//        } else {
//            mPhysicalSensorInfoAdapter = PhysicalSensorInfoAdapter(context, sensor, mRealTime, savedInstanceState.getInt(ARGUMENT_KEY_START_MEASUREMENT_INDEX))
//        }
//        val view = inflater.inflate(R.layout.fragment_physical_sensor_info, container, false)
//        val tvTitle = view.findViewById<TextView>(R.id.tv_sensor_info_label)
//        tvTitle.text = getString(R.string.sensor_info_title, sensor!!.name)
//        val tvAddress = view.findViewById<TextView>(R.id.tv_sensor_info_address)
//        tvAddress.text = getString(R.string.sensor_info_address, sensor!!.formatAddress)
//        val tvState = view.findViewById<TextView>(R.id.tv_sensor_state)
//        tvState.setText(if (sensor!!.state == Sensor.ON_LINE)
//            R.string.sensor_info_state_on
//        else
//            R.string.sensor_info_state_off)
//        mTvDate = view.findViewById(R.id.tv_date)
//
//        //设置日期标签及选择面板（历史）
//        if (mRealTime) {
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
//        //设置RecyclerView header
//        val measurements = sensor!!.measurementCollections
//        mTvValueLabels[0] = view.findViewById<View>(R.id.tv_measurement1) as TextView
//        mTvValueLabels[1] = view.findViewById<View>(R.id.tv_measurement2) as TextView
//        if (mPhysicalSensorInfoAdapter!!.scheduledDisplayCount >= PhysicalSensorInfoAdapter.MAX_DISPLAY_COUNT) {
//            val vsMeasurement = view.findViewById<View>(R.id.vs_measurement) as ViewStub
//            mTvValueLabels[2] = vsMeasurement.inflate() as TextView
//        }
//        setValueLabels(measurements)
//
//        val rvSensorInfo = view.findViewById<View>(R.id.rv_logical_sensor_info) as RecyclerView
//        //设置info orientation
//        val displayState = mPhysicalSensorInfoAdapter!!.infoDisplayState
//        if (displayState != PhysicalSensorInfoAdapter.HAS_NO_EXCESS_DISPLAY_ITEM) {
//            val vsInfoOrientation = view.findViewById<View>(R.id.vs_info_orientation) as ViewStub
//            mIvInfoOrientation = vsInfoOrientation.inflate() as ImageView
//            setInfoOrientation(displayState)
//            rvSensorInfo.setOnTouchListener(this)
//            mPhysicalSensorInfoAdapter!!.setOnDisplayStateChangeListener(this)
//        }
//        rvSensorInfo.layoutManager = LinearLayoutManager(context)
//        rvSensorInfo.adapter = mPhysicalSensorInfoAdapter
//
//        return view
//    }

//    override fun onDestroy() {
//        getBaseActivity()!!.dataPrepareService.dataTransferStation.payNoAttentionToSensor(sensor!!)
//        mPhysicalSensorInfoAdapter!!.detachSensorValueContainer()
//        super.onDestroy()
//    }

//    private fun chooseDate(date: Long) {
//        var isDateChanged = true
//        if (date == 0L) {
//            if (mPhysicalSensorInfoAdapter!!.getIntraday() == 0L) {
//                mPhysicalSensorInfoAdapter!!.setIntraday(System.currentTimeMillis())
//            } else {
//                isDateChanged = false
//            }
//        } else if (date > 0) {
//            if (mPhysicalSensorInfoAdapter!!.isIntraday(date)) {
//                isDateChanged = false
//            } else {
//                val previousSize = mPhysicalSensorInfoAdapter!!.itemCount
//                mPhysicalSensorInfoAdapter!!.setIntraday(date)
//                mPhysicalSensorInfoAdapter!!.notifyDataSetChanged(previousSize)
//            }
//        } else {
//            throw IllegalArgumentException("intraday start time may not be less than 0")
//        }
//        if (isDateChanged) {
//            val dateTime = mPhysicalSensorInfoAdapter!!.getIntraday()
//            setDateLabel(dateTime)
//            if (mPhysicalSensorInfoAdapter!!.itemCount <= 1) {
//                getBaseActivity()!!
//                        .dataPrepareService
//                        .sensorHistoryDataAccessor
//                        .importSensorHistoryValue(sensor!!.rawAddress.toLong(),
//                                dateTime, getNextDayTime(dateTime), this)
//            }
//        } else if (TextUtils.isEmpty(mTvDate!!.text)) {
//            setDateLabel(mPhysicalSensorInfoAdapter!!.getIntraday())
//        }
//    }

//    private fun setDateLabel(date: Long) {
//        mTvDate!!.text = getString(R.string.sensor_info_date, date)
//    }

    private fun setValueLabels(measurements: List<LogicalSensor>) {
        var i = 0
        val measurementSize = adapter.scheduledDisplayCount - 1
        val displayCount = adapter.actualDisplayCount
        val offset = adapter.displayStartIndex
        while (i < displayCount) {
            val measurement = if (offset + i < measurementSize)
                measurements[offset + i]
            else
                null
            mTvValueLabels[i]!!.text = measurement?.name ?: "电量"
            ++i
        }
    }

    private fun setInfoOrientation(displayState: Int) {
        when (displayState) {
            PhysicalSensorInfoAdapter.ONLY_HAS_RIGHT_DISPLAY_ITEM -> mIvInfoOrientation!!.setImageResource(R.drawable.ic_info_right)
            PhysicalSensorInfoAdapter.ONLY_HAS_LEFT_DISPLAY_ITEM -> mIvInfoOrientation!!.setImageResource(R.drawable.ic_info_left)
            PhysicalSensorInfoAdapter.HAS_BOTH_DISPLAY_ITEM -> mIvInfoOrientation!!.setImageResource(R.drawable.ic_info_both)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARGUMENT_KEY_START_MEASUREMENT_INDEX, adapter.displayStartIndex)
    }

//    fun show(transaction: FragmentTransaction, sensor: PhysicalSensor, realTime: Boolean): Int {
//        this.sensor = sensor
//        mRealTime = realTime
//        return super.show(transaction, TAG)
//    }
//
//    fun show(manager: FragmentManager, sensor: PhysicalSensor, realTime: Boolean) {
//        this.sensor = sensor
//        mRealTime = realTime
//        super.show(manager, TAG)
//    }

//    override fun show(transaction: FragmentTransaction, tag: String): Int {
//        throw UnsupportedOperationException("use show(FragmentTransaction transaction, PhysicalSensor sensor) for instead")
//    }
//
//    override fun show(manager: FragmentManager, tag: String) {
//        throw UnsupportedOperationException("use show(FragmentManager manager, PhysicalSensor sensor) for instead")
//    }

//    private fun canNotifyValueChanged(sensor: PhysicalSensor): Boolean {
//        return this.sensor === sensor && isDialogShow
//    }

    override fun notifyPhysicalSensorRealTimeValueChanged(physicalSensor: PhysicalSensor, logicalPosition: Int) {
        notifySensorValueChanged(physicalSensor, logicalPosition)
    }

    override fun notifyPhysicalSensorHistoryValueChanged(physicalSensor: PhysicalSensor, logicalPosition: Int) {
        notifySensorValueChanged(physicalSensor, logicalPosition)
    }

    override fun notifyLogicalSensorHistoryValueChanged(logicalSensor: LogicalSensor, logicalPosition: Int) {
        if (logicalSensor.id.address == sensor.rawAddress) {
            val value = logicalSensor.getValueByContainerAddMethodReturnValue(logicalSensor.historyValueContainer, logicalPosition) ?: return
            val position = sensor.historyValueContainer.findValuePosition(logicalPosition, value.timestamp)
            if (position >= 0) {
                adapter.notifySensorValueUpdate(- position - 1, rvSensorInfo)
            }
        }
    }

//    fun notifySensorValueChanged(sensor: PhysicalSensor, valuePosition: Int) {
//        if (!canNotifyValueChanged(sensor)) {
//            return
//        }
//        mPhysicalSensorInfoAdapter!!.notifySensorValueUpdate(valuePosition)
//    }

//    fun notifyWarnProcessorLoaded() {
//        if (isDialogShow) {
//            mPhysicalSensorInfoAdapter!!.notifyItemRangeChanged(0, mPhysicalSensorInfoAdapter!!.itemCount)
//        }
//    }

    //    public void notifyMeasurementDataChanged(PhysicalSensor sensor, int position, long timestamp) {
    //        if (!canNotifyValueChanged(sensor)
    //                || mSensor.getHistoryValueContainer().interpretAddResult(position) == ADD_VALUE_FAILED) {
    //            return;
    //        }
    //        int sensorValuePosition = mSensor.getHistoryValueContainer().findValuePosition(position, timestamp);
    //        if (sensorValuePosition >= 0) {
    //            mPhysicalSensorInfoAdapter.notifyItemChanged(position);
    //        }
    //    }

    override fun onInfoOrientationChanged(newDisplayState: Int) {
        setInfoOrientation(newDisplayState)
    }

    override fun onDisplayStartIndexChanged(newDisplayStartIndex: Int) {
        setValueLabels(sensor.measurementCollections)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchX = event.x
                mLastTouchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - mLastTouchX
                val absX = Math.abs(deltaX)
                if (absX > 100 && absX > Math.abs(event.y - mLastTouchY)) {
                    if (deltaX > 0) {
                        adapter.showNextItem(true)
                        return true
                    } else if (deltaX < 0) {
                        adapter.showNextItem(false)
                        return true
                    }
                }
            }
        }
        return false
    }

//    companion object {
//
//        private val ARGUMENT_KEY_START_MEASUREMENT_INDEX = "smi"
//        val TAG = "physical_sensor_info"
//    }
}
