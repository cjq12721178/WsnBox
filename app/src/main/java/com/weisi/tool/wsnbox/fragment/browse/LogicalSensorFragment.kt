package com.weisi.tool.wsnbox.fragment.browse

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.lib.weisi.iot.container.ValueContainer
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.FilterDialog
import com.cjq.tool.qbox.ui.dialog.SearchDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.browse.DataBrowseLogicalSensorAdapter
import com.weisi.tool.wsnbox.bean.filter.*
import com.weisi.tool.wsnbox.bean.sorter.SensorIdSorter
import com.weisi.tool.wsnbox.bean.sorter.SensorNameSorter
import com.weisi.tool.wsnbox.bean.sorter.SensorNetInTimeSorter
import com.weisi.tool.wsnbox.fragment.dialog.LogicalSensorDetailsDialog
import com.weisi.tool.wsnbox.processor.accessor.SensorHistoryDataAccessor
import com.weisi.tool.wsnbox.service.DataPrepareService
import kotlinx.android.synthetic.main.fragment_logical_sensor.*
import kotlinx.android.synthetic.main.fragment_logical_sensor.view.*
import kotlinx.android.synthetic.main.lh_logical_sensor.view.*

/**
 * Created by CJQ on 2018/5/29.
 */
class LogicalSensorFragment : DataBrowseFragment<LogicalSensor, DataBrowseLogicalSensorAdapter>(),
        View.OnClickListener,
        SensorHistoryDataAccessor.OnMissionFinishedListener {

    //private lateinit var rvSensors: RecyclerView
    //private var logicalSensorInfoDialog: LogicalSensorInfoDialog? = null

    override fun onCreateStorage(): Storage<LogicalSensor> {
        val s = Storage<LogicalSensor>() { elements, filters ->
            SensorManager.getSensors(elements, filters, LogicalSensor::class.java)
        }
        s.addFilter(FILTER_ID_DATA_SOURCE, SensorHasValueFilter())
        s.setSorter(SensorNetInTimeSorter(), false)
        return s
    }

//    override fun onRecoverInformationDialog(savedInstanceState: Bundle?) {
//        savedInstanceState ?: return
//        logicalSensorInfoDialog = childFragmentManager
//                .findFragmentByTag(SensorInfoDialog.TAG) as LogicalSensorInfoDialog?
//                ?: return
//    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_logical_sensor, null)
    }

    override fun onInitAdapter(view: View, storage: Storage<LogicalSensor>): DataBrowseLogicalSensorAdapter {
        //绑定事件
        view.tv_measurement_name_id.setOnClickListener(this)

        //初始化RecycleView
        //rvSensors = view.findViewById<RecyclerView>(R.id.rv_sensors)
        view.rv_sensors.layoutManager = LinearLayoutManager(context)
        view.rv_sensors.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_small), true))
        view.rv_sensors.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(view.rv_sensors) {
            override fun onItemClick(v: View, position: Int) {
                val sensor = adapter.getItemByPosition(position)
                val dialog = LogicalSensorDetailsDialog()
                dialog.init(sensor)
                dialog.show(childFragmentManager, DIALOG_TAG_INFO)
//                if (logicalSensorInfoDialog == null) {
//                    logicalSensorInfoDialog = LogicalSensorInfoDialog()
//                }
//                logicalSensorInfoDialog!!.show(childFragmentManager,
//                        sensor,
//                        isRealTime())
            }
        })

        //创建DataBrowseSensorAdapter
        val sensorAdapter = DataBrowseLogicalSensorAdapter(storage)
        view.rv_sensors.adapter = sensorAdapter
        return sensorAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_measurement_name_id -> {
                adapter.showMeasurementNameOrId = setListHeadMeasurementNameOrId(v as TextView)
                adapter.notifyItemRangeChanged(0, adapter.itemCount,
                        DataBrowseLogicalSensorAdapter.UPDATE_TYPE_MEASUREMENT_LABEL_CHANGED)
            }
        }
    }

    private fun setListHeadMeasurementNameOrId(tvMeasurementNameId: TextView): Boolean {
        val measurementNameLabel = getString(R.string.measurement_name)
        if (measurementNameLabel == tvMeasurementNameId.text) {
            tvMeasurementNameId.text = getString(R.string.sensor_id)
            return false
        } else {
            tvMeasurementNameId.text = measurementNameLabel
            return true
        }
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        enableDetectLogicalSensorNetIn = true
        enableDetectMeasurementDynamicValueUpdate = true
        enableDetectMeasurementHistoryValueUpdate = true
    }

//    override fun onServiceConnectionStop(service: DataPrepareService) {
//        if (isRealTime()) {
//            //enableDetectLogicalSensorValueUpdate = false
//            enableDetectMeasurementDynamicValueUpdate = false
//        }
//    }

//    override fun onServiceConnectionDestroy(service: DataPrepareService) {
//        if (isRealTime()) {
//            enableDetectLogicalSensorNetIn = false
//        }
//        //service.dataTransferStation.dynamicSensorDataImportMode = true
//        super.onServiceConnectionDestroy(service)
//    }

