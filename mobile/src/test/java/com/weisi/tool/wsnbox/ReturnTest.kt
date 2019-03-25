package com.weisi.tool.wsnbox

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by CJQ on 2018/4/11.
 */
class ReturnTest {

    @Test
    fun innerReturn() {
        var expect = canGetMarried(22)
        var actual = false
        assertEquals(expect, actual)
    }

    private fun canGetMarried(age: Int): Boolean {
        return if (age < 18) {
            false
        } else {
            var stages = listOf<Int>(20, 22, 30)
            var i = 0
            while (i < stages.size) {
                if (stages[i] <= age) {
                    return true
                }
            }
            false
        }
    }
}