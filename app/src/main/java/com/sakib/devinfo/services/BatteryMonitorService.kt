package com.sakib.devinfo.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sakib.devinfo.DevInfoApplication
import com.sakib.devinfo.R
import com.sakib.devinfo.ui.MainActivity

class BatteryMonitorService : Service() {

    private lateinit var batteryReceiver: BroadcastReceiver
    private var isCharging = false
    private var batteryLevel = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setupBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun setupBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL

                        checkBatteryConditions()
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        isCharging = true
                        showNotification("Charging Started", "Device is now charging")
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        isCharging = false
                        showNotification("Charging Stopped", "Device disconnected from charger")
                    }
                }
            }
        }
    }

    private fun startMonitoring() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)

        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    private fun checkBatteryConditions() {
        when {
            batteryLevel <= 15 && !isCharging -> {
                showNotification(
                    "Low Battery Warning",
                    "Battery level is $batteryLevel%. Please charge your device."
                )
            }
            batteryLevel >= 90 && isCharging -> {
                showNotification(
                    "Battery Almost Full",
                    "Battery level is $batteryLevel%. Consider unplugging to preserve battery health."
                )
            }
            batteryLevel == 100 -> {
                showNotification(
                    "Battery Full",
                    "Battery is fully charged. Unplug to preserve battery health."
                )
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DevInfoApplication.BATTERY_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_battery_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DevInfoApplication.BATTERY_CHANNEL_ID)
            .setContentTitle("Battery Monitor Active")
            .setContentText("Monitoring battery status")
            .setSmallIcon(R.drawable.ic_battery_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    companion object {
        const val ACTION_START = "START_BATTERY_MONITOR"
        const val ACTION_STOP = "STOP_BATTERY_MONITOR"
        private const val NOTIFICATION_ID = 1002
    }
}
