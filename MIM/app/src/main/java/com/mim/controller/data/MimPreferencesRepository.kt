package com.mim.controller.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.mimDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mim_preferences"
)

class MimPreferencesRepository(private val context: Context) {

    private object Keys {
        val POWER_ICON_STYLE = stringPreferencesKey("power_icon_style")
        val BUTTON_SIZE = stringPreferencesKey("button_size")
        val BUTTON_SPACING = stringPreferencesKey("button_spacing")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FLOATING_ENABLED = booleanPreferencesKey("floating_controller_enabled")
        val SHOW_SYSTEM_VOLUME_UI = booleanPreferencesKey("show_system_volume_ui")
        val LOCK_METHOD_PREFERENCE = stringPreferencesKey("lock_method_preference")
    }

    val preferencesFlow: Flow<MimPreferences> = context.mimDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            MimPreferences(
                powerIconStyle = prefs[Keys.POWER_ICON_STYLE]
                    ?.let { runCatching { PowerIconStyle.valueOf(it) }.getOrNull() }
                    ?: PowerIconStyle.STANDARD_POWER,
                buttonSize = prefs[Keys.BUTTON_SIZE]
                    ?.let { runCatching { ButtonSizeOption.valueOf(it) }.getOrNull() }
                    ?: ButtonSizeOption.STANDARD,
                buttonSpacing = prefs[Keys.BUTTON_SPACING]
                    ?.let { runCatching { ButtonSpacingOption.valueOf(it) }.getOrNull() }
                    ?: ButtonSpacingOption.COMFORTABLE,
                themeMode = prefs[Keys.THEME_MODE]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                floatingControllerEnabled = prefs[Keys.FLOATING_ENABLED] ?: false,
                showSystemVolumeUi = prefs[Keys.SHOW_SYSTEM_VOLUME_UI] ?: true,
                lockMethodPreference = prefs[Keys.LOCK_METHOD_PREFERENCE]
                    ?.let { runCatching { LockMethodPreference.valueOf(it) }.getOrNull() }
                    ?: LockMethodPreference.AUTO
            )
        }

    suspend fun setPowerIconStyle(style: PowerIconStyle) {
        context.mimDataStore.edit { it[Keys.POWER_ICON_STYLE] = style.name }
    }

    suspend fun setButtonSize(size: ButtonSizeOption) {
        context.mimDataStore.edit { it[Keys.BUTTON_SIZE] = size.name }
    }

    suspend fun setButtonSpacing(spacing: ButtonSpacingOption) {
        context.mimDataStore.edit { it[Keys.BUTTON_SPACING] = spacing.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.mimDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setFloatingControllerEnabled(enabled: Boolean) {
        context.mimDataStore.edit { it[Keys.FLOATING_ENABLED] = enabled }
    }

    suspend fun setShowSystemVolumeUi(show: Boolean) {
        context.mimDataStore.edit { it[Keys.SHOW_SYSTEM_VOLUME_UI] = show }
    }

    suspend fun setLockMethodPreference(method: LockMethodPreference) {
        context.mimDataStore.edit { it[Keys.LOCK_METHOD_PREFERENCE] = method.name }
    }

    suspend fun resetToDefaults() {
        context.mimDataStore.edit { it.clear() }
    }
}
