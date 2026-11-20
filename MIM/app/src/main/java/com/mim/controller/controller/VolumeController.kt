package com.mim.controller.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class VolumeState(
    val current: Int,
    val min: Int,
    val max: Int
) {
    val percentage: Int
        get() {
            val range = (max - min).coerceAtLeast(1)
            return (((current - min).toFloat() / range.toFloat()) * 100f)
                .toInt()
                .coerceIn(0, 100)
        }

    val isAtMax: Boolean get() = current >= max
    val isAtMin: Boolean get() = current <= min
}

/**
 * Controls actual Android device media volume (AudioManager.STREAM_MUSIC).
 * Every click adjusts the real OS media stream by exactly one step and respects device min/max bounds.
 */
class VolumeController(private val context: Context) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getCurrentVolumeState(): VolumeState {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }
        return VolumeState(current = current, min = min, max = max)
    }

    fun increaseVolume(showSystemUi: Boolean = true): VolumeState {
        val before = getCurrentVolumeState()
        val flags = if (showSystemUi) AudioManager.FLAG_SHOW_UI else 0
        if (before.current < before.max) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                flags
            )
        } else if (showSystemUi) {
            // Still display the native Android volume HUD so the user sees volume is already at max
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI
            )
        }
        return getCurrentVolumeState()
    }

    fun decreaseVolume(showSystemUi: Boolean = true): VolumeState {
        val before = getCurrentVolumeState()
        val flags = if (showSystemUi) AudioManager.FLAG_SHOW_UI else 0
        if (before.current > before.min) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                flags
            )
        } else if (showSystemUi) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI
            )
        }
        return getCurrentVolumeState()
    }

    /**
     * Observes real Android system media volume changes in real time via both BroadcastReceiver
     * and Settings.System ContentObserver so the UI always stays synchronized with device volume.
     */
    fun observeVolumeState(): Flow<VolumeState> = callbackFlow {
        trySend(getCurrentVolumeState())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(getCurrentVolumeState())
            }
        }

        val intentFilter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, intentFilter)
        }

        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(getCurrentVolumeState())
            }
        }

        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            contentObserver
        )

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
            runCatching { context.contentResolver.unregisterContentObserver(contentObserver) }
        }
    }.distinctUntilChanged()
}
