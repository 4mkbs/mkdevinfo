package com.sakib.devinfo.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getTheme(): String {
        return sharedPreferences.getString(KEY_THEME, "system") ?: "system"
    }

    fun setTheme(theme: String) {
        sharedPreferences.edit().putString(KEY_THEME, theme).apply()
    }

    fun isFloatingOverlayEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_FLOATING_OVERLAY, false)
    }

    fun setFloatingOverlayEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FLOATING_OVERLAY, enabled).apply()
    }

    fun isBatteryMonitorEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BATTERY_MONITOR, false)
    }

    fun setBatteryMonitorEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BATTERY_MONITOR, enabled).apply()
    }

    fun getRefreshInterval(): Long {
        return sharedPreferences.getLong(KEY_REFRESH_INTERVAL, 1000L)
    }

    fun setRefreshInterval(interval: Long) {
        sharedPreferences.edit().putLong(KEY_REFRESH_INTERVAL, interval).apply()
    }

    companion object {
        private const val PREF_NAME = "devinfo_preferences"
        private const val KEY_THEME = "theme"
        private const val KEY_FLOATING_OVERLAY = "floating_overlay"
        private const val KEY_BATTERY_MONITOR = "battery_monitor"
        private const val KEY_REFRESH_INTERVAL = "refresh_interval"
    }
}
