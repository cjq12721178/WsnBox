package com.weisi.tool.wsnbox.processor.accessor

import android.content.Context
import com.weisi.tool.wsnbox.bean.configuration.Settings
import com.wsn.lib.wsb.protocol.BleSensorProtocol
import java.util.*

class IntelligentGasketSimulationDataAccess : SensorDynamicDataAccessor<BleSensorProtocol>(null) {

    private val timer = Timer()

    override fun onStartDataAccess(context: Context, settings: Settings, listener: OnStartResultListener) {
        timer.schedule(GenerateDataTask(0xD00001, 0x60, -10.0, 110.0), 0, 3000)
        timer.schedule(GenerateDataTask(0xD00001, 0x05, -10.0, 110.0), 0, 3000)
        timer.schedule(GenerateDataTask(0xD00002, 0x60, 26.0, 74.0), 0, 2000)
        timer.schedule(GenerateDataTask(0xD00002, 0x05, 26.0, 74.0), 0, 2000)
        timer.schedule(GenerateDataTask(0xD00003, 0x60, -20.0, 40.0), 0, 4000)
        timer.schedule(GenerateDataTask(0xD00003, 0x05, -20.0, 40.0), 0, 4000)
        timer.schedule(GenerateDataTask(0xD00004, 0x60, 60.0, 120.0), 0, 5000)
        timer.schedule(GenerateDataTask(0xD00004, 0x05, 60.0, 120.0), 0, 5000)
    }

    override fun onStopDataAccess(context: Context?) {
        timer.cancel()

    }

    inner class GenerateDataTask(val address: Int, val dataType: Byte, val minValue: Double, val maxValue: Double) : TimerTask() {

        val generator = Random()

        override fun run() {
            dispatchSensorData(address,
                    dataType,
                    0,
                    System.currentTimeMillis(),
                    3.0f,
                    minValue + (maxValue - minValue) * generator.nextDouble())
        }
    }
}