package com.weisi.tool.wsnbox.bean.configuration

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.RatchetWheelMeasurement
import com.cjq.lib.weisi.iot.Sensor
import com.cjq.lib.weisi.iot.SensorManager


class SensorConfiguration(address: Int) {

    val base: Base
    private val measures: MutableList<Measure>

    init {
        val sensor = SensorManager.getPhysicalSensor(address)
        base = Base(address, sensor.info.defaultName, sensor.info.configuration)
        val size = sensor.displayMeasurementSize
        measures = MutableList(size) {
            val measurement = sensor.getDisplayMeasurementByPosition(it)
            Measure(measurement.defaultName, measurement.id.id, measurement.configuration)
        }
    }

    fun measureSize(): Int {
        return measures.size
    }

    fun getMeasure(position: Int): Measure {
        return measures[position]
    }

    fun setBaseConfiguration(configuration: Sensor.Info.Configuration) {
        base.configuration = configuration
    }

    fun setMeasureConfiguration(id: Long, configId: Long, configuration: DisplayMeasurement.Configuration) {
        val measure = measures.find { id == it.id }
        if (measure === null) {
            measures.add(Measure("未知测量量", id, configId, configuration))
        } else {
            measure.configurationId = configId
            measure.configuration = configuration
        }
    }

    class Base(val address: Int, val defaultName: String, internal var configuration: Sensor.Info.Configuration) {
    }

    class Measure(val defaultName: String, val id: Long, configId: Long, config: DisplayMeasurement.Configuration) {

        constructor(defaultName: String, id: Long, config: DisplayMeasurement.Configuration)
                : this(defaultName, id, 0L, config)

        companion object {
            const val WT_NONE = 0
            const val WT_SINGLE_RANGE = 1
            const val WT_SWITCH = 2

            const val CT_NORMAL = 0
            const val CT_RATCHET_WHEEL = 1

            private const val TYPE_MASK = 0xffff
            private const val MOVE_BITS = 16

            fun buildType(warnerType: Int, configType: Int) : Int {
                return ensureAvailableWarnerType(warnerType) or
                        (ensureAvailableConfigType(configType) shl MOVE_BITS)
            }

            private fun ensureAvailableWarnerType(warnerType: Int): Int {
                return if (warnerType in WT_NONE..WT_SWITCH) {
                    warnerType
                } else {
                    WT_NONE
                }
            }

            private fun ensureAvailableConfigType(configType: Int): Int {
                return if (configType in CT_NORMAL..CT_RATCHET_WHEEL) {
                    configType
                } else {
                    CT_NORMAL
                }
            }

            fun getWarnerType(measureType: Int): Int {
                return ensureAvailableWarnerType(getWarnerTypeUnchecked(measureType))
            }

            private fun getWarnerTypeUnchecked(measureType: Int): Int {
                return measureType and TYPE_MASK
            }

            fun getConfigType(measureType: Int): Int {
                return ensureAvailableConfigType(getConfigTypeUnchecked(measureType))
            }

            private fun getConfigTypeUnchecked(measureType: Int): Int {
                return measureType shr MOVE_BITS
            }
        }

        var type: Int = 0
        private set

        var configurationId: Long = configId
        internal set

        var configuration: DisplayMeasurement.Configuration = config
        internal set(value) {
            field = value
            type = buildType(when (value.warner) {
                is DisplayMeasurement.SingleRangeWarner -> WT_SINGLE_RANGE
                is DisplayMeasurement.SwitchWarner -> WT_SWITCH
                else -> WT_NONE
            }, when (value) {
                is RatchetWheelMeasurement.Configuration -> CT_RATCHET_WHEEL
                else -> CT_NORMAL
            })
        }

        init {
            configuration = config
        }

        fun getWarnerType(): Int {
            return getWarnerTypeUnchecked(type)
        }

        fun getConfigType(): Int {
            return getConfigTypeUnchecked(type)
        }
    }
}