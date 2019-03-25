package com.weisi.tool.wsnbox.fragment.demo

import android.os.Bundle
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.PhysicalSensor
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.fragment.BaseFragment
import com.weisi.tool.wsnbox.processor.transfer.DataTransferStation
import com.weisi.tool.wsnbox.service.DataPrepareService
import com.weisi.tool.wsnbox.util.NullHelper

open class DemonstrateFragment : BaseFragment(), DataTransferStation.Detector {

    override var enableDetectPhysicalSensorNetIn = false
    override var enableDetectLogicalSensorNetIn = false
    override var enableDetectMeasurementHistoryValueUpdate = false
    override var enableDetectMeasurementDynamicValueUpdate = false
    override var enableDetectSensorInfoHistoryValueUpdate = false
    override var enableDetectSensorInfoDynamicValueUpdate = false

    private val ARGUMENT_KEY_DEVICES = "devices"
    open val titleRes = R.string.product_display
    protected var devices: List<Device> by NullHelper.readonlyNotNull()
    private val measurementIds = HashSet<Long>()

    fun initDevices(devices: List<Device>) {
        if (!measurementIds.isEmpty()) {
            return
        }
        this.devices = devices
        devices.forEach { device ->
            measurementIds.addAll(device.nodes.map { node ->
                node.measurement.id.id
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            initDevices(savedInstanceState.getParcelableArrayList(ARGUMENT_KEY_DEVICES) ?: listOf())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(ARGUMENT_KEY_DEVICES, ArrayList(devices))
    }

    override fun onServiceConnectionCreate(service: DataPrepareService) {
        service.dataTransferStation.register(this)
        //registerMeasurements(service)
        //service.dataTransferStation.enableDetectMeasurementRealTimeValueUpdate = DataTransferStation.MODE_CHECK_ATTENTION
        //Log.d(Tag.LOG_TAG_D_TEST, "fragment onServiceConnectionCreate")
    }

//    private fun registerMeasurements(service: DataPrepareService) {
//        devices.forEach { device ->
//            device.nodes.forEach { node ->
//                service.dataTransferStation.payAttentionToMeasurement(node.measurement)
//            }
//        }
//    }

    override fun onServiceConnectionStart(service: DataPrepareService) {
        //registerMeasurements(service)
        //Log.d(Tag.LOG_TAG_D_TEST, "fragment onServiceConnectionStart")
        //enableDetectMeasurementRealTimeValueUpdate = true
        enableDetectMeasurementDynamicValueUpdate = true
    }

    override fun onServiceConnectionStop(service: DataPrepareService) {
        //unregisterMeasurements(service)
        //Log.d(Tag.LOG_TAG_D_TEST, "fragment onServiceConnectionStop")
        //enableDetectMeasurementRealTimeValueUpdate = false
        enableDetectMeasurementDynamicValueUpdate = false
    }

//    private fun unregisterMeasurements(service: DataPrepareService) {
//        devices.forEach { device ->
//            device.nodes.forEach { node ->
//                service.dataTransferStation.payNoAttentionToMeasurement(node.measurement)
//            }
//        }
//    }

    override fun onServiceConnectionDestroy(service: DataPrepareService) {
        //Log.d(Tag.LOG_TAG_D_TEST, "fragment onServiceConnectionDestroy")
        //service.dataTransferStation.enableDetectMeasurementRealTimeValueUpdate = DataTransferStation.MODE_NO_CHECK
        service.dataTransferStation.unregister(this)
    }

    override fun onPhysicalSensorNetIn(sensor: PhysicalSensor) {
    }

    override fun onLogicalSensorNetIn(sensor: LogicalSensor) {
    }

    override fun onSensorInfoHistoryValueUpdate(info: Sensor.Info, valuePosition: Int) {
    }

    override fun onSensorInfoDynamicValueUpdate(info: Sensor.Info, valuePosition: Int) {
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
    }

    override fun onMeasurementHistoryValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
    }

    override fun enableUpdateMeasurementValue(measurementId: Long): Boolean {
//        devices.forEachIndexed { _, device ->
//            device.nodes.forEach { node ->
//                if (node.measurement.id.id == measurementId) {
//                    return true
//                }
//            }
//        }
        return measurementIds.contains(measurementId)
    }
}