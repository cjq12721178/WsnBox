package com.weisi.tool.wsnbox.fragment.dialog

import android.os.Bundle
import com.cjq.lib.weisi.iot.*

class PhysicalSensorDetailsDialog : SensorDetailsDialog<PhysicalSensor>() {

    //private var viewPrepared = false
    //private var sensor: PhysicalSensor by NullHelper.readonlyNotNull()
//    private var tableView: TableView by NullHelper.readonlyNotNull()
//    private var tableAdapter: PhysicalSensorInfoTableAdapter by NullHelper.readonlyNotNull()

    override fun init(sensor: PhysicalSensor) {
        //this.sensor = sensor
        super.init(sensor)
        initMeasurements(sensor)
    }

//    override fun getObjectLabel(): String {
//        return sensor.info.name
//    }

//    override fun getSensorAddressLabel(): String {
//        return sensor.formatAddress
//    }

//    override fun getSensorStateLabel(): String {
//        return getString(if (sensor.state == Sensor.ON_LINE) {
//            R.string.sensor_info_state_on
//        } else {
//            R.string.sensor_info_state_off
//        })
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            sensor = SensorManager.getPhysicalSensor(it.getLong(ARGUMENT_KEY_SENSOR))
        }
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putLong(ARGUMENT_KEY_SENSOR, sensor.id.id)
//        super.onSaveInstanceState(outState)
//    }

//    override fun onServiceConnectionCreate(service: DataPrepareService) {
//        super.onServiceConnectionCreate(service)
//        if (isRealTime()) {
//            //service.dataTransferStation.payAttentionToPhysicalSensor(sensor)
//            payAttentionToSensor(service)
//        }
//    }
//
//    override fun onServiceConnectionStart(service: DataPrepareService) {
//        super.onServiceConnectionStart(service)
//        if (isRealTime()) {
//            enableDetectSensorInfoDynamicValueUpdate = true
//        } else {
//            enableDetectSensorInfoHistoryValueUpdate = true
//        }
//    }
//
//    override fun onServiceConnectionStop(service: DataPrepareService) {
//        super.onServiceConnectionStop(service)
//        if (isRealTime()) {
//            enableDetectSensorInfoDynamicValueUpdate = false
//        } else {
//            enableDetectSensorInfoHistoryValueUpdate = false
//        }
//    }
//
//    override fun onServiceConnectionDestroy(service: DataPrepareService) {
//        if (isRealTime()) {
//            payNoAttentionToSensor(service)
//        }
//        super.onServiceConnectionDestroy(service)
//    }
//
//    private fun payAttentionToSensor(service: DataPrepareService?) {
//        service ?: return
//        service.dataTransferStation.payAttentionToPhysicalSensor(sensor)
//    }
//
//    private fun payNoAttentionToSensor(service: DataPrepareService?) {
//        service ?: return
//        service.dataTransferStation.payNoAttentionToPhysicalSensor(sensor)
//    }

    override fun enableUpdateMeasurementValue(measurementId: Long): Boolean {
        return sensor.rawAddress == ID.getAddress(measurementId)
    }

//    override fun onStopHistoryMode() {
//        payAttentionToSensor(getBaseActivity()?.dataPrepareService)
//    }
//
//    override fun onStopRealTimeMode() {
//        payNoAttentionToSensor(getBaseActivity()?.dataPrepareService)
//    }

//    override fun onStartRealTimeMode() {
//        initTableData()
//    }
//
//    override fun onStartHistoryMode(startTime: Long, endTime: Long) {
//        initTableData()
//        getBaseActivity()?.dataPrepareService?.sensorHistoryDataAccessor?.importSensorHistoryValue(sensor.id.id, startTime, endTime, null)
//    }

//    override fun initDataTableView(selectedMeasurementIndexes: IntArray?): View {
//        val c = context ?: throw IllegalArgumentException("initDataTableView run in error time")
//        tableView = LayoutInflater.from(c).inflate(R.layout.tableview_physical_sensor_info, null) as TableView
//        tableAdapter = PhysicalSensorInfoTableAdapter(c)
//        tableView.adapter = tableAdapter
//        tableView.tableViewListener = this
//        tableView.isIgnoreSelectionColors = true
//        viewPrepared = true
//        initTableData(selectedMeasurementIndexes)
//        return tableView
//    }

//    private fun initTableData(selectedMeasurementIndexes: IntArray?) {
//        if (!viewPrepared) {
//            return
//        }
//        initTableDataImpl(selectedMeasurementIndexes)
//    }
//
//    private fun initTableDataImpl(selectedMeasurementIndexes: IntArray?) {
//        tableAdapter.initData(sensor.info, if (selectedMeasurementIndexes === null) {
//            measurements
//        } else {
//            selectedMeasurementIndexes.map {
//                measurements[it]
//            }
//        })
//    }
//
//    private fun initTableData() {
//        if (!viewPrepared) {
//            return
//        }
//        initTableDataImpl(getSelectedMeasurementIndexes())
//    }

