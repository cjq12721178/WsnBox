package com.weisi.tool.wsnbox.fragment.demo

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.Warner
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.adapter.demo.IntelligentGasketDemoAdapter
import com.weisi.tool.wsnbox.bean.data.Device
import com.weisi.tool.wsnbox.bean.data.Node
import com.weisi.tool.wsnbox.util.NullHelper
import kotlinx.android.synthetic.main.fragment_intelligent_gasket_demo.view.*


class IntelligentGasketDemoFragment : DemonstrateFragment() {

    private val SOUND_NO_WARNING = 1

    override val titleRes = R.string.intelligent_gasket_demo
    //private lateinit var vGaskets: Array<IntelligentGasketView>
    private val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        SoundPool.Builder().setMaxStreams(1).build()
    } else {
        SoundPool(1, AudioManager.STREAM_MUSIC, 0)
    }
    private var warningVolume = 1.0f
    private var gaskets by NullHelper.readonlyNotNull<List<Node>>()
    private var adapter by NullHelper.readonlyNotNull<IntelligentGasketDemoAdapter>()

    override fun initDevices(devices: List<Device>): Boolean {
        if (!super.initDevices(devices)) {
            return false
        }
        val nodes = ArrayList<Node>()
        devices.forEach() { device ->
            device.nodes.forEach() { node ->
                if (node.measurement.id.dataTypeAbsValue == 0x60
                        && node.measurement.configuration.warner is DisplayMeasurement.SingleRangeWarner) {
                    nodes.add(node)
                }
            }
        }
        gaskets = nodes.toList()
        return nodes.isNotEmpty()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Log.d(Tag.LOG_TAG_D_TEST, "fragment onCreateView")
        initWarning()
//        val result = inflater.inflate(R.layout.fragment_intelligent_gasket_demo, null)
//        vGaskets = arrayOf(result.ig1, result.ig2, result.ig3, result.ig4)
//        devices[0].nodes.forEachIndexed { index, node ->
//            vGaskets[index].let { gasket ->
//                gasket.setLabel(node.name ?: node.measurement.name)
//                gasket.setOnClickListener {
//                    val dialog = PhysicalSensorDetailsDialog()
//                    dialog.init(SensorManager.getPhysicalSensor(node.measurement.id.address))
//                    dialog.show(childFragmentManager, "dddd")
//                }
//                node.measurement.configuration.warner?.let { warner ->
//                    if (warner is CommonSingleRangeWarner) {
//                        gasket.setLimit(warner.lowLimit, warner.highLimit)
//                    }
//                }
//                updateGasketRealTimeValue(gasket, node)
//            }
//        }
//        return result
        val view = inflater.inflate(R.layout.fragment_intelligent_gasket_demo, null)
        adapter = IntelligentGasketDemoAdapter(context!!, gaskets)
        view.vp_gasket_groups.adapter = adapter
        return view
    }

    private fun initWarning() {
        soundPool.load(context, R.raw.warning, SOUND_NO_WARNING)
        val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager? ?: return
        // 获取当前音量
        val streamVolumeCurrent = am
                .getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 获取系统最大音量
        val streamVolumeMax = am
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 计算得到播放音量
        warningVolume = streamVolumeCurrent / streamVolumeMax
    }

    override fun onMeasurementDynamicValueUpdate(measurement: PracticalMeasurement, valuePosition: Int) {
        adapter.updateGasketData(measurement)
//        vGaskets.forEachIndexed { index, gasket ->
//            if (updateGasketRealTimeValue(gasket, devices[0].nodes[index]) != Warner.RESULT_NORMAL) {
//                soundPool.play(SOUND_NO_WARNING, warningVolume, warningVolume, 1, 0, 1.0f)
//            }
//        }
    }

//    private fun updateGasketRealTimeValue(gasket: IntelligentGasketView, node: Node): Int {
//        gasket.realTimeValue = node.measurement.realTimeValue?.rawValue
//        return node.measurement.testRealTimeValue()
//    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.stop(SOUND_NO_WARNING)
        soundPool.release()
    }

    override fun enableUpdateMeasurementValue(measurementId: Long): Boolean {
        repeat(gaskets.size) {
            if (gaskets[it].measurement.id.id == measurementId) {
                return true
            }
        }
        return false
    }

    override fun onValueTestResult(info: Sensor.Info, measurement: PracticalMeasurement, value: DisplayMeasurement.Value, warnResult: Int): Boolean {
        if (enableUpdateMeasurementValue(measurement.id.id)) {
            if (warnResult != Warner.RESULT_NORMAL) {
                soundPool.play(SOUND_NO_WARNING, warningVolume, warningVolume, 1, 0, 1.0f)
                return true
            }
        }
        return false
    }
}