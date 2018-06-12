package com.weisi.tool.wsnbox.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cjq.lib.weisi.data.Filter
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.lib.weisi.iot.ValueContainer
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.FilterDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.DataBrowseLogicalSensorAdapter
import com.weisi.tool.wsnbox.adapter.DataBrowseLogicalSensorAdapter.Companion.UPDATE_TYPE_VALUE_CHANGED
import com.weisi.tool.wsnbox.adapter.SensorInfoAdapter
import com.weisi.tool.wsnbox.bean.filter.*
import com.weisi.tool.wsnbox.bean.sorter.SensorEarliestValueTimeSorter
import com.weisi.tool.wsnbox.bean.sorter.SensorIdSorter
import com.weisi.tool.wsnbox.bean.sorter.SensorNameSorter
import com.weisi.tool.wsnbox.bean.sorter.SensorNetInTimeSorter
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import com.weisi.tool.wsnbox.dialog.LogicalSensorInfoDialog
import com.weisi.tool.wsnbox.dialog.SensorInfoDialog
import com.weisi.tool.wsnbox.processor.accessor.SensorHistoryDataAccessor
import com.weisi.tool.wsnbox.service.DataPrepareService
import kotlinx.android.synthetic.main.lh_logical_sensor.view.*

/**
 * Created by CJQ on 2018/5/29.
 */
