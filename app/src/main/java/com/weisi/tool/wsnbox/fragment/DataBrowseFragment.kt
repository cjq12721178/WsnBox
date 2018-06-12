package com.weisi.tool.wsnbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.data.Sorter
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter
import com.cjq.tool.qbox.ui.dialog.FilterDialog
import com.cjq.tool.qbox.ui.dialog.SearchDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.cjq.tool.qbox.ui.manager.OnDataSetChangedListener
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.activity.DataBrowseActivity
import com.weisi.tool.wsnbox.adapter.SensorInfoAdapter
import com.weisi.tool.wsnbox.bean.filter.SensorWithHistoryValueFilter
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import com.weisi.tool.wsnbox.processor.transfer.DataTransferStation
import com.weisi.tool.wsnbox.service.DataPrepareService

/**
 * Created by CJQ on 2018/5/25.
 */
open class DataBrowseFragment<E, A : RecyclerViewBaseAdapter<E>> : BaseFragment(),
        OnDataSetChangedListener,
        DataTransferStation.OnEventListener,
        Storage.OnFilterChangeListener,
        Storage.OnSortChangeListener<E>,
        SortDialog.OnSortTypeChangedListener,
        FilterDialog.OnFilterChangeListener,
        SearchDialog.OnSearchListener {

    companion object {
        @JvmStatic
        protected val ARGUMENT_KEY_SELECTED_SENSOR_INDEX = "selected_index"
        @JvmStatic
        protected val ARGUMENT_KEY_SELECTED_SENSOR_ADDRESS = "selected_sensor_addr"
        @JvmStatic
        protected val ARGUMENT_KEY_SELECTED_SENSOR_ID = "selected_sensor_id"
        @JvmStatic
        protected val ARGUMENT_KEY_STORAGE = "storage"
        @JvmStatic
        protected val FILTER_ID_DATA_SOURCE = 1
        @JvmStatic
        protected val FILTER_ID_SENSOR_PROTOCOL = 2
        @JvmStatic
        protected val FILTER_ID_SENSOR_TYPE = 3
        @JvmStatic
        protected val FILTER_ID_SENSOR_INFO = 4
        //@JvmStatic
        //protected var warnProcessor: CommonWarnProcessor<View>? = null
    }

    protected lateinit var storage : Storage<E>
    protected lateinit var adapter: A

    override fun getBaseActivity(): DataBrowseActivity {
        return activity as DataBrowseActivity;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        establishStorage(savedInstanceState)
        onRecoverInformationDialog(savedInstanceState)
        val view = onInitView(inflater, container, savedInstanceState)
        adapter = onInitAdapter(view, storage)
        return view
    }

    private fun establishStorage(savedInstanceState: Bundle?) {
        storage = savedInstanceState?.getParcelable(ARGUMENT_KEY_STORAGE)
                ?: onCreateStorage()
    }

    protected open fun onCreateStorage(): Storage<E> {
        throw NullPointerException("please override onCreateStorage for storage")
    }

    protected open fun onRecoverInformationDialog(savedInstanceState: Bundle?) {
    }

    protected open fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        throw NullPointerException("please override onInitView for view")
    }

    protected open fun onInitAdapter(view: View, storage: Storage<E>): A {
        throw NullPointerException("please override onInitAdapter for adapter")
    }

    override fun onDestroy() {
        SensorInfoAdapter.warnProcessor = null
        super.onDestroy()
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        service.dataTransferStation.register(this)
        onDataSourceChange(isRealTime())
    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
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

    override fun onPhysicalSensorDynamicValueUpdate(sensor: PhysicalSensor, logicalPosition: Int) {
    }

    override fun onLogicalSensorNetIn(sensor: LogicalSensor) {
    }

    override fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, logicalPosition: Int) {
    }

    override fun onPhysicalSensorHistoryValueUpdate(sensor: PhysicalSensor, logicalPosition: Int) {
    }

    override fun onLogicalSensorHistoryValueUpdate(sensor: LogicalSensor, logicalPosition: Int) {
    }

    override fun onDataSetChanged() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(isRealTime())
        processServiceConnection(true)
        performImportWarnProcessor()
    }

    protected fun setTitle(realTime: Boolean) {
        getBaseActivity().title = if (realTime) {
            getString(R.string.real_time) + getString(R.string.data_browse)
        } else {
            getString(R.string.history) + getString(R.string.data_browse)
        }
    }

    protected open fun isRealTime() = storage.getFilter(FILTER_ID_DATA_SOURCE) !is SensorWithHistoryValueFilter

    private fun performImportWarnProcessor() {
        val processor = getBaseActivity().warnProcessor ?: return
        onImportWarnProcessor(processor)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        processServiceConnection(!hidden)
        if (!hidden) {
            performImportWarnProcessor()
        }
    }

    private fun processServiceConnection(connected: Boolean) {
        val service = getBaseActivity().dataPrepareService ?: return
        if (connected) {
            onServiceConnectionCreate(service)
            onServiceConnectionStart(service)
        } else {
            onServiceConnectionStop(service)
            onServiceConnectionDestroy(service)
        }
    }

    open fun onImportWarnProcessor(processor: CommonWarnProcessor<View>) {
        SensorInfoAdapter.warnProcessor = processor
    }

    fun changeDataSource() {
        onDataSourceChange(!isRealTime())
    }

    protected open fun onDataSourceChange(realTime: Boolean) {
        setTitle(realTime)
    }

    open fun onSortButtonClick(dialog: SortDialog?) : SortDialog? {
        val d = dialog ?: onInitSortDialog(SortDialog())
        d.show(childFragmentManager, "sort_dialog")
        return d
    }

    protected open fun onInitSortDialog(dialog: SortDialog) : SortDialog {
        return  dialog
    }

    open fun onFilterButtonClick(dialog: FilterDialog?) : FilterDialog? {
        val d = dialog ?: onInitFilterDialog(FilterDialog())
        d.show(childFragmentManager, "filter_dialog")
        return d
    }

    protected open fun onInitFilterDialog(dialog: FilterDialog) : FilterDialog {
        return dialog
    }

    open fun onSearchButtonClick(dialog: SearchDialog?) : SearchDialog? {
        val d = dialog ?: onInitSearchDialog(SearchDialog())
        d.show(childFragmentManager, "search_dialog")
        return d
    }

    protected open fun onInitSearchDialog(dialog: SearchDialog) : SearchDialog {
        return dialog
    }

    override fun onSortTypeChanged(checkedId: Int, isAscending: Boolean) {
    }

    override fun onFilterChange(dialog: FilterDialog?, hasFilters: BooleanArray?, checkedFilterEntryValues: Array<out MutableList<Int>>?) {
    }

    override fun onSearch(target: String?) {
    }
}