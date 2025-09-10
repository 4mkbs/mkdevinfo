package com.sakib.devinfo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class DevInfoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Battery monitor channel
            val batteryChannel = NotificationChannel(
                BATTERY_CHANNEL_ID,
                "Battery Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Battery status notifications"
            }

            // Floating overlay channel
            val overlayChannel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                "Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Floating overlay service notifications"
            }

            // Benchmark channel
            val benchmarkChannel = NotificationChannel(
                BENCHMARK_CHANNEL_ID,
                "Benchmark",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Benchmark progress notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(batteryChannel, overlayChannel, benchmarkChannel)
            )
        }
    }

    companion object {
        const val BATTERY_CHANNEL_ID = "battery_monitor"
        const val OVERLAY_CHANNEL_ID = "floating_overlay"
        const val BENCHMARK_CHANNEL_ID = "benchmark"
    }
}
