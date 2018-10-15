package com.weisi.tool.wsnbox.dialog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.info.PhysicalSensorInfoAdapter
import kotlinx.android.synthetic.main.fragment_physical_sensor_info.view.*
import kotlinx.android.synthetic.main.li_physical_sensor_info.view.*


/**
 * Created by CJQ on 2017/11/3.
 */

class PhysicalSensorInfoDialog : SensorInfoDialog<PhysicalSensor, PhysicalSensorInfoAdapter>(),
        PhysicalSensorInfoAdapter.OnDisplayStateChangeListener,
        View.OnTouchListener {

    private val ARGUMENT_KEY_START_MEASUREMENT_INDEX = "smi"
    private var mIvInfoOrientation: ImageView? = null
    private val mTvValueLabels = arrayOfNulls<TextView>(PhysicalSensorInfoAdapter.MAX_DISPLAY_COUNT)
    private var mLastTouchX: Float = 0.toFloat()
    private var mLastTouchY: Float = 0.toFloat()

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
        tvTitle.text = getString(R.string.sensor_info_title, sensor.info.name)
        val tvAddress = view.findViewById<TextView>(R.id.tv_sensor_info_address)
        tvAddress.text = getString(R.string.sensor_info_address, sensor.formatAddress)
        val tvState = view.findViewById<TextView>(R.id.tv_sensor_state)
        tvState.setText(if (sensor.state == Sensor.ON_LINE)
            R.string.sensor_info_state_on
        else
            R.string.sensor_info_state_off)

        //设置RecyclerView header
        //val measurements = sensor.measurementCollections
        mTvValueLabels[0] = view.tv_measurement1
        mTvValueLabels[1] = view.tv_measurement2
        if (adapter.scheduledDisplayCount >= PhysicalSensorInfoAdapter.MAX_DISPLAY_COUNT) {
            val vsMeasurement = view.findViewById<View>(R.id.vs_measurement) as ViewStub
            mTvValueLabels[2] = vsMeasurement.inflate() as TextView
        }
        setValueLabels()

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

    private fun setValueLabels() {
        var i = 0
        val measurementSize = adapter.scheduledDisplayCount - 1
        val displayCount = adapter.actualDisplayCount
        val offset = adapter.displayStartIndex
        while (i < displayCount) {
            val measurement = if (offset + i < measurementSize)
                sensor.getDisplayMeasurementByPosition(offset + i)
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

    fun notifyRealTimeValueChanged(physicalSensor: PhysicalSensor, valuePosition: Int) {
        notifySensorValueChanged(physicalSensor, valuePosition)
    }

    fun notifySensorInfoHistoryValueChanged(info: Sensor.Info, valuePosition: Int) {
        if (info === sensor.info) {
            notifySensorValueChanged(sensor, valuePosition)
        }
    }

    fun notifyMeasurementHistoryValueChanged(measurement: PracticalMeasurement, valuePosition: Int) {
        if (measurement.id.address == sensor.rawAddress) {
            val value = measurement.getValueByContainerAddMethodReturnValue(measurement.historyValueContainer, valuePosition) ?: return
            val position = sensor.info.historyValueContainer.findValuePosition(valuePosition, value.timestamp)
            if (position >= 0) {
                adapter.notifySensorValueUpdate(- position - 1, rvSensorInfo)
            }
        }
    }

    override fun onInfoOrientationChanged(newDisplayState: Int) {
        setInfoOrientation(newDisplayState)
    }

    override fun onDisplayStartIndexChanged(newDisplayStartIndex: Int) {
        setValueLabels()
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
}