class LogicalSensorFragment : DataBrowseFragment<LogicalSensor, DataBrowseLogicalSensorAdapter>(),
        View.OnClickListener,
        SensorHistoryDataAccessor.OnMissionFinishedListener {

    private lateinit var rvSensors: RecyclerView
    private var logicalSensorInfoDialog: LogicalSensorInfoDialog? = null

    override fun onCreateStorage(): Storage<LogicalSensor> {
        val s = Storage<LogicalSensor>() { elements, filters ->
            SensorManager.getSensors(elements, filters, LogicalSensor::class.java)
        }
        s.addFilter(FILTER_ID_DATA_SOURCE, SensorUseForRealTimeFilter())
        s.setSorter(SensorNetInTimeSorter(), false)
        return s
    }

    override fun onRecoverInformationDialog(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        logicalSensorInfoDialog = childFragmentManager
                ?.findFragmentByTag(SensorInfoDialog.TAG) as LogicalSensorInfoDialog?
                ?: return
        //logicalSensorInfoDialog?.realTime = isRealTime()
        //logicalSensorInfoDialog?.sensor = getSavedSelectedSensor(savedInstanceState) ?: return
        //getBaseActivity().dataPrepareService.dataTransferStation.payAttentionToSensor(logicalSensorInfoDialog!!.sensor!!)
    }

//    private fun getSavedSelectedSensor(savedInstanceState: Bundle): LogicalSensor? {
//        val selectedId = savedInstanceState.getLong(ARGUMENT_KEY_SELECTED_SENSOR_ID)
//        return if (selectedId == -1L) {
//            null
//        } else SensorManager.getLogicalSensor(selectedId, false)
//    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_logical_sensor, null)
    }

    override fun onInitAdapter(view: View, storage: Storage<LogicalSensor>): DataBrowseLogicalSensorAdapter {
        //绑定事件
        view.tv_measurement_name_id.setOnClickListener(this)

        //初始化RecycleView
        rvSensors = view.findViewById<RecyclerView>(R.id.rv_sensors)
        rvSensors.layoutManager = LinearLayoutManager(context)
        rvSensors.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_small), true))
        rvSensors.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(rvSensors) {
            override fun onItemClick(v: View, position: Int) {
                val sensor = adapter.getItemByPosition(position)
                if (logicalSensorInfoDialog == null) {
                    logicalSensorInfoDialog = LogicalSensorInfoDialog()
                }
                logicalSensorInfoDialog!!.show(childFragmentManager,
                        sensor,
                        isRealTime())
            }
        })

        //创建DataBrowseSensorAdapter
        val sensorAdapter = DataBrowseLogicalSensorAdapter(storage, isRealTime())
        //sensorAdapter.warnProcessor = getBaseActivity().warnProcessor
        rvSensors.adapter = sensorAdapter
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

    override fun onServiceConnectionStart(service: DataPrepareService) {
        if (isRealTime()) {
            service.dataTransferStation.enableDetectLogicalSensorValueUpdate = true
        }
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        if (isRealTime()) {
            service.dataTransferStation.enableDetectLogicalSensorValueUpdate = false
        }
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
        if (isRealTime()) {
            service.dataTransferStation.enableDetectLogicalSensorNetIn = false
        }
        super.onServiceConnectionDestroy(service)
    }

    override fun onDataSourceChange(realTime: Boolean) {
        super.onDataSourceChange(realTime)
        val service = getBaseActivity().dataPrepareService ?: return
        adapter.realTime = realTime
        val transfer = service.dataTransferStation
        transfer.enableDetectPhysicalSensorNetIn = false
        transfer.enableDetectPhysicalSensorValueUpdate = false
        if (realTime) {
            storage.addFilter(FILTER_ID_DATA_SOURCE, SensorUseForRealTimeFilter())
            if (storage.sorter is SensorEarliestValueTimeSorter) {
                storage.setSorter(SensorNetInTimeSorter(), storage.isDescend)
            }
            transfer.enableDetectLogicalSensorNetIn = true
            transfer.enableDetectLogicalSensorValueUpdate = true
            transfer.enableDetectLogicalSensorHistoryValueReceive = false
            transfer.enableDetectPhysicalSensorHistoryValueReceive = false
        } else {
            storage.addFilter(FILTER_ID_DATA_SOURCE, SensorWithHistoryValueFilter())
            if (storage.sorter is SensorNetInTimeSorter) {
                storage.setSorter(SensorEarliestValueTimeSorter(), storage.isDescend)
            }
            transfer.enableDetectLogicalSensorNetIn = false
            transfer.enableDetectLogicalSensorValueUpdate = false
            transfer.enableDetectPhysicalSensorHistoryValueReceive = true
            transfer.enableDetectLogicalSensorHistoryValueReceive = true
            getBaseActivity().dataPrepareService.sensorHistoryDataAccessor.importSensorsWithEarliestValue(this, false)
        }
        storage.refresh(null, this)
    }

    override fun onMissionFinished(result: Boolean) {
        if (!result) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.import_measurement_earliest_value_failed)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager,
                    "import_measurement_earliest_value_failed")
        }
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putInt(ARGUMENT_KEY_SELECTED_SENSOR_INDEX, adapter.selectedIndex)
//        outState.putLong(ARGUMENT_KEY_SELECTED_SENSOR_ID,
//                logicalSensorInfoDialog?.sensor?.id?.id ?: -1)
//    }

    override fun onDestroy() {
        //释放配置资源
        adapter.warnProcessor = null
        SensorInfoAdapter.warnProcessor = null
        super.onDestroy()
    }

    override fun onLogicalSensorNetIn(sensor: LogicalSensor) {
        addSensor(sensor)
    }

    private fun addSensor(sensor: LogicalSensor) {
        //根据设置有序添加
        val position = storage.add(sensor)
        if (position != -1) {
            adapter.notifyItemInserted(position)
        }
    }

    override fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, logicalPosition: Int) {
        //notifySensorValueUpdate(sensor, logicalPosition)
        val position = storage.find(sensor)
        if (position >= 0) {
            adapter.notifyItemChanged(position, DataBrowseLogicalSensorAdapter.UPDATE_TYPE_VALUE_CHANGED)
            logicalSensorInfoDialog?.notifyLogicalSensorRealTimeValueChanged(sensor, logicalPosition)
        }
    }

