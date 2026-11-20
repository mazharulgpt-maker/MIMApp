package com.mim.controller.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Official Android DeviceAdminReceiver used exclusively for DevicePolicyManager.lockNow()
 * so MIM's center Power button can immediately turn off and lock the display.
 */
class MimDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "MIM Screen Lock permission enabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling MIM Screen Lock will stop the virtual Power button from locking the screen unless Accessibility Lock is enabled."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "MIM Screen Lock permission disabled",
            Toast.LENGTH_SHORT
        ).show()
    }
}
