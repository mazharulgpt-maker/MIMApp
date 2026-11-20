package com.mim.controller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.mim.controller.MainActivity
import com.mim.controller.R
import com.mim.controller.controller.ScreenLockController
import com.mim.controller.controller.ScreenLockResult
import com.mim.controller.controller.VolumeController
import kotlin.math.abs

/**
 * Optional draggable Floating MIM Controller overlay.
 *
 * Disabled by default; only started when the user explicitly turns it on and grants
 * SYSTEM_ALERT_WINDOW permission.
 *
 * Note: An Android overlay cannot wake a fully sleeping or powered-off screen.
 */
class MimFloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var volumeController: VolumeController
    private lateinit var screenLockController: ScreenLockController

    private var rootContainer: LinearLayout? = null
    private var expandedControls: LinearLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isExpanded: Boolean = false

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        volumeController = VolumeController(this)
        screenLockController = ScreenLockController(this)

        startForegroundNotification()
        attachOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        rootContainer?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        rootContainer = null
        expandedControls = null
        super.onDestroy()
    }

    private fun startForegroundNotification() {
        val channelId = CHANNEL_ID
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            getString(R.string.floating_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.floating_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MimFloatingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.floating_notification_title))
            .setContentText(getString(R.string.floating_notification_text))
            .setContentIntent(openAppIntent)
            .addAction(0, "Hide", stopIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun attachOverlayView() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(6), dp(6), dp(6), dp(6))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EE0F172A"))
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.parseColor("#334155"))
            }
            elevation = dp(8).toFloat()
        }

        val mimBadge = createCircleButton(
            label = "MIM",
            bgColor = Color.parseColor("#1E293B"),
            textColor = Color.WHITE,
            textSizeSp = 11f
        )

        val controlsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
        }

        val volUpBtn = createCircleButton(
            label = "+",
            bgColor = Color.parseColor("#1E293B"),
            textColor = Color.WHITE,
            textSizeSp = 20f
        ).apply {
            setOnClickListener { volumeController.increaseVolume(showSystemUi = true) }
        }

        val powerBtn = createCircleButton(
            label = "⏻",
            bgColor = Color.parseColor("#2563EB"),
            textColor = Color.WHITE,
            textSizeSp = 18f
        ).apply {
            setOnClickListener {
                when (screenLockController.lockScreen()) {
                    is ScreenLockResult.Locked -> {
                        collapseControls()
                    }
                    is ScreenLockResult.PermissionRequired -> {
                        Toast.makeText(
                            this@MimFloatingService,
                            "Enable Screen Lock permission inside MIM Settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is ScreenLockResult.Failed -> {
                        Toast.makeText(
                            this@MimFloatingService,
                            "Could not lock screen. Check MIM Settings.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        val volDownBtn = createCircleButton(
            label = "−",
            bgColor = Color.parseColor("#1E293B"),
            textColor = Color.WHITE,
            textSizeSp = 20f
        ).apply {
            setOnClickListener { volumeController.decreaseVolume(showSystemUi = true) }
        }

        val closeBtn = createCircleButton(
            label = "×",
            bgColor = Color.parseColor("#334155"),
            textColor = Color.parseColor("#CBD5E1"),
            textSizeSp = 16f
        ).apply {
            setOnClickListener { stopSelf() }
        }

        controlsRow.addView(volUpBtn, marginStartParams(dp(6)))
        controlsRow.addView(powerBtn, marginStartParams(dp(6)))
        controlsRow.addView(volDownBtn, marginStartParams(dp(6)))
        controlsRow.addView(closeBtn, marginStartParams(dp(6)))

        container.addView(mimBadge)
        container.addView(controlsRow)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(240)
        }

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        mimBadge.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (abs(dx) > dp(4) || abs(dy) > dp(4)) {
                        moved = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(container, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        toggleExpanded()
                    }
                    true
                }
                else -> false
            }
        }

        rootContainer = container
        expandedControls = controlsRow
        layoutParams = params

        windowManager.addView(container, params)
    }

    private fun toggleExpanded() {
        isExpanded = !isExpanded
        expandedControls?.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    private fun collapseControls() {
        isExpanded = false
        expandedControls?.visibility = View.GONE
    }

    private fun createCircleButton(
        label: String,
        bgColor: Int,
        textColor: Int,
        textSizeSp: Float
    ): TextView {
        val size = dp(44)
        return TextView(this).apply {
            text = label
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
            gravity = Gravity.CENTER
            width = size
            height = size
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(bgColor)
            }
        }
    }

    private fun marginStartParams(marginPx: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = marginPx
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val CHANNEL_ID = "mim_floating_controller"
        private const val NOTIFICATION_ID = 4102
        const val ACTION_STOP = "com.mim.controller.ACTION_STOP_FLOATING"

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            val intent = Intent(context, MimFloatingService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MimFloatingService::class.java))
        }
    }
}
