package com.weisi.tool.wsnbox.adapter.info

import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.Measurement
import fr.castorflex.android.verticalviewpager.VerticalViewPager

class MeasurementCurvePageAdapter : PagerAdapter() {

    var onMeasurementValueSelectedListener: MeasurementCurvePage.OnMeasurementValueSelectedListener? = null
        set(value) {
            repeat(pages.size()) {
                pages.valueAt(it).onMeasurementValueSelectedListener = onMeasurementValueSelectedListener
            }
            field = value
        }

    private val pages = SparseArray<MeasurementCurvePage>()
    private val pages2 = ArrayList<MeasurementCurvePage>()

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return pages2.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) {
            container.removeView(`object`)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = pages2[position].getView(container)
        container.addView(view)
        return view
    }

//    private fun getSignificantPosition(position: Int): Int {
//        return count - position - 1
//    }

    fun addPage(measurement: Measurement<*, *>, historyDataTimeInterval: Long) {
        val page = pages[measurement.getCurveType()] ?: MeasurementCurvePage(historyDataTimeInterval).apply {
            this.onMeasurementValueSelectedListener = this@MeasurementCurvePageAdapter.onMeasurementValueSelectedListener
            pages.put(measurement.getCurveType(), this)
            pages2.add(this)
        }
        page.addMeasurement(measurement)
        notifyDataSetChanged()
    }

    fun removePage(measurement: Measurement<*, *>) {
        val pos = pages.indexOfKey(measurement.getCurveType())
        if (pos < 0) {
            return
        }
        val page = pages.valueAt(pos)
        if (page.removeMeasurement(measurement) == 0) {
            pages.removeAt(pos)
            pages2.remove(page)
        }
        notifyDataSetChanged()
    }

    fun updateValue(measurement: Measurement<*, *>, valueLogicalPosition: Int) {
        val page = pages[measurement.getCurveType()] ?: return
        page.notifyMeasurementValueUpdate(measurement, valueLogicalPosition)
        notifyDataSetChanged()
    }

    fun notifyDataTimeChanged() {
        repeat(pages2.size) {
            pages2[it].notifyDataSetChanged()
            //Log.d(Tag.LOG_TAG_D_TEST, "page $it notifyDataSetChanged")
        }
        notifyDataSetChanged()
        //Log.d(Tag.LOG_TAG_D_TEST, "view pager notifyDataSetChanged")
    }

    fun notifyDataHighlight(viewPager: VerticalViewPager, measurement: Measurement<*, *>, targetTimestamp: Long) {
        val page = pages[measurement.getCurveType()] ?: return
        //val pagePhysicalPosition = pages.indexOfValue(page)
        //val pageSignificantPosition = getSignificantPosition(pagePhysicalPosition)
        val pageSignificantPosition = pages2.indexOf(page)
        if (pageSignificantPosition != viewPager.currentItem) {
            viewPager.setCurrentItem(pageSignificantPosition, true)
        }
        page.highlightEntry(measurement, targetTimestamp)
    }
}