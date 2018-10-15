package com.weisi.tool.wsnbox.fragment.browse

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.data.Storage
import com.cjq.lib.weisi.iot.*
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration
import com.cjq.tool.qbox.ui.dialog.ConfirmDialog
import com.cjq.tool.qbox.ui.dialog.FilterDialog
import com.cjq.tool.qbox.ui.dialog.SortDialog
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.browse.DataBrowseDeviceNodeAdapter
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.bean.filter.DeviceNodeInfoFilter
import com.weisi.tool.wsnbox.bean.sorter.DeviceNameSorter
import com.weisi.tool.wsnbox.bean.warner.processor.CommonWarnProcessor
import com.weisi.tool.wsnbox.dialog.LogicalSensorInfoDialog
import com.weisi.tool.wsnbox.dialog.SensorInfoDialog
import com.weisi.tool.wsnbox.io.database.SensorDatabase
import com.weisi.tool.wsnbox.processor.accessor.SensorHistoryDataAccessor
import com.weisi.tool.wsnbox.service.DataPrepareService
import kotlinx.android.synthetic.main.fragment_device_node.view.*

/**
 * Created by CJQ on 2018/5/29.
 */
class DeviceNodeFragment : DataBrowseFragment<Device, DataBrowseDeviceNodeAdapter>(), SensorHistoryDataAccessor.OnMissionFinishedListener {

    private lateinit var rvDevices: RecyclerView
    private var logicalSensorInfoDialog: LogicalSensorInfoDialog? = null
    private val devices = mutableListOf<Device>()
    private var realTime = true
    private val deviceFinder = Storage.Comparator<Device, PracticalMeasurement>() { device, measurement ->
        for (i in 0 until device.nodes.size) {
            if (device.nodes[i].measurement === measurement) {
                return@Comparator true
            }
        }
        false
    }

