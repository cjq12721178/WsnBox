package com.weisi.tool.wsnbox.bean.warner.processor

import com.cjq.lib.weisi.iot.DisplayMeasurement
import com.cjq.lib.weisi.iot.DisplayMeasurement.SingleRangeWarner.RESULT_ABOVE_HIGH_LIMIT
import com.cjq.lib.weisi.iot.DisplayMeasurement.SingleRangeWarner.RESULT_BELOW_LOW_LIMIT
import com.cjq.lib.weisi.iot.DisplayMeasurement.SwitchWarner.RESULT_IN_NORMAL_STATE
import com.cjq.lib.weisi.iot.Warner
import com.cjq.lib.weisi.iot.Warner.RESULT_NORMAL
import com.weisi.tool.wsnbox.bean.warner.executor.BackgroundNormalWarnExecutor
import com.weisi.tool.wsnbox.bean.warner.executor.NormalWarnExecutor
import com.weisi.tool.wsnbox.bean.warner.executor.SingleRangeWarnExecutor
import com.weisi.tool.wsnbox.bean.warner.executor.SwitchWarnExecutor
import kotlin.reflect.KClass

/**
 * Created by CJQ on 2018/2/8.
 */
class CommonWarnProcessor<E> {

    private val singleRangeWarnExecutors = mutableSetOf<SingleRangeWarnExecutor<E>>()
    private val switchWarnExecutors = mutableSetOf<SwitchWarnExecutor<E>>()
    private val normalWarnExecutors = mutableSetOf<NormalWarnExecutor<E>>()
    private val normalWarnExecutorTypes = mutableListOf<KClass<*>>()

    constructor() {
        normalWarnExecutorTypes.add(BackgroundNormalWarnExecutor::class)
    }

    fun addExecutor(executor: SingleRangeWarnExecutor<E>) {
        singleRangeWarnExecutors.add(executor)
        addNormalWarnExecutors(executor)
    }

    fun addExecutor(executor: SwitchWarnExecutor<E>) {
        switchWarnExecutors.add(executor)
        addNormalWarnExecutors(executor)
    }

    private fun addNormalWarnExecutors(executor: NormalWarnExecutor<E>) {
        var typeIndex = normalWarnExecutorTypes.indices.find {
            index -> normalWarnExecutorTypes[index].isInstance(executor)
        }
        if (typeIndex != null) {
            normalWarnExecutors.add(executor);
            normalWarnExecutorTypes.removeAt(typeIndex)
        }
    }

    fun process(value: DisplayMeasurement.Value?,
                warner: Warner<DisplayMeasurement.Value>?,
                env: E) {
        if (value != null && warner != null) {
            var warnResult = warner.test(value)
            when (warner) {
                is DisplayMeasurement.SingleRangeWarner -> when (warnResult) {
                    RESULT_NORMAL -> processNormalResult(env)
                    RESULT_ABOVE_HIGH_LIMIT -> singleRangeWarnExecutors.forEach {
                        executor -> executor.onResultAboveHighLimit(env)
                    }
                    RESULT_BELOW_LOW_LIMIT -> singleRangeWarnExecutors.forEach {
                        executor -> executor.onResultBelowLowLimit(env)
                    }
                }
                is DisplayMeasurement.SwitchWarner -> if (warnResult == RESULT_IN_NORMAL_STATE) {
                    processNormalResult(env)
                } else {
                    switchWarnExecutors.forEach {
                        executor -> executor.onResultInAbnormalState(env)
                    }
                }
                else -> processNormalResult(env)
            }
        } else {
            processNormalResult(env)
        }
    }

    private fun processNormalResult(env: E) {
        normalWarnExecutors.forEach { executor ->
            executor.onResultNormal(env)
        }
    }
}