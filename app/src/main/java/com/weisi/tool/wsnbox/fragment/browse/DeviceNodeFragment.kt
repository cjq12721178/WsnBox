package com.weisi.tool.wsnbox.fragment.browse

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.ID
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.SearchDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.browse.DataBrowseDeviceNodeAdapter
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.bean.filter.DeviceNodeInfoFilter
import com.weisi.tool.wsnbox.bean.sorter.DeviceNameSorter
import com.weisi.tool.wsnbox.fragment.dialog.NodeDetailsDialog
import com.weisi.tool.wsnbox.processor.accessor.SensorHistoryDataAccessor
import com.weisi.tool.wsnbox.processor.importer.DevicesOneByOneImporter
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.SafeAsyncTask
import kotlinx.android.synthetic.main.fragment_device_node.view.*

/**
 * Created by CJQ on 2018/5/29.
 */
class DeviceNodeFragment : DataBrowseFragment<Device, DataBrowseDeviceNodeAdapter>(), SensorHistoryDataAccessor.OnMissionFinishedListener, SafeAsyncTask.ResultAchiever<Boolean, Device> {

    private val devices = mutableListOf<Device>()

    override fun onCreateStorage(): Storage<Device> {
        val s = Storage<Device>() { elements, filters ->
            if (filters == null) {
                elements.addAll(devices)
            } else {
                var i = 0
                val size = devices.size
                while (i < size) {
                    if (filters.match(devices[i])) {
                        elements.add(devices[i])
                    }
                    ++i
                }
            }
        }
        s.setSorter(DeviceNameSorter(), true)
        return s
    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_device_node, null)
    }

    override fun onInitAdapter(view: View, storage: Storage<Device>): DataBrowseDeviceNodeAdapter {
        //初始化RecycleView
        view.rv_devices.layoutManager = LinearLayoutManager(context)
        view.rv_devices.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_small), true))
        view.rv_devices.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(view.rv_devices) {
            override fun onItemClick(v: View, position: Int) {
                if (v.tag is Int) {
                    val index = v.tag as Int
                    try {
                        val dialog = NodeDetailsDialog()
                        dialog.init(adapter.getItemByPosition(position).nodes[index])
                        dialog.show(childFragmentManager, DIALOG_TAG_INFO)
                    } catch (e: IllegalArgumentException) {
                        val dialog = ConfirmDialog()
                        dialog.setTitle(R.string.virtual_measurement_info_not_supported)
                        dialog.show(childFragmentManager, "vmins")
                    }
                }
            }
        }.setMinRangeEnable(true))

        //创建DataBrowseSensorAdapter
        val sensorAdapter = DataBrowseDeviceNodeAdapter(storage)
        view.rv_devices.adapter = sensorAdapter
        return sensorAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        importDevices()
        return view
    }

    private fun importDevices() {
        val id = getBaseActivity().baseApplication.settings.dataBrowseValueContainerConfigurationProviderId
        if (id > 0) {
            DevicesOneByOneImporter(this).execute(id)
        } else {
            showDeviceNotConfigured()
        }
    }

    override fun onProgressUpdate(values: Array<out Device>) {
        if (values.isEmpty()) {
            return
        }
        devices.add(values[0])
        val position = storage.add(values[0])
        if (position >= 0) {
            adapter.notifyItemInserted(position)
        }
    }

    override fun onResultAchieved(result: Boolean?) {
        if (isHidden) {
            return
        }
        if (result != true) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.import_devices_failed)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager,
                    "import_devices_failed")
        } else if (devices.isEmpty()) {
            showDeviceNotConfigured()
        }
    }

    private fun showDeviceNotConfigured() {
        val dialog = ConfirmDialog()
        dialog.setTitle(R.string.device_not_configured)
        dialog.setDrawCancelButton(false)
        dialog.show(childFragmentManager,
                "device_not_configured")
    }

    override fun onMissionFinished(result: Boolean) {
        if (!result) {
            val dialog = ConfirmDialog()
            dialog.setTitle(R.string.import_measurement_earliest_value_failed)
            dialog.setDrawCancelButton(false)
            dialog.show(childFragmentManager,
                    "import_device_earliest_value_failed")
        }
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        enableDetectMeasurementDynamicValueUpdate = true
        enableDetectMeasurementHistoryValueUpdate = true
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        notifyNodeValueUpdateToAdapter(measurement.id)
    }

    private fun notifyNodeValueUpdateToAdapter(id: ID) {
        var i = 0
        var j: Int
        val deviceSize = storage.size()
        var nodeSize: Int
        while (i < deviceSize) {
            nodeSize = storage[i].nodes.size
            j = 0
            while (j < nodeSize) {
                if (id == storage[i].nodes[j].measurement.id) {
                    adapter.notifyItemChanged(i, j)
                    return
                }
                ++j
            }
            ++i
        }
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (valuePosition == 0) {
            notifyNodeValueUpdateToAdapter(measurement.id)
        }
    }

    override fun onSortButtonClick() {
        SortDialog()
                .addSortType(R.id.rb_name, R.string.name, context)
                .setDefaultSelectedId(R.id.rb_name)
                .show(childFragmentManager, DIALOG_TAG_SORT)
    }

    override fun onSortTypeChanged(checkedId: Int, isAscending: Boolean) {
        storage.setSorter(DeviceNameSorter(), isAscending, this)
    }

    override fun onSearchButtonClick() {
        val dialog = SearchDialog()
        val filter = storage.getFilter(FILTER_ID_SENSOR_INFO)
        if (filter is DeviceNodeInfoFilter) {
            dialog.setContent(filter.keyWord)
        }
        dialog.show(childFragmentManager, DIALOG_TAG_SEARCH)
    }

    override fun onSearch(target: String?) {
        if (target.isNullOrEmpty()) {
            storage.removeFilter(FILTER_ID_SENSOR_INFO)
        } else {
            storage.addFilter(FILTER_ID_SENSOR_INFO, DeviceNodeInfoFilter(target))
        }
        storage.reFiltrate(this)
    }

    override fun onFilterButtonClick() {
        val d = ConfirmDialog()
        d.setTitle(R.string.device_mode_no_filters)
        d.setDrawCancelButton(false)
        d.show(childFragmentManager, "device_mode_no_filters")
    }
}