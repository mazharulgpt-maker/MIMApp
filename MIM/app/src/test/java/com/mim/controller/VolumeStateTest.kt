package com.mim.controller

import com.mim.controller.controller.VolumeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeStateTest {

    @Test
    fun percentageCalculationRespectsBounds() {
        val mid = VolumeState(current = 7, min = 0, max = 15)
        assertEquals(46, mid.percentage)
        assertFalse(mid.isAtMin)
        assertFalse(mid.isAtMax)

        val max = VolumeState(current = 15, min = 0, max = 15)
        assertEquals(100, max.percentage)
        assertTrue(max.isAtMax)

        val min = VolumeState(current = 0, min = 0, max = 15)
        assertEquals(0, min.percentage)
        assertTrue(min.isAtMin)
    }

    @Test
    fun handlesZeroRangeSafely() {
        val zeroRange = VolumeState(current = 0, min = 0, max = 0)
        assertEquals(0, zeroRange.percentage)
        assertTrue(zeroRange.isAtMin)
        assertTrue(zeroRange.isAtMax)
    }
}
