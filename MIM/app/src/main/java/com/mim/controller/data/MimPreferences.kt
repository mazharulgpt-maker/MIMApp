package com.mim.controller.data

enum class PowerIconStyle(val label: String) {
    STANDARD_POWER("Classic Power"),
    CIRCLE_POWER("Ring Power"),
    LOCK_SHIELD("Screen Lock"),
    SLEEP_MOON("Display Sleep"),
    MINIMAL_DOT("Minimal Standby")
}

enum class ButtonSizeOption(val label: String, val sideButtonDp: Int, val centerButtonDp: Int) {
    COMPACT("Compact", 72, 82),
    STANDARD("Standard", 84, 96),
    LARGE("Large", 96, 108),
    EXTRA_LARGE("Extra Large", 104, 118)
}

enum class ButtonSpacingOption(val label: String, val spacingDp: Int) {
    TIGHT("Tight", 12),
    COMFORTABLE("Comfortable", 20),
    WIDE("Wide", 28)
}

enum class ThemeMode(val label: String) {
    SYSTEM("Follow System"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class LockMethodPreference(val label: String) {
    AUTO("Automatic (Recommended)"),
    DEVICE_ADMIN("Device Admin (lockNow)"),
    ACCESSIBILITY("Accessibility (Biometric Friendly)")
}

data class MimPreferences(
    val powerIconStyle: PowerIconStyle = PowerIconStyle.STANDARD_POWER,
    val buttonSize: ButtonSizeOption = ButtonSizeOption.STANDARD,
    val buttonSpacing: ButtonSpacingOption = ButtonSpacingOption.COMFORTABLE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val floatingControllerEnabled: Boolean = false,
    val showSystemVolumeUi: Boolean = true,
    val lockMethodPreference: LockMethodPreference = LockMethodPreference.AUTO
)
