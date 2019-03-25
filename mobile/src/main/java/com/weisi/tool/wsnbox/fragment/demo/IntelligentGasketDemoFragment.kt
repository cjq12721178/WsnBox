package com.weisi.tool.wsnbox.fragment.demo

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cjq.lib.weisi.iot.PracticalMeasurement
import com.cjq.lib.weisi.iot.SensorManager
import com.cjq.lib.weisi.iot.Warner
import com.weisi.tool.wsnbox.R
import com.weisi.tool.wsnbox.bean.data.Node
import com.weisi.tool.wsnbox.bean.warner.CommonSingleRangeWarner
import com.weisi.tool.wsnbox.fragment.dialog.PhysicalSensorDetailsDialog
import com.weisi.tool.wsnbox.view.IntelligentGasketView
import kotlinx.android.synthetic.main.fragment_intelligent_gasket_demo.view.*



class IntelligentGasketDemoFragment : DemonstrateFragment() {

    private val SOUND_NO_WARNING = 1


    override val titleRes = R.string.intelligent_gasket_demo
    private lateinit var gaskets: Array<IntelligentGasketView>
    private val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        SoundPool.Builder().setMaxStreams(1).build()
    } else {
        SoundPool(1, AudioManager.STREAM_MUSIC, 0)
    }
    private var warningVolume = 1.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Log.d(Tag.LOG_TAG_D_TEST, "fragment onCreateView")
        initWarning()
        val result = inflater.inflate(R.layout.fragment_intelligent_gasket_demo, null)
        gaskets = arrayOf(result.ig1, result.ig2, result.ig3, result.ig4)
        devices[0].nodes.forEachIndexed { index, node ->
            gaskets[index].let { gasket ->
                gasket.setLabel(node.name ?: node.measurement.name)
                gasket.setOnClickListener {
                    val dialog = PhysicalSensorDetailsDialog()
                    dialog.init(SensorManager.getPhysicalSensor(node.measurement.id.address))
                    dialog.show(childFragmentManager, "dddd")
                }
                node.measurement.configuration.warner?.let { warner ->
                    if (warner is CommonSingleRangeWarner) {
                        gasket.setLimit(warner.lowLimit, warner.highLimit)
                    }
                }
                updateGasketRealTimeValue(gasket, node)
            }
        }
        return result
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
        gaskets.forEachIndexed { index, gasket ->
            if (updateGasketRealTimeValue(gasket, devices[0].nodes[index]) != Warner.RESULT_NORMAL) {
                soundPool.play(SOUND_NO_WARNING, warningVolume, warningVolume, 1, 0, 1.0f)
            }
        }
    }

    private fun updateGasketRealTimeValue(gasket: IntelligentGasketView, node: Node): Int {
        gasket.realTimeValue = node.measurement.realTimeValue?.rawValue
        return node.measurement.testRealTimeValue()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.stop(SOUND_NO_WARNING)
        soundPool.release()
    }
}