package com.weisi.tool.wsnbox

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestKotlinAs {

    @Test
    fun test_as() {
        val appContext = InstrumentationRegistry.getTargetContext()

        val view = LineChart(appContext)
        val actual = view as? Chart<out ChartData<IDataSet<Entry>>>
        assertEquals(view, actual)
    }
}