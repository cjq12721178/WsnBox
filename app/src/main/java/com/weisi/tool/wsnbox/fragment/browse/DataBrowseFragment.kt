package com.weisi.tool.wsnbox.fragment.browse

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.data.Sorter
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.cjq.tool.qbox.ui.dialog.FilterDialog
import com.cjq.tool.qbox.ui.dialog.SearchDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.activity.DataBrowseActivity
import com.weisi.tool.wsnbox.bean.filter.BleProtocolFilter
import com.weisi.tool.wsnbox.bean.filter.EsbProtocolFilter
import com.weisi.tool.wsnbox.bean.filter.SensorHasRealTimeValueFilter
import com.weisi.tool.wsnbox.bean.filter.SensorOnlyHasHistoryValueFilter
import com.weisi.tool.wsnbox.fragment.BaseFragment
import com.weisi.tool.wsnbox.processor.transfer.DataTransferStation
import com.weisi.tool.wsnbox.service.DataPrepareService

/**
 * Created by CJQ on 2018/5/25.
 */
abstract class DataBrowseFragment<E, A : RecyclerViewBaseAdapter<E>> : BaseFragment(),
        DataTransferStation.Detector,
        Storage.OnFilterChangeListener,
        Storage.OnSortChangeListener<E>,
        SortDialog.OnSortTypeChangedListener,
        FilterDialog.OnFilterChangeListener,
        SearchDialog.OnSearchListener {

    companion object {
        protected const val ARGUMENT_KEY_STORAGE = "storage"
        @JvmStatic
        protected val FILTER_ID_DATA_SOURCE = 1
        @JvmStatic
        protected val FILTER_ID_SENSOR_PROTOCOL = 2
        @JvmStatic
        protected val FILTER_ID_SENSOR_TYPE = 3
        @JvmStatic
        protected val FILTER_ID_SENSOR_INFO = 4
        @JvmStatic
        protected val DIALOG_TAG_SORT = "sort"
        @JvmStatic
        protected val DIALOG_TAG_FILTER = "filter"
        @JvmStatic
        protected val DIALOG_TAG_SEARCH = "search"
        @JvmStatic
        val DIALOG_TAG_INFO = "info"
    }

    override var enableDetectPhysicalSensorNetIn = false
    override var enableDetectLogicalSensorNetIn = false
    override var enableDetectMeasurementHistoryValueUpdate = false
    override var enableDetectMeasurementDynamicValueUpdate = false
    override var enableDetectSensorInfoHistoryValueUpdate = false
    override var enableDetectSensorInfoDynamicValueUpdate = false

    protected lateinit var storage : Storage<E>
    protected lateinit var adapter: A

    override fun getBaseActivity(): DataBrowseActivity {
        return activity as DataBrowseActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        establishStorage(savedInstanceState)
        val view = onInitView(inflater, container, savedInstanceState)
        adapter = onInitAdapter(view, storage)
        return view
    }

    private fun establishStorage(savedInstanceState: Bundle?) {
        storage = savedInstanceState?.getParcelable(ARGUMENT_KEY_STORAGE)
                ?: onCreateStorage()
    }

    protected abstract fun onCreateStorage(): Storage<E>

    protected abstract fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    protected abstract fun onInitAdapter(view: View, storage: Storage<E>): A

    override fun onServiceConnectionStart(service: DataPrepareService) {
        service.dataTransferStation.register(this)
        storage.refresh(null, null)
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        service.dataTransferStation.unregister(this)
    }

    override fun onSortChange(newSorter: Sorter<E>?, newOrder: Boolean) {
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    override fun onSizeChange(previousSize: Int, currentSize: Int) {
        adapter.notifyDataSetChanged(previousSize)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ARGUMENT_KEY_STORAGE, storage)
    }

    override fun onPhysicalSensorNetIn(sensor: PhysicalSensor) {
    }

    override fun onLogicalSensorNetIn(sensor: LogicalSensor) {
    }

    override fun onSensorInfoDynamicValueUpdate(info: Sensor.Info, valuePosition: Int) {
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
    }

    fun onSensorConfigurationChanged() {
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    open fun onSortButtonClick() {
    }

    open fun onFilterButtonClick() {
    }

    open fun onSearchButtonClick() {
    }

    override fun onSortTypeChanged(checkedId: Int, isAscending: Boolean) {
    }

    override fun onFilterChange(dialog: FilterDialog, hasFilters: BooleanArray, checkedFilterEntryValues: Array<out MutableList<Int>>) {

    }

    override fun onSearch(target: String?) {
    }

    protected fun processSensorInsertAtTop(recyclerView: RecyclerView, itemPosition: Int) {
        if (itemPosition == 0 && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(-1)) {
            recyclerView.smoothScrollToPosition(0)
        }
    }

    protected fun addSensorProtocolFilterType(dialog: FilterDialog): FilterDialog {
        return dialog.addFilterType(getString(R.string.sensor_protocol),
                resources.getStringArray(R.array.usb_protocols),
                when (storage.getFilter(FILTER_ID_SENSOR_PROTOCOL)) {
                    is BleProtocolFilter -> booleanArrayOf(false, true)
                    is EsbProtocolFilter -> booleanArrayOf(true, false)
                    else -> booleanArrayOf(false, false)
                })
    }

    protected fun addDataSourceFilterType(dialog: FilterDialog): FilterDialog {
        return dialog.addFilterType(getString(R.string.data_source),
                arrayOf(getString(R.string.real_time), getString(R.string.history)),
                when (storage.getFilter(FILTER_ID_DATA_SOURCE)) {
                    is SensorHasRealTimeValueFilter -> booleanArrayOf(true, false)
                    is SensorOnlyHasHistoryValueFilter -> booleanArrayOf(false, true)
                    else -> booleanArrayOf(false, false)
                })
    }
}