package com.weisi.tool.wsnbox.adapter.info

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.Measurement
import com.cjq.lib.weisi.iot.container.Value
import com.cjq.lib.weisi.iot.container.ValueContainer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.bean.warner.CommonSwitchWarner
import com.weisi.tool.wsnbox.util.NullHelper
import com.weisi.tool.wsnbox.util.Tag
import com.wsn.lib.wsb.util.SimpleReflection
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class MeasurementCurvePage(private val historyDataTimeInterval: Long, private val para: Int = 0) {

    var onMeasurementValueSelectedListener: OnMeasurementValueSelectedListener? = null
    private val measurements = mutableListOf<Measurement<*, *>>()
    private var vChart: BarLineChartBase<*>? = null
    private var chartMaker: ChartMaker<out Entry> by NullHelper.readonlyNotNull()

    fun addMeasurement(measurement: Measurement<*, *>) {
        if (measurements.isEmpty()) {
            chartMaker = buildChartMaker(measurement.curvePattern)
        } else {
            if (measurements[0].curveType != measurement.curveType) {
                throw IllegalArgumentException("wrong curve type: ${measurement.curveType}")
            }
        }
        measurements.add(measurement)
        chartMaker.addDataSet(measurements.size - 1)
    }

    private fun buildChartMaker(curvePattern: Int): ChartMaker<out Entry> {
        return when (curvePattern) {
            Measurement.CP_ANALOG -> AnalogChartMaker()
            Measurement.CP_STATUS -> StatusChartMaker()
            Measurement.CP_COUNT -> CountChartMaker()
            else -> throw IllegalArgumentException("unknown curve pattern: $curvePattern")
        }
    }

    fun removeMeasurement(measurement: Measurement<*, *>): Int {
        val pos = measurements.indexOf(measurement)
        if (pos >= 0) {
            measurements.removeAt(pos)
            chartMaker.removeDataSet(pos)
        }
        return measurements.size
    }

    fun notifyMeasurementValueUpdate(measurement: Measurement<*, *>, valueLogicalPosition: Int) {
        val measurementPosition = measurements.indexOf(measurement)
        if (measurementPosition < 0) {
            return
        }
        when (measurement.uniteValueContainer?.interpretAddResult(valueLogicalPosition)) {
            ValueContainer.NEW_VALUE_ADDED -> {
                chartMaker.addEntry(measurementPosition, valueLogicalPosition, false)
            }
            ValueContainer.LOOP_VALUE_ADDED -> {
                chartMaker.addEntry(measurementPosition, valueLogicalPosition, true)
            }
            ValueContainer.VALUE_UPDATED -> {
                chartMaker.setEntry(measurementPosition, valueLogicalPosition)
            }
        }
    }

    fun notifyDataSetChanged() {
        chartMaker.notifyDataSetChanged()
    }

    fun getView(group: ViewGroup): View {
        return vChart ?: chartMaker.createView(group).apply { vChart = this }
    }

    fun highlightEntry(measurement: Measurement<*, *>, targetTimestamp: Long) {
        val chart = vChart ?: return
        val pos = measurements.indexOf(measurement)
        if (pos < 0) {
            return
        }
        val deltaTime = chartMaker.correctTimestamp(targetTimestamp)
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = earliestTime
//        calendar.add(Calendar.SECOND, deltaTime.toInt())
//        Log.d(Tag.LOG_TAG_D_TEST, "curve src time: $targetTimestamp, earliest time: $earliestTime, deltaTime: $deltaTime, label: ${SimpleDateFormat("HH:mm:ss").format(calendar.time)}")
        chart.highlightValue(deltaTime, pos, false)
        chart.moveViewToX(deltaTime)
    }

    interface OnMeasurementValueSelectedListener {
        fun onMeasurementValueSelectedInCurve(measurement: Measurement<*, *>, timestamp: Long)
    }

    private abstract inner class ChartMaker<E : Entry> {

        private var earliestTime: Long by Delegates.notNull()
        private var timeInterval: Long by Delegates.notNull()

        private fun initTimeInterval() {
            if (measurements.isEmpty()) {
                return
            }
            timeInterval = if (measurements[0].uniteValueContainer == measurements[0].dynamicValueContainer
                    || measurements[0].uniteValueContainer == null) {
                1000L
            } else {
                historyDataTimeInterval
            }
        }

        fun createView(group: ViewGroup): BarLineChartBase<*> {
            Log.d(Tag.LOG_TAG_D_TEST, "createView")
            //设置时间间隔
            initTimeInterval()

            val context = group.context
            val chart = onCreateView(context)
            //设置曲线图事件
            // enable description text
            chart.description.isEnabled = false
            // enable touch gestures
            chart.setTouchEnabled(true)
            // enable scaling and dragging
            chart.isDragEnabled = true
            chart.setScaleEnabled(true)
            chart.setDrawGridBackground(false)
            // if disabled, scaling can be done on x- and y-axis separately
            chart.setPinchZoom(false)

            //设置X、Y轴
            setAxises(chart)

            //设置最早时间戳
            initEarliestTime()

            //设置数据集、告警线
            val data = createData(chart)
            measurements.forEachIndexed { index, measurement ->
                val set = createDataSet(context, index, getCurveDescriptionName(measurement))
                data.addDataSet(set)
                setLimitLines(context, chart.axisLeft, measurement)
            }

            initData(chart, data)

            chart.data = data
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {

                }

                override fun onValueSelected(e: Entry, h: Highlight) {
                    onMeasurementValueSelectedListener?.onMeasurementValueSelectedInCurve(measurements[h.dataSetIndex], e.data as Long)
                }
            })
            data.notifyDataChanged()
            chart.notifyDataSetChanged()

            return chart
        }

        protected open fun createData(chart: BarLineChartBase<*>): BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<E>> {
            val constructor = (SimpleReflection.getClassParameterizedType(chart, 0) as Class<BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<E>>>).getConstructor()
            if (!constructor.isAccessible) {
                constructor.isAccessible = true
            }
            return constructor.newInstance()
        }

        private fun getCurveDescriptionName(measurement: Measurement<*, *>): String {
            return measurement.getValueLabel(para)
        }

        protected open fun setAxises(chart: BarLineChartBase<*>) {
            val xAxis = chart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.setCenterAxisLabels(true)
            xAxis.labelCount = 5
            xAxis.isEnabled = true
            xAxis.granularity = 1f
            xAxis.valueFormatter = object : IAxisValueFormatter {
                private val dateFormat = SimpleDateFormat("HH:mm:ss")
                override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = earliestTime + value.toInt() * timeInterval.toInt()
                    //calendar.add(Calendar.MILLISECOND, )
                    //Log.d(Tag.LOG_TAG_D_TEST, dateFormat.format(date))
                    return dateFormat.format(calendar.time)
                }
            }
            val leftAxis = chart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.setDrawLimitLinesBehindData(true)
            val rightAxis = chart.axisRight
            rightAxis.isEnabled = true
        }

        fun addDataSet(dataSetIndex: Int) {
            val chart = vChart as? Chart<out ChartData<IDataSet<out Entry>>> ?: return
            val data = chart.data ?: return
            val measurement = measurements[dataSetIndex]
            updateEarliestTimeWhenAddSet(getMeasurementEarliestTime(measurement), chart.data)
            val set = createDataSet(chart.context, dataSetIndex, getCurveDescriptionName(measurement))
            fillDataSet(measurement, set)
            data.addDataSet(set)
            data.notifyDataChanged()
            chart.notifyDataSetChanged()
        }

        fun removeDataSet(dataSetIndex: Int) {
            val chart = vChart ?: return
            val data = chart.data ?: return
            data.removeDataSet(dataSetIndex)
            updateEarliestTimeWhenRemoveSet(data)
            data.notifyDataChanged()
            chart.notifyDataSetChanged()
        }

        fun addEntry(measurementPosition: Int, valueLogicalPosition: Int, isLoop: Boolean) {
            val chart = vChart ?: return
            val data = chart.data as? ChartData<out IDataSet<E>> ?: return
            val measurement = measurements[measurementPosition]
            val container = measurement.uniteValueContainer
            val set = data.getDataSetByIndex(measurementPosition)
            if (isLoop) {
                set.removeFirst()
            }
            val valuePhysicalPosition = container.getPhysicalPositionByLogicalPosition(valueLogicalPosition)
            val value = container.getValue(valuePhysicalPosition)
            if (earliestTime > value.timestamp) {
                resetDataSet(chart, data)
            } else {
                val entry = createEntry(value, measurement)
                if (valuePhysicalPosition >= set.entryCount) {
                    set.addEntry(entry)
                } else {
                    set.addEntryOrdered(entry)
                }
                onAddEntry(chart, data, measurementPosition, entry)
            }
            data.notifyDataChanged()
            chart.notifyDataSetChanged()
        }

        protected open fun onAddEntry(chart: BarLineChartBase<*>, data: ChartData<out IDataSet<E>>, dataSetIndex: Int, entry: E) {
        }

        fun setEntry(measurementPosition: Int, valueLogicalPosition: Int) {
            val chart = vChart ?: return
            val data = chart.data ?: return
            val measurement = measurements[measurementPosition]
            val container = measurement.uniteValueContainer
            val set = data.getDataSetByIndex(measurementPosition)
            val valuePhysicalPosition = container.getPhysicalPositionByLogicalPosition(valueLogicalPosition)
            if (valuePhysicalPosition <= 0 || valueLogicalPosition > set.entryCount) {
                return
            }
            val entry = set.getEntryForIndex(valuePhysicalPosition) ?: return
            entry.y = getYAxisValue(container.getValue(valuePhysicalPosition), measurement)
            data.notifyDataChanged()
            chart.notifyDataSetChanged()
        }

        fun correctTimestamp(src: Long): Float {
            return ((src - earliestTime + timeInterval / 2) / timeInterval).toFloat()
        }

        fun notifyDataSetChanged() {
            val chart = vChart ?: return
            val data = chart.data as? ChartData<IDataSet<E>> ?: return
            resetDataSet(chart, data)
            data.notifyDataChanged()
            //Log.d(Tag.LOG_TAG_D_TEST, "after data notifyDataChanged")
            chart.notifyDataSetChanged()
            //Log.d(Tag.LOG_TAG_D_TEST, "after chart notifyDataSetChanged")
        }

        private fun createEarliestTime() = measurements.map { getMeasurementEarliestTime(it) }.min() ?: 0L

        private fun updateEarliestTimeWhenAddSet(measurementEarliestTime: Long, data: ChartData<*>) {
            val deltaTime: Long = measurementEarliestTime - earliestTime
            if (deltaTime >= 0L) {
                return
            }
            if (updateDataSetsTime(data, deltaTime)) {
                earliestTime = measurementEarliestTime
            }
        }

        private fun updateDataSetsTime(data: ChartData<*>, deltaTime: Long): Boolean {
            val dt = TimeUnit.MILLISECONDS.toSeconds(deltaTime)
            if (deltaTime == 0L) {
                return false
            }
            for (i in 0 until data.dataSetCount) {
                val set = data.getDataSetByIndex(i)
                for (j in 0 until set.entryCount) {
                    set.getEntryForIndex(j).x += dt
                }
            }
            return true
        }

        private fun getMeasurementEarliestTime(measurement: Measurement<*, *>): Long {
            return measurement.uniteValueContainer.earliestValue?.timestamp ?: Long.MAX_VALUE
        }

        private fun fillDataSet(measurement: Measurement<*, *>, set: IDataSet<E>) {
            val container = measurement.uniteValueContainer
            for (j in 0 until container.size()) {
                set.addEntry(createEntry(container.getValue(j), measurement))
            }
            set.calcMinMax()
        }

        private fun updateEarliestTimeWhenRemoveSet(data: ChartData<*>) {
            val newEarliestTime = createEarliestTime()
            if (newEarliestTime > earliestTime) {
                if (updateDataSetsTime(data, earliestTime - newEarliestTime)) {
                    earliestTime = newEarliestTime
                }
            }
        }

        protected open fun initData(chart: BarLineChartBase<*>, data: ChartData<out IDataSet<E>>) {
            for (i in 0 until data.dataSetCount) {
                fillDataSet(measurements[i], data.getDataSetByIndex(i))
            }
            chart.animateX(750)
        }

        private fun resetDataSet(chart: BarLineChartBase<*>, data: ChartData<out IDataSet<E>>) {
            for (i in 0 until data.dataSetCount) {
                val set = data.getDataSetByIndex(i)
                set.clear()
            }

            chart.resetViewPortOffsets()
            chart.resetZoom()

            initTimeInterval()
            initEarliestTime()
            initData(chart, data)
        }

        private fun setLimitLines(context: Context, yAxis: YAxis, measurement: Measurement<*, *>) {
            val configuration = measurement.getConfiguration() as? DisplayMeasurement.Configuration
                    ?: return
            val warner = configuration.warner
            when (warner) {
                is CommonSingleRangeWarner -> {
                    val highLimitLabel = context.getString(R.string.high_limit)
                    val lowLimitLabel = context.getString(R.string.low_limit)
                    setLimitLine(context, yAxis, warner.lowLimit.toFloat(),
                            LimitLine.LimitLabelPosition.RIGHT_BOTTOM,
                            measurement.getName(), lowLimitLabel)
                    setLimitLine(context, yAxis, warner.highLimit.toFloat(),
                            LimitLine.LimitLabelPosition.RIGHT_TOP,
                            measurement.getName(), highLimitLabel)
                }
                is CommonSwitchWarner -> {
                    if (warner.abnormalValue == 1.0) {
                        setLimitLine(context, yAxis, 0.5f, LimitLine.LimitLabelPosition.RIGHT_TOP, measurement.getName(), "")
                    } else {
                        setLimitLine(context, yAxis, -0.5f, LimitLine.LimitLabelPosition.RIGHT_BOTTOM, measurement.getName(), "")
                    }
                }
            }
        }

        private fun setLimitLine(context: Context, yAxis: YAxis, limit: Float, labelPosition: LimitLine.LimitLabelPosition, measurementName: String, limitLabel: String) {
            val limitLinePosition = findLimitLine(yAxis, labelPosition, limit)
            if (limitLinePosition >= 0) {
                modifyLimitLineLabel(yAxis.limitLines[limitLinePosition], measurementName, limitLabel)
            } else {
                val limitLine = LimitLine(limit, if (measurements.size == 1) {
                    limitLabel
                } else {
                    measurementName + limitLabel
                })
                limitLine.lineWidth = 2f
                limitLine.enableDashedLine(10f, 10f, 0f)
                limitLine.labelPosition = labelPosition
                limitLine.textSize = context.resources.getDimensionPixelSize(R.dimen.size_text_comments).toFloat()
                val color = ContextCompat.getColor(context, if (labelPosition == LimitLine.LimitLabelPosition.RIGHT_BOTTOM) {
                    R.color.warner_low_limit
                } else {
                    R.color.warner_high_limit
                })
                limitLine.lineColor = color
                limitLine.textColor = color
                yAxis.addLimitLine(limitLine)
            }
        }

        private fun findLimitLine(yAxis: YAxis, labelPosition: LimitLine.LimitLabelPosition, limit: Float): Int {
            repeat(yAxis.limitLines.size) {
                val limitLine = yAxis.limitLines[it]
                if (limitLine.labelPosition == labelPosition
                        && Math.abs(limitLine.limit - limit) <= 1) {
                    return it
                }
            }
            return -1
        }

        private fun modifyLimitLineLabel(limitLine: LimitLine, measurementName: String, labelSuffix: String) {
            limitLine.label = limitLine.label.substringBeforeLast(labelSuffix) + '、' + measurementName + labelSuffix
        }

        protected open fun getYAxisValue(value: Value, measurement: Measurement<*, *>): Float {
            //return value.getRawValue(para).toFloat()
            //return measurement.getCorrectedValue(value, para).toFloat()
            return value.getCorrectedValue(measurement.getCorrector(para), para).toFloat()
        }

        protected fun initEarliestTime() {
            earliestTime = createEarliestTime()
        }

        protected fun getColorByDataSetIndex(index: Int): Int {
            return ColorTemplate.VORDIPLOM_COLORS[(index + 3) % ColorTemplate.VORDIPLOM_COLORS.size]
        }

        protected fun getTextSize(context: Context): Float {
            return context.resources.getDimensionPixelSize(R.dimen.size_text_comments).toFloat()
        }

        protected abstract fun onCreateView(context: Context): BarLineChartBase<*>

        protected abstract fun createEntry(value: Value, measurement: Measurement<*, *>): E

        protected abstract fun createDataSet(context: Context, dataSetIndex: Int, labelName: String): IBarLineScatterCandleBubbleDataSet<E>
    }

    private inner class AnalogChartMaker : ChartMaker<Entry>() {

        override fun onCreateView(context: Context): BarLineChartBase<*> {
            return LineChart(context)
        }

        override fun createEntry(value: Value, measurement: Measurement<*, *>): Entry {
            return Entry(correctTimestamp(value.timestamp), getYAxisValue(value, measurement), value.timestamp)
        }

        override fun createDataSet(context: Context, dataSetIndex: Int, labelName: String): IBarLineScatterCandleBubbleDataSet<Entry> {
            val set = LineDataSet(null, labelName)
            set.axisDependency = YAxis.AxisDependency.LEFT
            val lineColor = getColorByDataSetIndex(dataSetIndex)
            set.color = lineColor
            set.setCircleColor(lineColor)
            set.lineWidth = 2f
            set.circleRadius = 4f
            //set.fillAlpha = 65
            //set.fillColor = ColorTemplate.getHoloBlue()
            set.highLightColor = Color.rgb(244, 117, 117)
            //set.valueTextColor = Color.WHITE
            set.valueTextSize = getTextSize(context)
            set.setDrawValues(false)
            return set
        }

        override fun setAxises(chart: BarLineChartBase<*>) {
            super.setAxises(chart)
            val valueFormatter = IAxisValueFormatter { value, _ ->
                val label = measurements[0].decorateValue(value.toDouble(), para)
                var trimStart = label.lastIndexOf('.')
                if (trimStart >= 0) {
                    var trimEnd = trimStart + 1
                    while (trimEnd < label.length) {
                        if (label[trimEnd] == '0') {
                            ++trimEnd
                        } else if (label[trimEnd].isDigit()) {
                            trimStart = ++trimEnd
                        } else {
                            break
                        }
                    }
                    if (trimEnd > trimStart) {
                        label.removeRange(trimStart, trimEnd)
                    } else {
                        label
                    }
                } else {
                    label
                }
            }
            chart.axisLeft.valueFormatter = valueFormatter
            chart.axisRight.valueFormatter = valueFormatter
        }
    }

    private abstract inner class BarChartMaker : ChartMaker<BarEntry>() {

        private val fitWidth = 20f
        private var valueRange = -1f

        override fun onCreateView(context: Context): BarLineChartBase<*> {
            val chart = BarChart(context)
            //chart.isAutoScaleMinMaxEnabled = true
            //chart.set
            return chart
        }

        override fun createEntry(value: Value, measurement: Measurement<*, *>): BarEntry {
            return BarEntry(correctTimestamp(value.timestamp), getYAxisValue(value, measurement), value.timestamp)
        }

        override fun createDataSet(context: Context, dataSetIndex: Int, labelName: String): IBarLineScatterCandleBubbleDataSet<BarEntry> {
            val set = BarDataSet(ArrayList(), labelName)
            set.color = getColorByDataSetIndex(dataSetIndex)
            set.valueTextSize = getTextSize(context)
            set.setDrawValues(false)
            return set
        }

        override fun onAddEntry(chart: BarLineChartBase<*>, data: ChartData<out IDataSet<BarEntry>>, dataSetIndex: Int, entry: BarEntry) {
            //至少让那小条子看的见吧。。
            ensureBarWidthNotTooSmall(chart, data, entry)
        }

        private fun ensureBarWidthNotTooSmall(chart: BarLineChartBase<*>, data: ChartData<out IDataSet<BarEntry>>, entry: BarEntry?) {
            entry ?: return
//            if (data.entryCount <= 1) {
//                return
//            }
            if (valueRange == chart.xRange) {
                return
            }
            valueRange = if (chart.xRange > 1f && chart.xRange != Float.POSITIVE_INFINITY) {
                chart.xRange
            } else if (entry.x == 0f) {
                return
            } else {
                entry.x + 1
            }
            //Log.d(Tag.LOG_TAG_D_TEST, "before visibleXRange: ${chart.visibleXRange}, xRange: ${chart.xRange}, chart.scaleX: ${chart.scaleX}, chartWidth: ${chart.width}, entry.x: ${entry.x}")
            val scaleX = valueRange / fitWidth / chart.scaleX
            chart.zoom(scaleX, 1f, entry.x, entry.y)
            chart.moveViewToX(entry.x)
            //Log.d(Tag.LOG_TAG_D_TEST, "after visibleXRange: ${chart.visibleXRange}, xRange: ${chart.xRange}, scaleX: $scaleX, chartWidth: ${chart.width}, entry.x: ${entry.x}")
        }

        override fun initData(chart: BarLineChartBase<*>, data: ChartData<out IDataSet<BarEntry>>) {
            super.initData(chart, data)
//            Log.d(Tag.LOG_TAG_D_TEST, "init data ${if (measurements[0].getUniteValueContainer() === measurements[0].getDynamicValueContainer()) {
//                "real-time"
//            } else {
//                "history"
//            }}")
            ensureBarWidthNotTooSmall(chart, data, getLatestEntry(data))
        }

        private fun getLatestEntry(data: ChartData<out IDataSet<BarEntry>>): BarEntry? {
            var entry: BarEntry? = null
            var tmp: BarEntry
            repeat(data.dataSetCount) {
                data.getDataSetByIndex(it).apply {
                    if (entryCount > 0) {
                        if (entry === null) {
                            entry = getEntryForIndex(entryCount - 1)
                        } else {
                            tmp = getEntryForIndex(entryCount - 1)
                            if (entry!!.x < tmp.x) {
                                entry = tmp
                            }
                        }
                    }
                }
            }
            return entry
        }
    }

    private inner class StatusChartMaker : BarChartMaker() {

        override fun getYAxisValue(value: Value, measurement: Measurement<*, *>): Float {
            val yAxisValue = value.getCorrectedValue(measurement.getCorrector(para), para)
            return if (yAxisValue == 1.0){
                1f
            } else {
                -1f
            }
        }

        override fun setAxises(chart: BarLineChartBase<*>) {
            super.setAxises(chart)
            chart.axisLeft.axisMaximum = 1f
            chart.axisLeft.axisMinimum = -1f
            chart.axisLeft.labelCount = 3
            chart.axisRight.axisMaximum = 1f
            chart.axisRight.axisMinimum = -1f
            chart.axisRight.labelCount = 3
            chart.isDragYEnabled = false
            val valueFormatter = IAxisValueFormatter { value, _ ->
                if (value == 0f) {
                    return@IAxisValueFormatter ""
                }
                measurements[0].decorateValue(if (value > 0) {
                    1.0
                } else {
                    0.0
                }, para)
            }
            chart.axisLeft.valueFormatter = valueFormatter
            chart.axisRight.valueFormatter = valueFormatter
        }
    }

    private inner class CountChartMaker : BarChartMaker() {

        override fun setAxises(chart: BarLineChartBase<*>) {
            super.setAxises(chart)
            chart.axisLeft.axisMinimum = 0f
            chart.axisRight.axisMinimum = 0f
            val valueFormatter = IAxisValueFormatter { value, _ ->
                measurements[0].decorateValue(value.toDouble(), para)
            }
            chart.axisLeft.valueFormatter = valueFormatter
            chart.axisRight.valueFormatter = valueFormatter
        }
    }
}

//@ExperimentalContracts
//internal fun isDataPrepared(view: View?): Boolean {
//    contract {
//        returns(true) implies (view is Chart<out ChartData<out IDataSet<out Entry>>>)
//    }
//    return view is Chart<out ChartData<out IDataSet<out Entry>>> && view.data !== null
//}