//    override fun onDataSourceChange(realTime: Boolean) {
//        super.onDataSourceChange(realTime)
//        val service = getBaseActivity().dataPrepareService ?: return
//        adapter.realTime = realTime
//        val transfer = service.dataTransferStation
//        enableDetectPhysicalSensorNetIn = false
//        //enableDetectPhysicalSensorValueUpdate = false
//        if (realTime) {
//            storage.addFilter(FILTER_ID_DATA_SOURCE, SensorHasRealTimeValueFilter())
//            if (storage.sorter is SensorEarliestValueTimeSorter) {
//                storage.setSorter(SensorNetInTimeSorter(), storage.isAscending)
//            }
//            enableDetectLogicalSensorNetIn = true
//            //enableDetectLogicalSensorValueUpdate = true
//            //enableDetectMeasurementHistoryValueReceive = false
//            //enableDetectSensorInfoHistoryValueReceive = false
//            enableDetectMeasurementDynamicValueUpdate = true
//            enableDetectMeasurementHistoryValueUpdate = false
//        } else {
//            storage.addFilter(FILTER_ID_DATA_SOURCE, SensorOnlyHasHistoryValueFilter())
//            if (storage.sorter is SensorNetInTimeSorter) {
//                storage.setSorter(SensorEarliestValueTimeSorter(), storage.isAscending)
//            }
//            enableDetectLogicalSensorNetIn = false
//            //enableDetectLogicalSensorValueUpdate = false
//            //enableDetectSensorInfoHistoryValueReceive = true
//            //enableDetectMeasurementHistoryValueReceive = true
//            enableDetectMeasurementDynamicValueUpdate = false
//            enableDetectMeasurementHistoryValueUpdate = true
//            getBaseActivity().dataPrepareService.sensorHistoryDataAccessor.importSensorsWithEarliestValue(this, false)
//        }
//        storage.refresh(null, this)
//    }

    override fun onMissionFinished(result: Boolean) {
        if (!result) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.import_measurement_earliest_value_failed)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager,
                    "import_measurement_earliest_value_failed")
        }
    }

//    override fun onDestroy() {
//        //释放配置资源
//        adapter.warnProcessor = null
//        SensorInfoAdapter.warnProcessor = null
//        super.onDestroy()
//    }

    override fun onLogicalSensorNetIn(sensor: LogicalSensor) {
        if (sensor.mainMeasurement.hasHistoryValue()) {
            val onlyAllowedHistoryValue = storage.getFilter(FILTER_ID_DATA_SOURCE) is SensorOnlyHasHistoryValueFilter
            val position = storage.find(sensor, !onlyAllowedHistoryValue, false)
            if (position != -1) {
                if (storage.removeAt(position)) {
                    adapter.notifyItemRemoved(position)
                }
            }
            if (!onlyAllowedHistoryValue) {
                addSensorAndNotify(sensor)
            }
        } else {
            addSensorAndNotify(sensor)
        }
//        if (sensor.practicalMeasurement.hasHistoryValue() && storage.contains(sensor, false)) {
//            storage.resort(this)
//        } else {
//            addSensorAndNotify(sensor)
//        }
    }

    private fun addSensorAndNotify(sensor: LogicalSensor) {
        //根据设置有序添加
        val position = storage.add(sensor)
        if (position != -1) {
            adapter.notifyItemInserted(position)
            processSensorInsertAtTop(rv_sensors, position)
        }
    }

//    override fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, measurementValuePosition: Int) {
//        val position = storage.find(sensor)
//        if (position >= 0) {
//            adapter.notifyItemChanged(position, DataBrowseLogicalSensorAdapter.UPDATE_TYPE_VALUE_CHANGED)
//            logicalSensorInfoDialog?.notifyRealTimeValueChanged(sensor, measurementValuePosition)
//        }
//    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        val sensor = SensorManager.getLogicalSensor(measurement.id) ?: return
        val position = storage.find(sensor)
        if (position >= 0) {
            adapter.notifyItemChanged(position, DataBrowseLogicalSensorAdapter.UPDATE_TYPE_VALUE_CHANGED)
            //logicalSensorInfoDialog?.notifyRealTimeValueChanged(sensor, valuePosition)
        }
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (valuePosition == 0) {
            if (measurement.historyValueContainer.interpretAddResult(valuePosition) == ValueContainer.NEW_VALUE_ADDED) {
                val sensor = SensorManager.getLogicalSensor(measurement.id) ?: return
                addSensorAndNotify(sensor)
            }
        }
//        else {
//            logicalSensorInfoDialog?.notifyMeasurementHistoryValueChanged(measurement, valuePosition)
//        }
    }

//    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
//        logicalSensorInfoDialog?.notifySensorInfoHistoryValueChanged(info, valuePosition)
//    }

