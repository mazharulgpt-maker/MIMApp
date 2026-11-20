package com.mim.controller

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mim.controller.controller.ScreenLockController
import com.mim.controller.controller.ScreenLockResult
import com.mim.controller.controller.ScreenLockStatus
import com.mim.controller.controller.VolumeController
import com.mim.controller.data.MimPreferences
import com.mim.controller.data.MimPreferencesRepository
import com.mim.controller.service.MimFloatingService
import com.mim.controller.ui.screens.HomeScreen
import com.mim.controller.ui.screens.SettingsScreen
import com.mim.controller.ui.theme.MimTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepository: MimPreferencesRepository
    private lateinit var volumeController: VolumeController
    private lateinit var screenLockController: ScreenLockController

    private var lockStatusState = mutableStateOf(
        ScreenLockStatus(
            isDeviceAdminActive = false,
            isAccessibilityLockActive = false,
            isAccessibilityLockSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
            canDrawOverlays = false
        )
    )

    private val deviceAdminLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshPermissionStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureSupportedScreenTurnOn()

        preferencesRepository = MimPreferencesRepository(applicationContext)
        volumeController = VolumeController(applicationContext)
        screenLockController = ScreenLockController(applicationContext)
        refreshPermissionStatus()

        setContent {
            val preferences by preferencesRepository.preferencesFlow.collectAsState(
                initial = MimPreferences()
            )
            val volumeState by volumeController.observeVolumeState().collectAsState(
                initial = volumeController.getCurrentVolumeState()
            )
            val lockStatus by lockStatusState
            val coroutineScope = rememberCoroutineScope()

            var isSettingsOpen by remember { mutableStateOf(false) }
            var showPermissionSheet by remember { mutableStateOf(false) }
            var lastTestMessage by remember { mutableStateOf<String?>(null) }

            // Keep optional Floating Controller service synchronized with saved preference
            LaunchedEffect(preferences.floatingControllerEnabled, lockStatus.canDrawOverlays) {
                if (preferences.floatingControllerEnabled && lockStatus.canDrawOverlays) {
                    MimFloatingService.start(applicationContext)
                } else {
                    MimFloatingService.stop(applicationContext)
                }
            }

            MimTheme(themeMode = preferences.themeMode) {
                Crossfade(
                    targetState = isSettingsOpen,
                    label = "mim_screen_transition"
                ) { showSettings ->
                    if (!showSettings) {
                        HomeScreen(
                            preferences = preferences,
                            volumeState = volumeState,
                            lockStatus = lockStatus,
                            showPermissionSheet = showPermissionSheet,
                            onDismissPermissionSheet = { showPermissionSheet = false },
                            onVolumeUp = {
                                volumeController.increaseVolume(
                                    showSystemUi = preferences.showSystemVolumeUi
                                )
                            },
                            onVolumeDown = {
                                volumeController.decreaseVolume(
                                    showSystemUi = preferences.showSystemVolumeUi
                                )
                            },
                            onPowerLock = {
                                when (val result = screenLockController.lockScreen(preferences.lockMethodPreference)) {
                                    is ScreenLockResult.Locked -> Unit
                                    is ScreenLockResult.PermissionRequired -> {
                                        showPermissionSheet = true
                                    }
                                    is ScreenLockResult.Failed -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            result.reason,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onOpenSettings = {
                                refreshPermissionStatus()
                                isSettingsOpen = true
                            },
                            onActivateDeviceAdmin = {
                                deviceAdminLauncher.launch(
                                    screenLockController.createDeviceAdminActivationIntent()
                                )
                            },
                            onOpenAccessibilitySettings = {
                                startActivity(screenLockController.createAccessibilitySettingsIntent())
                            }
                        )
                    } else {
                        SettingsScreen(
                            preferences = preferences,
                            volumeState = volumeState,
                            lockStatus = lockStatus,
                            lastTestMessage = lastTestMessage,
                            onBack = { isSettingsOpen = false },
                            onPowerIconSelected = { style ->
                                coroutineScope.launch {
                                    preferencesRepository.setPowerIconStyle(style)
                                }
                            },
                            onButtonSizeSelected = { size ->
                                coroutineScope.launch {
                                    preferencesRepository.setButtonSize(size)
                                }
                            },
                            onButtonSpacingSelected = { spacing ->
                                coroutineScope.launch {
                                    preferencesRepository.setButtonSpacing(spacing)
                                }
                            },
                            onThemeModeSelected = { mode ->
                                coroutineScope.launch {
                                    preferencesRepository.setThemeMode(mode)
                                }
                            },
                            onLockMethodSelected = { method ->
                                coroutineScope.launch {
                                    preferencesRepository.setLockMethodPreference(method)
                                }
                            },
                            onToggleFloatingController = { enabled ->
                                if (enabled && !lockStatus.canDrawOverlays) {
                                    startActivity(screenLockController.createOverlayPermissionIntent())
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Allow Display Over Other Apps, then toggle on again",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    coroutineScope.launch {
                                        preferencesRepository.setFloatingControllerEnabled(enabled)
                                    }
                                    if (enabled) {
                                        MimFloatingService.start(applicationContext)
                                    } else {
                                        MimFloatingService.stop(applicationContext)
                                    }
                                }
                            },
                            onToggleSystemVolumeUi = { show ->
                                coroutineScope.launch {
                                    preferencesRepository.setShowSystemVolumeUi(show)
                                }
                            },
                            onActivateDeviceAdmin = {
                                deviceAdminLauncher.launch(
                                    screenLockController.createDeviceAdminActivationIntent()
                                )
                            },
                            onRemoveDeviceAdmin = {
                                screenLockController.removeDeviceAdmin()
                                refreshPermissionStatus()
                            },
                            onOpenAccessibilitySettings = {
                                startActivity(screenLockController.createAccessibilitySettingsIntent())
                            },
                            onOpenOverlaySettings = {
                                startActivity(screenLockController.createOverlayPermissionIntent())
                            },
                            onOpenDisplaySettings = {
                                startActivity(screenLockController.createDisplaySettingsIntent())
                            },
                            onTestVolumeUp = {
                                val updated = volumeController.increaseVolume(
                                    showSystemUi = preferences.showSystemVolumeUi
                                )
                                lastTestMessage = "Volume Up OK → ${updated.current}/${updated.max}"
                            },
                            onTestVolumeDown = {
                                val updated = volumeController.decreaseVolume(
                                    showSystemUi = preferences.showSystemVolumeUi
                                )
                                lastTestMessage = "Volume Down OK → ${updated.current}/${updated.max}"
                            },
                            onTestScreenLock = {
                                when (val result = screenLockController.lockScreen(preferences.lockMethodPreference)) {
                                    is ScreenLockResult.Locked -> {
                                        lastTestMessage = "Locked via ${result.methodUsed}"
                                    }
                                    is ScreenLockResult.PermissionRequired -> {
                                        lastTestMessage = "Enable Device Admin or Accessibility Lock first"
                                    }
                                    is ScreenLockResult.Failed -> {
                                        lastTestMessage = result.reason
                                    }
                                }
                            },
                            onResetDefaults = {
                                coroutineScope.launch {
                                    preferencesRepository.resetToDefaults()
                                    MimFloatingService.stop(applicationContext)
                                    lastTestMessage = "All preferences restored to defaults"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
    }

    private fun refreshPermissionStatus() {
        if (::screenLockController.isInitialized) {
            lockStatusState.value = screenLockController.getStatus()
        }
    }

    /**
     * Uses official Android Activity APIs so if MIM is launched via a supported wake trigger,
     * Android can turn the screen on cleanly without relying on unreliable background WakeLocks.
     */
    private fun configureSupportedScreenTurnOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