    override fun onCreateStorage(): Storage<Device> {
        val s = Storage<Device>() { elements, filters ->
            if (filters == null) {
                elements.addAll(devices)
            } else {
                var i = 0;
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

    override fun onRecoverInformationDialog(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        logicalSensorInfoDialog = childFragmentManager
                ?.findFragmentByTag(SensorInfoDialog.TAG) as LogicalSensorInfoDialog?
                ?: return
    }

    override fun onInitView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_device_node, null)
    }

    override fun onInitAdapter(view: View, storage: Storage<Device>): DataBrowseDeviceNodeAdapter {
        //初始化RecycleView
        rvDevices = view.rv_devices
        rvDevices.layoutManager = LinearLayoutManager(context)
        rvDevices.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_small), true))
        rvDevices.addOnItemTouchListener(object : SimpleRecyclerViewItemTouchListener(rvDevices) {
            override fun onItemClick(v: View, position: Int) {
                if (v.tag is Int) {
                    val index = v.tag as Int
                    val measurement = adapter.getItemByPosition(position).nodes[index].measurement
                    if (measurement.id.isPracticalMeasurement) {
                        val sensor = SensorManager.getLogicalSensor(measurement.id) ?: return
                        if (logicalSensorInfoDialog == null) {
                            logicalSensorInfoDialog = LogicalSensorInfoDialog()
                        }
                        logicalSensorInfoDialog!!.show(childFragmentManager,
                                sensor,
                                isRealTime())
                    } else {
                        val dialog = ConfirmDialog()
                        dialog.setTitle(R.string.virtual_measurement_info_not_supported)
                        dialog.show(childFragmentManager, "vmins")
                    }
                }
            }
        }.setMinRangeEnable(true))

        //创建DataBrowseSensorAdapter
        val sensorAdapter = DataBrowseDeviceNodeAdapter(storage)
        rvDevices.adapter = sensorAdapter
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
            ImportDevicesTask().execute(id)
        } else {
            showDeviceNotConfigured()
        }
    }

    private inner class ImportDevicesTask : AsyncTask<Long, Device, Boolean>() {

        private var deviceCount = 0;

        override fun doInBackground(vararg params: Long?): Boolean {
            return SensorDatabase.importDevices(params!![0] as Long) {
                publishProgress(it)
            }
        }

        override fun onProgressUpdate(vararg device: Device?) {
            if (device.isEmpty() || device[0] === null) {
                return
            }
            ++deviceCount
            devices.add(device[0]!!)
            val position = storage.add(device[0]!!)
            if (position >= 0) {
                adapter.notifyItemInserted(position)
            }
        }

        override fun onPostExecute(result: Boolean?) {
            if (!result!!) {
                val dialog = ConfirmDialog()
                dialog.setTitle(R.string.import_devices_failed)
                dialog.setDrawCancelButton(false)
                dialog.show(childFragmentManager,
                        "import_devices_failed")
            } else if (deviceCount == 0) {
                showDeviceNotConfigured()
            }
        }
    }

    private fun showDeviceNotConfigured() {
        val dialog = ConfirmDialog()
        dialog.setTitle(R.string.device_not_configured)
        dialog.setDrawCancelButton(false)
        dialog.show(childFragmentManager,
                "device_not_configured")
    }

    override fun isRealTime(): Boolean {
        return realTime
    }

    override fun onDestroy() {
        DataBrowseDeviceNodeAdapter.Delegate.warnProcessor = null
        super.onDestroy()
    }

    override fun onDataSourceChange(realTime: Boolean) {
        super.onDataSourceChange(realTime)
        val service = getBaseActivity().dataPrepareService ?: return
        this.realTime = realTime
        DataBrowseDeviceNodeAdapter.Delegate.realTime = realTime
        val transfer = service.dataTransferStation
        transfer.enableDetectPhysicalSensorNetIn = false
        transfer.enableDetectPhysicalSensorValueUpdate = false
        transfer.enableDetectLogicalSensorNetIn = false
        if (realTime) {
            transfer.enableDetectLogicalSensorValueUpdate = true
            transfer.enableDetectMeasurementHistoryValueReceive = false
            transfer.enableDetectSensorInfoHistoryValueReceive = false
        } else {
            transfer.enableDetectLogicalSensorValueUpdate = false
            transfer.enableDetectSensorInfoHistoryValueReceive = true
            transfer.enableDetectMeasurementHistoryValueReceive = true
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
                    "import_device_earliest_value_failed")
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

    override fun onLogicalSensorDynamicValueUpdate(sensor: LogicalSensor, valuePosition: Int) {
        notifyNodeValueUpdate(sensor, valuePosition)
    }

    private fun notifyNodeValueUpdate(sensor: LogicalSensor, valuePosition: Int) {
        notifyNodeValueUpdateToAdapter(sensor.id)
        notifyNodeValueUpdateToDialog(sensor, valuePosition)
    }

    private fun notifyNodeValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        notifyNodeValueUpdateToAdapter(measurement.id)
        notifyNodeValueUpdateToDialog(measurement, valuePosition)
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

    private fun notifyNodeValueUpdateToDialog(sensor: LogicalSensor, valuePosition: Int) {
        logicalSensorInfoDialog?.notifyRealTimeValueChanged(sensor, valuePosition)
    }

    private fun notifyNodeValueUpdateToDialog(measurement: PracticalMeasurement, valuePosition: Int) {
        logicalSensorInfoDialog ?: return
        val sensor = SensorManager.getLogicalSensor(measurement.id) ?: return
        notifyNodeValueUpdateToDialog(sensor, valuePosition)
    }

    //private fun find

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        if (valuePosition == 0) {
            notifyNodeValueUpdate(measurement, valuePosition)
        } else {
            //logicalSensorInfoDialog?.notifyRealTimeValueChanged(sensor, valuePosition)
            notifyNodeValueUpdateToDialog(measurement, valuePosition)
        }
    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, infoValuePosition: Int) {
        logicalSensorInfoDialog?.notifySensorInfoHistoryValueChanged(info, infoValuePosition)
    }

    override fun onInitSortDialog(dialog: SortDialog): SortDialog {
        return dialog
                .addSortType(R.id.rb_name, R.string.name, context)
                .setDefaultSelectedId(R.id.rb_time)
    }

    override fun onSortTypeChanged(checkedId: Int, isAscending: Boolean) {
        storage.setSorter(DeviceNameSorter(), isAscending, this)
    }

    override fun onSearch(target: String?) {
        if (TextUtils.isEmpty(target)) {
            storage.removeFilter(FILTER_ID_SENSOR_INFO)
        } else {
            storage.addFilter(FILTER_ID_SENSOR_INFO, DeviceNodeInfoFilter(target!!))
        }
        storage.reFiltrate(this)
    }

    override fun onImportWarnProcessor(processor: CommonWarnProcessor<View>) {
        super.onImportWarnProcessor(processor)
        DataBrowseDeviceNodeAdapter.Delegate.warnProcessor = processor
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
        logicalSensorInfoDialog?.notifyWarnProcessorLoaded()
    }

    override fun onFilterButtonClick(dialog: FilterDialog?): FilterDialog? {
        val d = ConfirmDialog()
        d.setTitle(R.string.device_mode_no_filters)
        d.setDrawCancelButton(false)
        d.show(childFragmentManager, "device_mode_no_filters")
        return null
    }
}