//    override fun onMeasurementHistoryValueUpdate(sensor: LogicalSensor, measurementValuePosition: Int) {
//        if (measurementValuePosition == 0) {
//            if (sensor.practicalMeasurement.historyValueContainer.interpretAddResult(measurementValuePosition) == ValueContainer.NEW_VALUE_ADDED) {
//                addSensorAndNotify(sensor)
//            }
//        } else {
//            logicalSensorInfoDialog?.notifyMeasurementHistoryValueChanged(sensor, measurementValuePosition)
//        }
//    }
//
//    override fun onSensorInfoHistoryValueUpdate(sensor: PhysicalSensor, infoValuePosition: Int) {
//        logicalSensorInfoDialog?.notifySensorInfoHistoryValueChanged(sensor, infoValuePosition)
//    }

//    override fun onImportWarnProcessor() {
//        //super.onImportWarnProcessor()
//        //adapter.warnProcessor = processor
//        adapter.notifyItemRangeChanged(0, adapter.itemCount, UPDATE_TYPE_VALUE_CHANGED)
//        //logicalSensorInfoDialog?.notifyWarnProcessorLoaded()
//    }

    override fun onSortButtonClick() {
        SortDialog()
                .addSortType(R.id.rb_id, R.string.sensor_id, context)
                .addSortType(R.id.rb_time, R.string.timestamp, context)
                .addSortType(R.id.rb_name, R.string.name, context)
                .setDefaultSelectedId(when (storage.sorter) {
                    is SensorIdSorter -> R.id.rb_id
                    is SensorNameSorter -> R.id.rb_name
                    else -> R.id.rb_time
                })
                .show(childFragmentManager, DIALOG_TAG_SORT)
    }

    override fun onFilterButtonClick() {
        FilterDialog()
                .addFilterType(getString(R.string.sensor_type),
                        LogicalSensorTypeFilter.sensorTypeNames,
                        getSensorTypeFilterStates(storage.getFilter(FILTER_ID_SENSOR_TYPE)))
                .apply {
                    addDataSourceFilterType(this)
                    addSensorProtocolFilterType(this)
                }
                .show(childFragmentManager, DIALOG_TAG_FILTER)
    }

    private fun getSensorTypeFilterStates(filter: Filter<LogicalSensor>?): BooleanArray? {
        if (filter !is LogicalSensorTypeFilter || filter.selectedSensorTypeNos.isEmpty()) {
            return null
        }
        val result = BooleanArray(LogicalSensorTypeFilter.sensorTypeNames.size)
        for (selectedSensorTypeNo in filter.selectedSensorTypeNos) {
            result[selectedSensorTypeNo] = true
        }
        return result
    }

    override fun onSortTypeChanged(checkedId: Int, isAscending: Boolean) {
        storage.setSorter(when (checkedId) {
            R.id.rb_time -> SensorNetInTimeSorter()
            R.id.rb_id -> SensorIdSorter()
            R.id.rb_name -> SensorNameSorter()
            else -> SensorNetInTimeSorter()
        }, isAscending, this)
    }

    override fun onFilterChange(dialog: FilterDialog, hasFilters: BooleanArray, checkedFilterEntryValues: Array<out MutableList<Int>>) {
        //筛选类型
        if (hasFilters[0]) {
            storage.addFilter(FILTER_ID_SENSOR_TYPE, LogicalSensorTypeFilter(checkedFilterEntryValues[0]))
        } else {
            storage.removeFilter(FILTER_ID_SENSOR_TYPE)
        }

        //筛选数据源
        if (hasFilters[1]) {
            storage.addFilter(FILTER_ID_DATA_SOURCE,
                    if (checkedFilterEntryValues[1][0] == 0) {
                        enableDetectSensorInfoHistoryValueUpdate = false
                        enableDetectMeasurementHistoryValueUpdate = false
                        SensorHasRealTimeValueFilter()
                    } else {
                        enableDetectSensorInfoHistoryValueUpdate = true
                        enableDetectMeasurementHistoryValueUpdate = true
                        SensorOnlyHasHistoryValueFilter()
                    })
        } else {
            enableDetectSensorInfoHistoryValueUpdate = true
            enableDetectMeasurementHistoryValueUpdate = true
            storage.removeFilter(FILTER_ID_DATA_SOURCE)
        }

        //筛选协议
        if (hasFilters[2]) {
            storage.addFilter(FILTER_ID_SENSOR_PROTOCOL,
                    if (checkedFilterEntryValues[2][0] == 0) {
                        EsbProtocolFilter()
                    } else {
                        BleProtocolFilter()
                    })
        } else {
            storage.removeFilter(FILTER_ID_SENSOR_PROTOCOL)
        }
        storage.reFiltrate(this)
    }

    override fun onSearchButtonClick() {
        val dialog = SearchDialog()
        val filter = storage.getFilter(FILTER_ID_SENSOR_INFO)
        if (filter is LogicalSensorInfoFilter) {
            dialog.setContent(filter.keyWord)
        }
        dialog.show(childFragmentManager, DIALOG_TAG_SEARCH)
    }

    override fun onSearch(target: String?) {
        if (target.isNullOrEmpty()) {
            storage.removeFilter(FILTER_ID_SENSOR_INFO)
        } else {
            storage.addFilter(FILTER_ID_SENSOR_INFO, LogicalSensorInfoFilter(target))
        }
        storage.reFiltrate(this)
    }
}