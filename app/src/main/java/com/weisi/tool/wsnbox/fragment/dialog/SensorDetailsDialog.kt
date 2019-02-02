package com.weisi.tool.wsnbox.fragment.dialog

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.cjq.lib.weisi.iot.Measurement
import com.cjq.lib.weisi.iot.Sensor
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.info.SensorInfoTableAdapter
import com.weisi.tool.wsnbox.processor.querier.LatestSensorHistoryDataTimestampQuerier
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.NullHelper
import com.weisi.tool.wsnbox.util.SafeAsyncTask

abstract class SensorDetailsDialog<S : Sensor> : MeasurementsDetailsDialog(), ITableViewListener, SafeAsyncTask.ResultAchiever<Long, Void> {

    protected val ARGUMENT_KEY_SENSOR = "sensor"

    protected var sensor: S by NullHelper.readonlyNotNull()
    protected var viewPrepared = false
        private set
    private var tableView: TableView by NullHelper.readonlyNotNull()
    //private var tableAdapter: SensorInfoTableAdapter<out Value, out Measurement<out Value, out Configuration<out Value>>> by NullHelper.readonlyNotNull()
    private var tableAdapter: SensorInfoTableAdapter by NullHelper.readonlyNotNull()

    open fun init(sensor: S) {
        this.sensor = sensor
    }

    override fun getObjectLabel(): String {
        return sensor.mainMeasurement.getName()
    }

    override fun getSensorAddressLabel(): String {
        return sensor.id.formatAddress
    }

    override fun getSensorStateLabel(): String {
        return getString(if (sensor.state == Sensor.ON_LINE) {
            R.string.sensor_info_state_on
        } else {
            R.string.sensor_info_state_off
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(ARGUMENT_KEY_SENSOR, sensor.id.id)
        super.onSaveInstanceState(outState)
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        super.onServiceConnectionCreate(service)
        if (isRealTime()) {
            //service.dataTransferStation.payAttentionToPhysicalSensor(sensor)
            payAttentionToSensor(service)
        }
    }

    override fun onServiceConnectionStart(service: DataPrepareService) {
        super.onServiceConnectionStart(service)
        if (isRealTime()) {
            enableDetectSensorInfoDynamicValueUpdate = true
        } else {
            enableDetectSensorInfoHistoryValueUpdate = true
        }
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        super.onServiceConnectionStop(service)
        if (isRealTime()) {
            enableDetectSensorInfoDynamicValueUpdate = false
        } else {
            enableDetectSensorInfoHistoryValueUpdate = false
        }
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
        if (isRealTime()) {
            payNoAttentionToSensor(service)
        }
        super.onServiceConnectionDestroy(service)
    }

    private fun payAttentionToSensor(service: DataPrepareService?) {
        service ?: return
        service.dataTransferStation.payAttentionToSensor(sensor)
    }

    private fun payNoAttentionToSensor(service: DataPrepareService?) {
        service ?: return
        service.dataTransferStation.payAttentionToSensor(sensor)
    }

    override fun onStopHistoryMode() {
        payAttentionToSensor(getBaseActivity()?.dataPrepareService)
    }

    override fun onStopRealTimeMode() {
        payNoAttentionToSensor(getBaseActivity()?.dataPrepareService)
    }

    override fun hasRealTimeValue(): Boolean {
        return sensor.mainMeasurement.hasRealTimeValue()
    }

    override fun onStartRealTimeMode() {
        initTableData()
    }

    override fun onStartHistoryMode(startTime: Long, endTime: Long) {
        initTableData()
        getBaseActivity()?.dataPrepareService?.sensorHistoryDataAccessor?.importSensorHistoryValue(sensor.id.id, startTime, endTime, null)
    }

    override fun initDataTableView(selectedMeasurementIndexes: IntArray?): View {
        val c = context ?: throw IllegalArgumentException("initDataTableView run in error time")
        tableView = LayoutInflater.from(c).inflate(R.layout.tableview_physical_sensor_info, null) as TableView
        tableAdapter = SensorInfoTableAdapter(c)
        tableView.adapter = tableAdapter
        tableView.tableViewListener = this
        tableView.isIgnoreSelectionColors = true
        viewPrepared = true
        initTableData(selectedMeasurementIndexes)
        return tableView
    }

    private fun initTableData(selectedMeasurementIndexes: IntArray?) {
        if (!viewPrepared) {
            return
        }
        initTableDataImpl(selectedMeasurementIndexes)
    }

    private fun initTableDataImpl(selectedMeasurementIndexes: IntArray?) {
        tableAdapter.initData(sensor, if (selectedMeasurementIndexes === null) {
            measurements
        } else {
            selectedMeasurementIndexes.map {
                measurements[it]
            }
        })
    }

    private fun initTableData() {
        if (!viewPrepared) {
            return
        }
        initTableDataImpl(getSelectedMeasurementIndexes())
    }

    override fun onEnableMeasurementDisplay(measurement: Measurement<*, *>, enabled: Boolean) {
        tableAdapter.showMeasurement(sensor, measurement, enabled)
    }

    protected fun updateMeasurementTableValue(measurement: Measurement<*, *>, valuePosition: Int) {
        tableAdapter.updateMeasurementValue(sensor, measurement, valuePosition)
    }

    protected fun updateMainMeasurementTableValue(mainMeasurement: Measurement<*, *>, valuePosition: Int) {
        tableAdapter.updateMainValue(sensor, mainMeasurement, valuePosition)
    }

    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
    }

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
        tableAdapter.selectRow(row)
    }

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
        tableAdapter.selectColumn(column)
    }

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
        tableAdapter.selectCell(row, column)
        val position = tableAdapter.getActualRowPosition(row, isRealTime(), false)
        if (position < 0 || position >= sensor.info.uniteValueContainer.size()) {
            return
        }
        val targetTimestamp = sensor.info.uniteValueContainer.getValue(position).timestamp
        onMeasurementValueSelectedInTable(tableAdapter.getColumnHeaderItem(column), targetTimestamp)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
    }

    override fun onMeasurementValueSelectedInCurve(measurement: Measurement<*, *>, timestamp: Long) {
        val physicalPosition = sensor.mainMeasurement.getUniteValueContainer().findValuePosition(timestamp)
        if (physicalPosition < 0) {
            return
        }
        val column = tableAdapter.findActualColumnPosition(measurement)
        if (column < 0) {
            return
        }
        val row = tableAdapter.getActualRowPosition(physicalPosition, isRealTime(), false)
        tableAdapter.selectCell(row, column)
    }

    override fun queryLatestHistroyDataTimestamp() {
        LatestSensorHistoryDataTimestampQuerier(this).execute(sensor)
    }

    override fun onResultAchieved(result: Long?) {
        result?.let {
            chooseDate(it)
        }
    }
}