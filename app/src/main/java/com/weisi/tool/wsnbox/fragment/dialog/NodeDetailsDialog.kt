package com.weisi.tool.wsnbox.fragment.dialog

import android.os.Bundle
import com.cjq.lib.weisi.iot.LogicalSensor
import com.cjq.lib.weisi.iot.SensorManager
import com.weisi.tool.wsnbox.bean.data.Node
import com.weisi.tool.wsnbox.util.NullHelper

class NodeDetailsDialog : LogicalSensorDetailsDialog() {

    private val ARGUMENT_KEY_NODE = "node"
    private var node: Node by NullHelper.readonlyNotNull()

    override fun init(sensor: LogicalSensor) {
        throw UnsupportedOperationException("please use init(node: Node) for instead")
    }

    fun init(node: Node) {
        this.node = node
        val sensor = SensorManager.getLogicalSensor(node.measurement.id) ?: throw IllegalArgumentException("node has unsupported measurement(id: ${node.measurement.id})")
        super.init(sensor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            node = it.getParcelable(ARGUMENT_KEY_NODE) ?: throw IllegalArgumentException("node is missing")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ARGUMENT_KEY_NODE, node)
        super.onSaveInstanceState(outState)
    }

    override fun getObjectLabel(): String {
        return node.name ?: super.getObjectLabel()
    }
}