package com.mim.controller

import com.mim.controller.data.ButtonSizeOption
import com.mim.controller.data.ButtonSpacingOption
import com.mim.controller.data.LockMethodPreference
import com.mim.controller.data.MimPreferences
import com.mim.controller.data.PowerIconStyle
import com.mim.controller.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MimPreferencesTest {

    @Test
    fun defaultPreferencesKeepFloatingDisabledAndSystemTheme() {
        val defaults = MimPreferences()
        assertEquals(PowerIconStyle.STANDARD_POWER, defaults.powerIconStyle)
        assertEquals(ButtonSizeOption.STANDARD, defaults.buttonSize)
        assertEquals(ButtonSpacingOption.COMFORTABLE, defaults.buttonSpacing)
        assertEquals(ThemeMode.SYSTEM, defaults.themeMode)
        assertEquals(LockMethodPreference.AUTO, defaults.lockMethodPreference)
        assertFalse(defaults.floatingControllerEnabled)
        assertTrue(defaults.showSystemVolumeUi)
    }
}
