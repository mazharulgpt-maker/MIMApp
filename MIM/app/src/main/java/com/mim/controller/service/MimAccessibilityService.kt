package com.mim.controller.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

/**
 * Minimal AccessibilityService used solely to invoke GLOBAL_ACTION_LOCK_SCREEN on Android 9+.
 * Does NOT inspect window contents, read text, or monitor user activity in other applications.
 */
class MimAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally no-op: MIM does not inspect or log events from any other application.
    }

    override fun onInterrupt() {
        // Intentionally no-op.
    }

    override fun onDestroy() {
        instanceRef.clear()
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var instanceRef: WeakReference<MimAccessibilityService> = WeakReference(null)

        fun isRunning(): Boolean = instanceRef.get() != null

        fun lockScreenNow(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
            val service = instanceRef.get() ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
    }
}