//    private fun notifySensorValueUpdate(sensor: LogicalSensor, valuePosition: Int) {
//        val position = storage.find(sensor)
//        if (position >= 0) {
//            adapter.notifyItemChanged(position, DataBrowseLogicalSensorAdapter.UPDATE_TYPE_VALUE_CHANGED)
//            logicalSensorInfoDialog?.notifyLogicalSensorRealTimeValueChanged(sensor, valuePosition)
//        }
//    }

    override fun onLogicalSensorHistoryValueUpdate(sensor: LogicalSensor, logicalPosition: Int) {
        if (logicalPosition == 0) {
            if (sensor.historyValueContainer.interpretAddResult(logicalPosition) == ValueContainer.NEW_VALUE_ADDED) {
                addSensor(sensor)
            }
        } else {
            logicalSensorInfoDialog?.notifyLogicalSensorHistoryValueChanged(sensor, logicalPosition)
        }
    }

    override fun onPhysicalSensorHistoryValueUpdate(sensor: PhysicalSensor, logicalPosition: Int) {
        logicalSensorInfoDialog?.notifyPhysicalSensorHistoryValueChanged(sensor, logicalPosition)
//        if (sensor.rawAddress != logicalSensorInfoDialog?.sensor?.id?.address) {
//            return
//        }
//        val value = sensor.getValueByContainerAddMethodReturnValue(sensor.historyValueContainer, logicalPosition) ?: return
//        var position = logicalSensorInfoDialog!!.sensor!!.historyValueContainer.findValuePosition(logicalPosition, value.timestamp)
//        if (position >= 0) {
//            position = - position - 1;
//        }
//        logicalSensorInfoDialog?.notifySensorValueChanged(logicalSensorInfoDialog!!.sensor!!, position)
    }

    override fun onImportWarnProcessor(processor: CommonWarnProcessor<View>) {
        super.onImportWarnProcessor(processor)
        adapter.warnProcessor = processor
        //SensorInfoAdapter.warnProcessor = processor
        adapter.notifyItemRangeChanged(0, adapter.itemCount, UPDATE_TYPE_VALUE_CHANGED)
        logicalSensorInfoDialog?.notifyWarnProcessorLoaded()
    }

    override fun onInitSortDialog(dialog: SortDialog): SortDialog {
        return dialog
                .addSortType(R.id.rb_id, R.string.sensor_id, context)
                .addSortType(R.id.rb_time, R.string.timestamp, context)
                .addSortType(R.id.rb_name, R.string.name, context)
                .setDefaultSelectedId(when (storage.sorter) {
                    is SensorIdSorter -> R.id.rb_id
                    is SensorNameSorter -> R.id.rb_name
                    else -> R.id.rb_time
                })
    }

    override fun onInitFilterDialog(dialog: FilterDialog): FilterDialog {
        return dialog
                .addFilterType(getString(R.string.sensor_protocol),
                        resources.getStringArray(R.array.usb_protocols),
                        when (storage.getFilter(FILTER_ID_SENSOR_PROTOCOL)) {
                            is BleProtocolFilter -> booleanArrayOf(false, true)
                            is EsbProtocolFilter -> booleanArrayOf(true, false)
                            else -> booleanArrayOf(false, false)
                        })
                .addFilterType(getString(R.string.sensor_type),
                        LogicalSensorTypeFilter.sensorTypeNames,
                        getSensorTypeFilterStates(storage.getFilter(FILTER_ID_SENSOR_TYPE)))
    }

    private fun getSensorTypeFilterStates(filter: Filter<LogicalSensor>?): BooleanArray? {
        if (filter !is LogicalSensorTypeFilter || filter.selectedSensorTypeNos?.isEmpty()) {
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
            R.id.rb_id -> SensorIdSorter()
            R.id.rb_name -> SensorNameSorter()
            R.id.rb_time -> if (isRealTime()) {
                SensorNetInTimeSorter()
            } else {
                SensorEarliestValueTimeSorter()
            }
            else -> SensorNetInTimeSorter()
        }, !isAscending, this)
    }

    override fun onFilterChange(dialog: FilterDialog?, hasFilters: BooleanArray?, checkedFilterEntryValues: Array<out MutableList<Int>>?) {
        //筛选协议
        if (hasFilters!![0]) {
            storage.addFilter(FILTER_ID_SENSOR_PROTOCOL,
                    if (checkedFilterEntryValues!![0][0] == 0) {
                        EsbProtocolFilter()
                    } else {
                        BleProtocolFilter()
                    })
        }
        //筛选类型
        if (hasFilters[1]) {
            storage.addFilter(FILTER_ID_SENSOR_TYPE, LogicalSensorTypeFilter(checkedFilterEntryValues!![1]))
        }
        storage.reFiltrate(this)
    }

    override fun onSearch(target: String?) {
        if (TextUtils.isEmpty(target)) {
            storage.removeFilter(FILTER_ID_SENSOR_INFO)
        } else {
            storage.addFilter(FILTER_ID_SENSOR_INFO, LogicalSensorInfoFilter(target!!))
        }
        storage.reFiltrate(this)
    }
}