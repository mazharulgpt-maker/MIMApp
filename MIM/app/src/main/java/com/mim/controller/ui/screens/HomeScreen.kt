package com.mim.controller.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mim.controller.controller.ScreenLockStatus
import com.mim.controller.controller.VolumeState
import com.mim.controller.data.MimPreferences
import com.mim.controller.ui.icons.MimPowerGlyph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    preferences: MimPreferences,
    volumeState: VolumeState,
    lockStatus: ScreenLockStatus,
    showPermissionSheet: Boolean,
    onDismissPermissionSheet: () -> Unit,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onPowerLock: () -> Unit,
    onOpenSettings: () -> Unit,
    onActivateDeviceAdmin: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sideSize = preferences.buttonSize.sideButtonDp.dp
    val centerSize = preferences.buttonSize.centerButtonDp.dp
    val spacing = preferences.buttonSpacing.spacingDp.dp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top bar: MIM app name + small Settings/Edit icon in top-right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MIM",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.semantics { contentDescription = "Open MIM Settings" }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Center focal area: Exactly three large circular icon buttons in one horizontal row
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT BUTTON: Volume Up (+)
                CircularHardwareButton(
                    size = sideSize,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    contentDescription = "Volume Up",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onVolumeUp()
                    }
                ) {
                    VolumeStepGlyph(
                        isPlus = true,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // CENTER BUTTON: Screen Off / Lock (NOT phone shutdown)
                CircularHardwareButton(
                    size = centerSize,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    borderColor = Color.Transparent,
                    elevation = 6.dp,
                    contentDescription = "Turn screen off and lock",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPowerLock()
                    }
                ) {
                    MimPowerGlyph(
                        style = preferences.powerIconStyle,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        size = (centerSize.value * 0.4f).dp
                    )
                }

                // RIGHT BUTTON: Volume Down (-)
                CircularHardwareButton(
                    size = sideSize,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    contentDescription = "Volume Down",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onVolumeDown()
                    }
                ) {
                    VolumeStepGlyph(
                        isPlus = false,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Subtle, live media volume synchronization indicator at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 28.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = if (volumeState.max > volumeState.min) {
                    (volumeState.current - volumeState.min).toFloat() /
                        (volumeState.max - volumeState.min).toFloat()
                } else {
                    0f
                }

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${volumeState.current} / ${volumeState.max}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showPermissionSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissPermissionSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Enable Screen Lock Permission",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Android requires explicit user permission before an app can turn off and lock the screen. MIM only requests screen-lock permission — never data wipe, monitoring, or root access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onDismissPermissionSheet()
                        onActivateDeviceAdmin()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Activate Device Admin (Immediate Lock)")
                }

                if (lockStatus.isAccessibilityLockSupported) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onDismissPermissionSheet()
                            onOpenAccessibilitySettings()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Use Accessibility Lock (Keeps Fingerprint)")
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeStepGlyph(
    isPlus: Boolean,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.VolumeUp,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(1.dp))
        Icon(
            imageVector = if (isPlus) Icons.Rounded.Add else Icons.Rounded.Remove,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CircularHardwareButton(
    size: Dp,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    elevation: Dp = 2.dp,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_press_scale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .scale(scale)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = elevation,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
        interactionSource = interactionSource
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}
