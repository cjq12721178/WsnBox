package com.weisi.tool.wsnbox

import org.junit.Assert.assertEquals
import org.junit.Test

class RangeTest {

    @Test
    fun positiveLoop() {
        var j = 0
        for (i in 0..10 step 2) {
            assertEquals(j, i)
            j += 2
            println("i: $i")
        }
    }

    @Test
    fun negativeLoop() {
        var j = 10
        for (i in 10 downTo 0 step -2) {
            assertEquals(j, i)
            j -= 2
            println("i: $i")
        }
    }

    //private fun rangeLoop(range: Range<T>, )
}