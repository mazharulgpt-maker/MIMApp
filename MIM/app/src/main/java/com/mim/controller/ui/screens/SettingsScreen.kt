package com.mim.controller.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mim.controller.controller.ScreenLockStatus
import com.mim.controller.controller.VolumeState
import com.mim.controller.data.ButtonSizeOption
import com.mim.controller.data.ButtonSpacingOption
import com.mim.controller.data.LockMethodPreference
import com.mim.controller.data.MimPreferences
import com.mim.controller.data.PowerIconStyle
import com.mim.controller.data.ThemeMode
import com.mim.controller.ui.icons.MimPowerGlyph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: MimPreferences,
    volumeState: VolumeState,
    lockStatus: ScreenLockStatus,
    lastTestMessage: String?,
    onBack: () -> Unit,
    onPowerIconSelected: (PowerIconStyle) -> Unit,
    onButtonSizeSelected: (ButtonSizeOption) -> Unit,
    onButtonSpacingSelected: (ButtonSpacingOption) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLockMethodSelected: (LockMethodPreference) -> Unit,
    onToggleFloatingController: (Boolean) -> Unit,
    onToggleSystemVolumeUi: (Boolean) -> Unit,
    onActivateDeviceAdmin: () -> Unit,
    onRemoveDeviceAdmin: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onTestVolumeUp: () -> Unit,
    onTestVolumeDown: () -> Unit,
    onTestScreenLock: () -> Unit,
    onResetDefaults: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MIM Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to MIM"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Power Icon Customization
            SettingsSection(title = "Power Icon Style") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PowerIconStyle.entries.forEach { style ->
                        val selected = preferences.powerIconStyle == style
                        Surface(
                            onClick = { onPowerIconSelected(style) },
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (selected) 0.dp else 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                MimPowerGlyph(
                                    style = style,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    size = 24.dp,
                                    contentDescription = style.label
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = preferences.powerIconStyle.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 2. Button Size Selection
            SettingsSection(title = "Button Size") {
                OptionRow(
                    options = ButtonSizeOption.entries,
                    selected = preferences.buttonSize,
                    labelFor = { it.label },
                    onSelect = onButtonSizeSelected
                )
            }

            // 3. Button Spacing Adjustment
            SettingsSection(title = "Button Spacing") {
                OptionRow(
                    options = ButtonSpacingOption.entries,
                    selected = preferences.buttonSpacing,
                    labelFor = { it.label },
                    onSelect = onButtonSpacingSelected
                )
            }

            // 4, 5, 6. Theme Selection (Light, Dark, Follow System)
            SettingsSection(title = "Appearance") {
                OptionRow(
                    options = ThemeMode.entries,
                    selected = preferences.themeMode,
                    labelFor = { it.label },
                    onSelect = onThemeModeSelected
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Android volume bar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Displays the system media volume slider on each tap",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preferences.showSystemVolumeUi,
                        onCheckedChange = onToggleSystemVolumeUi
                    )
                }
            }

            // 7. Screen Lock Permission Status
            SettingsSection(title = "Screen Lock Permission") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusLine(
                            label = "Device Admin (lockNow)",
                            active = lockStatus.isDeviceAdminActive
                        )
                        if (lockStatus.isDeviceAdminActive) {
                            OutlinedButton(
                                onClick = onRemoveDeviceAdmin,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Disable Device Admin")
                            }
                        } else {
                            Button(
                                onClick = onActivateDeviceAdmin,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Activate Device Admin")
                            }
                        }

                        if (lockStatus.isAccessibilityLockSupported) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            StatusLine(
                                label = "Accessibility Lock (Biometric Friendly)",
                                active = lockStatus.isAccessibilityLockActive
                            )
                            Text(
                                text = "Uses GLOBAL_ACTION_LOCK_SCREEN on Android 9+ so fingerprint and face unlock remain active. Never reads screen content.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = onOpenAccessibilitySettings,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Open Accessibility Settings")
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Text(
                            text = "Preferred Lock Method",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OptionRow(
                            options = LockMethodPreference.entries,
                            selected = preferences.lockMethodPreference,
                            labelFor = {
                                when (it) {
                                    LockMethodPreference.AUTO -> "Auto"
                                    LockMethodPreference.DEVICE_ADMIN -> "Admin"
                                    LockMethodPreference.ACCESSIBILITY -> "Accessibility"
                                }
                            },
                            onSelect = onLockMethodSelected
                        )
                    }
                }
            }

            // Optional Floating Controller
            SettingsSection(title = "Optional Floating Controller") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show floating MIM button",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Draggable bubble over other apps with Vol+, Lock, and Vol−",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = preferences.floatingControllerEnabled && lockStatus.canDrawOverlays,
                                onCheckedChange = onToggleFloatingController
                            )
                        }

                        StatusLine(
                            label = "Display Over Other Apps Permission",
                            active = lockStatus.canDrawOverlays
                        )

                        if (!lockStatus.canDrawOverlays) {
                            OutlinedButton(
                                onClick = onOpenOverlaySettings,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Grant Overlay Permission")
                            }
                        }

                        Text(
                            text = "Important: A floating overlay cannot wake the screen once Android enters sleep mode.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 8. Button Functionality Testing
            SettingsSection(title = "Button Functionality Testing") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Current Android Media Volume: ${volumeState.current} / ${volumeState.max} (${volumeState.percentage}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onTestVolumeUp,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Vol +")
                            }
                            OutlinedButton(
                                onClick = onTestScreenLock,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Lock")
                            }
                            OutlinedButton(
                                onClick = onTestVolumeDown,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Vol −")
                            }
                        }
                        if (!lastTestMessage.isNullOrBlank()) {
                            Text(
                                text = lastTestMessage,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // 9. Screen Wake Instructions
            SettingsSection(title = "Screen Wake Guide") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Android security prevents ordinary apps from waking a fully sleeping or powered-off screen via a button tap. Use your phone's built-in hardware wake gestures instead:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Double Tap to Wake (Settings → Display or Advanced Features → Motions)\n• Lift to Wake (Settings → Display → Lock screen)\n• Tap to Check Phone / Always-On Display\n• Fingerprint Sensor Wake (touches wake and unlock directly)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedButton(
                            onClick = onOpenDisplaySettings,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open Android Display Settings")
                        }
                    }
                }
            }

            // 10. Reset to Default
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Reset to Default",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset MIM Settings?") },
            text = {
                Text("This restores default Power icon style, button size, spacing, theme, and turns off the optional floating controller.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetDefaults()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun StatusLine(
    label: String,
    active: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (active) "Enabled" else "Not enabled",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (active) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun <T> OptionRow(
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelFor(option),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}
