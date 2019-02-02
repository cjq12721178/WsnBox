package com.weisi.tool.wsnbox

import com.cjq.lib.weisi.iot.interpreter.FloatInterpreter
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTargetBiasTest {

    companion object {
        private val VALUE_INTERPRETER = FloatInterpreter(3, "KN")
        private const val MIN_ANIMATION_TIME_INTERVAL = 0.005
        private const val OVERFLOW_RATIO = 0.5
        private const val MIN_LOW_LIMIT_BIAS = 0.7f
        private const val MAX_LOW_LIMIT_BIAS = 0.83f
        private const val MIN_HIGH_LIMIT_BIAS = 0.17f
        private const val MAX_HIGH_LIMIT_BIAS = 0.3f
    }

    private var topValue = 100.0
    private var bottomValue = 0.0
    private var lowLimit = 25.0
    private var highLimit = 75.0

    @Test
    fun calculateTargetBias_ltBottomValue() {
        val actual = calculateTargetBias(-1.0)
        val expected = 1.0f
        assertEquals(expected, actual, 0.000001f)
    }

    @Test
    fun calculateTargetBias_gtBottomValue_lsMinLowLimit() {
        val actual = calculateTargetBias(17.0)
        val expected = 0.83f
        assertEquals(expected, actual, 0.000001f)
    }

    private fun calculateTargetBias(value: Double): Float {
        val theoryBias = (1 - (value - bottomValue) / (topValue - bottomValue)).toFloat()
        var actualBias = 0.0f
        if (value < bottomValue) {
            actualBias = 1.0f
        } else if (value < topValue) {
            if (theoryBias in MIN_LOW_LIMIT_BIAS..MAX_LOW_LIMIT_BIAS) {
                if (value > lowLimit) {
                    actualBias = MIN_LOW_LIMIT_BIAS
                } else {
                    actualBias = MAX_LOW_LIMIT_BIAS
                }
            } else if (theoryBias in MIN_HIGH_LIMIT_BIAS..MAX_HIGH_LIMIT_BIAS) {
                if (value < highLimit) {
                    actualBias = MAX_HIGH_LIMIT_BIAS
                } else {
                    actualBias = MIN_HIGH_LIMIT_BIAS
                }
            } else {
                actualBias = theoryBias
            }
        }
        return actualBias
    }
}