//    override fun onEnableMeasurementDisplay(measurement: Measurement<*, *>, enabled: Boolean) {
//        tableAdapter.showMeasurement(sensor.info, measurement, enabled)
//    }

//    private fun processSelection(updateRowPosition: Int) {
//        val selectedRowPosition = tableView.selectedRow
//        if (selectedRowPosition == SelectionHandler.UNSELECTED_POSITION
//                || updateRowPosition == SelectionHandler.UNSELECTED_POSITION
//                || updateRowPosition > selectedRowPosition) {
//            return
//        }
//        if (tableView.selectedColumn == SelectionHandler.UNSELECTED_POSITION) {
//            tableView.selectedRow += 1
//        } else {
//            tableView.setSelectedCell(tableView.selectedColumn, selectedRowPosition + 1)
//        }
//    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
        if (isRealTime()) {
            return
        }
        super.onSensorInfoHistoryValueUpdate(info, valuePosition)
        if (!viewPrepared) {
            return
        }
        //tableAdapter.updateMainValue(info, valuePosition)
        updateMainMeasurementTableValue(info, valuePosition)
    }

    override fun onSensorInfoDynamicValueUpdate(info: Sensor.Info, valuePosition: Int) {
        setStateLabel()
        if (!isRealTime()) {
            return
        }
        super.onSensorInfoDynamicValueUpdate(info, valuePosition)
        //Log.d(Tag.LOG_TAG_D_TEST, "info, physical position = ${info.uniteValueContainer.getPhysicalPositionByLogicalPosition(valuePosition)}")
        if (!viewPrepared) {
            return
        }
        //tableAdapter.updateMainValue(info, valuePosition)
        updateMainMeasurementTableValue(info, valuePosition)
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (!isRealTime()) {
            return
        }
        super.onMeasurementDynamicValueUpdate(measurement, valuePosition)
        //Log.d(Tag.LOG_TAG_D_TEST, "measurement(${measurement.name}), physical position = ${measurement.uniteValueContainer.getPhysicalPositionByLogicalPosition(valuePosition)}")
        if (!viewPrepared) {
            return
        }
        //tableAdapter.updateMeasurementValue(sensor.info, measurement, valuePosition)
        updateMeasurementTableValue(measurement, valuePosition)
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (isRealTime()) {
            return
        }
        super.onMeasurementHistoryValueUpdate(measurement, valuePosition)
        if (!viewPrepared) {
            return
        }
        //tableAdapter.updateMeasurementValue(sensor.info, measurement, valuePosition)
        updateMeasurementTableValue(measurement, valuePosition)
    }

//    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
//    }
//
//    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
//    }
//
//    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
//        tableAdapter.selectRow(row)
//    }
//
//    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int) {
//        tableAdapter.selectColumn(column)
//    }
//
//    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
//        //Log.d(Tag.LOG_TAG_D_TEST, "row: $row, column: $column, holder item id: ${cellView.itemId}, layoutPosition: ${cellView.layoutPosition}, rowPosition: ${cellView.itemView.tag}")
//        tableAdapter.selectCell(row, column)
//        val position = tableAdapter.getActualRowPosition(row, isRealTime(), false)
//        if (position < 0 || position >= sensor.info.uniteValueContainer.size()) {
//            return
//        }
//        val targetTimestamp = sensor.info.uniteValueContainer.getValue(position).timestamp
////        val calendar = Calendar.getInstance()
////        calendar.timeInMillis = targetTimestamp
////        Log.d(Tag.LOG_TAG_D_TEST, "table header label: ${tableAdapter.rowHeaderRecyclerViewAdapter.getItem(row)}, view label: ${cellView.itemView.tv_content.text}, value time: ${SimpleDateFormat("HH:mm:ss.SSS").format(calendar.time)}")
//        onMeasurementValueSelectedInTable(tableAdapter.getColumnHeaderItem(column), targetTimestamp)
//    }
//
//    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
//    }

//    override fun onMeasurementValueSelectedInCurve(measurement: Measurement<*, *>, timestamp: Long) {
//        val physicalPosition = sensor.info.uniteValueContainer.findValuePosition(timestamp)
//        if (physicalPosition < 0) {
//            return
//        }
//        val column = tableAdapter.findActualColumnPosition(measurement)
//        if (column < 0) {
//            return
//        }
//        val row = tableAdapter.getActualRowPosition(physicalPosition, isRealTime(), false)
//        tableAdapter.selectCell(row, column)
//    }

//    override fun hasRealTimeValue(): Boolean {
//        return sensor.info.hasRealTimeValue()
//    }

//    override fun queryLatestHistroyDataTimestamp() {
//        LatestSensorHistoryDataTimestampQuerier(this).execute(sensor)
//    }

//    override fun onResultAchieved(result: Long?) {
//        result?.let {
//            //Log.d(Tag.LOG_TAG_D_TEST, "onResultAchieved")
//            chooseDate(it)
//            //Log.d(Tag.LOG_TAG_D_TEST, "after chooseDate")
//        }
//    }
}