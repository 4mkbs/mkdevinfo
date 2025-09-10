package com.sakib.devinfo.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.sakib.devinfo.DevInfoApplication
import com.sakib.devinfo.R
import com.sakib.devinfo.ui.MainActivity
import com.sakib.devinfo.utils.SystemInfoUtils

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    private lateinit var cpuUsageText: TextView
    private lateinit var ramUsageText: TextView
    private lateinit var batteryText: TextView

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startFloatingOverlay()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startFloatingOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingView()
        startUpdating()
    }

    private fun createFloatingView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_overlay, null)

        cpuUsageText = floatingView!!.findViewById(R.id.cpu_usage_text)
        ramUsageText = floatingView!!.findViewById(R.id.ram_usage_text)
        batteryText = floatingView!!.findViewById(R.id.battery_text)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        windowManager?.addView(floatingView, params)

        // Add touch listener for drag functionality
        var initialX: Int = 0
        var initialY: Int = 0
        var initialTouchX: Float = 0f
        var initialTouchY: Float = 0f

        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun startUpdating() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateSystemInfo()
                updateHandler.postDelayed(this, 1000)
            }
        }
        updateHandler.post(updateRunnable!!)
    }

    private fun updateSystemInfo() {
        val cpuUsage = SystemInfoUtils.getCpuUsage()
        val memoryInfo = SystemInfoUtils.getMemoryInfo(this)
        val batteryIntent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: 0

        cpuUsageText.text = "CPU: ${cpuUsage.toInt()}%"
        
        val ramPercentage = if (memoryInfo.first > 0) 
            ((memoryInfo.second.toFloat() / memoryInfo.first) * 100).toInt() else 0
        ramUsageText.text = "RAM: $ramPercentage%"
        
        batteryText.text = "BAT: $batteryLevel%"
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DevInfoApplication.OVERLAY_CHANNEL_ID)
            .setContentTitle("Floating Overlay Active")
            .setContentText("System monitor is running")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DevInfoApplication.OVERLAY_CHANNEL_ID,
                "Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
        floatingView?.let { windowManager?.removeView(it) }
    }

    companion object {
        const val ACTION_START = "START_FLOATING_OVERLAY"
        const val ACTION_STOP = "STOP_FLOATING_OVERLAY"
        private const val NOTIFICATION_ID = 1001
    }
}
