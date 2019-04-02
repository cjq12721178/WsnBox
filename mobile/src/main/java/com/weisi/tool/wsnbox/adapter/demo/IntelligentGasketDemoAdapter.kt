package com.weisi.tool.wsnbox.adapter.demo

import android.content.Context
import android.content.res.Configuration
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.weisi.tool.wsnbox.bean.data.Node
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.view.IntelligentGasketView

class IntelligentGasketDemoAdapter(context: Context, private val nodes: List<Node>) : PagerAdapter() {

    private val displayCount= if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        6
    } else {
        4
    }
    private val gaskets= Array(count) { i ->
        Array(displayCount) { j ->
            val view = IntelligentGasketView(context)
            val position = i * displayCount + j
            if (position >= nodes.size) {
                view.visibility = View.INVISIBLE
            } else {
                view.setLabel(nodes[position].getProperName())
                val warner = nodes[position].measurement.configuration.warner
                if (warner is CommonSingleRangeWarner) {
                    view.setLimit(warner.lowLimit, warner.highLimit)
                }
            }
            view
        }
    }
    private val pages = Array(count) { i ->
        val result = LinearLayout(context)
        result.orientation = LinearLayout.HORIZONTAL
        gaskets[i].forEach { gasket ->
            result.addView(gasket, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        }
        result
    }

    override fun isViewFromObject(v: View, o: Any): Boolean {
        return v === o
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return (nodes.size + displayCount - 1) / displayCount
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        if (o is View) {
            container.removeView(o)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = pages[position]
        container.addView(view)
        return view
    }

    fun updateGasketData(measurement: PracticalMeasurement) {
        repeat(nodes.size) {
            if (nodes[it].measurement == measurement) {
                gaskets[it / displayCount][it % displayCount].realTimeValue = measurement.realTimeValue?.getCorrectedValue(measurement.getCorrector(0), 0)
            }
        }
    }
}