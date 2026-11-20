package com.mim.controller.controller

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.mim.controller.R
import com.mim.controller.admin.MimDeviceAdminReceiver
import com.mim.controller.data.LockMethodPreference
import com.mim.controller.service.MimAccessibilityService

data class ScreenLockStatus(
    val isDeviceAdminActive: Boolean,
    val isAccessibilityLockActive: Boolean,
    val isAccessibilityLockSupported: Boolean,
    val canDrawOverlays: Boolean
) {
    val canLockScreen: Boolean
        get() = isDeviceAdminActive || (isAccessibilityLockSupported && isAccessibilityLockActive)
}

sealed interface ScreenLockResult {
    data class Locked(val methodUsed: String) : ScreenLockResult
    data object PermissionRequired : ScreenLockResult
    data class Failed(val reason: String) : ScreenLockResult
}

/**
 * Turns the phone display OFF and immediately puts the phone into its normal locked state.
 * Never performs shutdown, reboot, or opens the system power menu.
 *
 * Supports two official Android mechanisms:
 * 1. DevicePolicyManager.lockNow() via MimDeviceAdminReceiver (force-lock policy only)
 * 2. AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN on Android 9+ (preserves fingerprint/face unlock)
 */
class ScreenLockController(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent: ComponentName =
        ComponentName(context, MimDeviceAdminReceiver::class.java)

    fun getStatus(): ScreenLockStatus {
        val isAccessibilitySupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        return ScreenLockStatus(
            isDeviceAdminActive = devicePolicyManager.isAdminActive(adminComponent),
            isAccessibilityLockActive = isAccessibilitySupported && MimAccessibilityService.isRunning(),
            isAccessibilityLockSupported = isAccessibilitySupported,
            canDrawOverlays = Settings.canDrawOverlays(context)
        )
    }

    fun lockScreen(preference: LockMethodPreference = LockMethodPreference.AUTO): ScreenLockResult {
        val status = getStatus()
        if (!status.canLockScreen) {
            return ScreenLockResult.PermissionRequired
        }

        return when (preference) {
            LockMethodPreference.ACCESSIBILITY -> {
                if (status.isAccessibilityLockActive) {
                    performAccessibilityLock()
                } else if (status.isDeviceAdminActive) {
                    performDeviceAdminLock()
                } else {
                    ScreenLockResult.PermissionRequired
                }
            }

            LockMethodPreference.DEVICE_ADMIN -> {
                if (status.isDeviceAdminActive) {
                    performDeviceAdminLock()
                } else if (status.isAccessibilityLockActive) {
                    performAccessibilityLock()
                } else {
                    ScreenLockResult.PermissionRequired
                }
            }

            LockMethodPreference.AUTO -> {
                // On Android 9+, Accessibility GLOBAL_ACTION_LOCK_SCREEN preserves biometric unlock;
                // otherwise use DevicePolicyManager.lockNow() directly.
                if (status.isAccessibilityLockActive) {
                    val result = performAccessibilityLock()
                    if (result is ScreenLockResult.Locked) return result
                }
                if (status.isDeviceAdminActive) {
                    performDeviceAdminLock()
                } else {
                    ScreenLockResult.PermissionRequired
                }
            }
        }
    }

    private fun performDeviceAdminLock(): ScreenLockResult {
        return try {
            devicePolicyManager.lockNow()
            ScreenLockResult.Locked("Device Admin (lockNow)")
        } catch (e: SecurityException) {
            ScreenLockResult.PermissionRequired
        } catch (e: Exception) {
            ScreenLockResult.Failed(e.message ?: "Unable to lock screen")
        }
    }

    private fun performAccessibilityLock(): ScreenLockResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return ScreenLockResult.Failed("Accessibility screen lock requires Android 9 or newer")
        }
        val locked = MimAccessibilityService.lockScreenNow()
        return if (locked) {
            ScreenLockResult.Locked("Accessibility (GLOBAL_ACTION_LOCK_SCREEN)")
        } else {
            ScreenLockResult.PermissionRequired
        }
    }

    fun createDeviceAdminActivationIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.device_admin_explanation)
            )
        }
    }

    fun removeDeviceAdmin() {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.removeActiveAdmin(adminComponent)
        }
    }

    fun createAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createOverlayPermissionIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createDisplaySettingsIntent(): Intent {
        return Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
