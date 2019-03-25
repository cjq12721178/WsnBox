package com.weisi.tool.wsnbox.fragment.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.Measurement
import com.github.mikephil.charting.charts.LineChart
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.fragment.BaseFragment

class MeasurementCurveFragment<M : Measurement<*, *>> : BaseFragment() {

    private val measurements = mutableListOf<M>()

    fun addMeasurement(measurement: M) {
        if (measurements.isNotEmpty()
                && measurements[0].getCurveType() != measurement.getCurveType()) {
            throw IllegalArgumentException("wrong curve type: ${measurement.getCurveType()}")
        }
        measurements.add(measurement)
    }

    fun removeMeasurement(measurement: Measurement<*, *>): Int {
        measurements.remove(measurement)
        return measurements.size
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_measurement_curve, null) as LineChart
    }
}