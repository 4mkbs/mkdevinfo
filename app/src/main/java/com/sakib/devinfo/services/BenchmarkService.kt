package com.sakib.devinfo.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.sakib.devinfo.DevInfoApplication
import com.sakib.devinfo.R
import com.sakib.devinfo.ui.MainActivity
import com.sakib.devinfo.utils.SystemInfoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class BenchmarkService : Service() {

    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CPU_BENCHMARK -> startCpuBenchmark()
            ACTION_GPU_BENCHMARK -> startGpuBenchmark()
            ACTION_STORAGE_BENCHMARK -> startStorageBenchmark()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startCpuBenchmark() {
        if (isRunning) return
        isRunning = true

        startForeground(NOTIFICATION_ID, createBenchmarkNotification("CPU Benchmark", "Running CPU stress test..."))

        CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            var progress = 0

            // Simulate CPU intensive operations
            for (i in 0..100) {
                if (!isRunning) break

                // CPU intensive calculation
                var result = 0.0
                for (j in 0..1000000) {
                    result += Math.sqrt(j.toDouble()) * Math.sin(j.toDouble())
                }

                progress = i
                updateNotification("CPU Benchmark", "Progress: $progress% - Result: ${result.toInt()}")
                Thread.sleep(50) // Small delay to prevent overwhelming
            }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            showResultNotification(
                "CPU Benchmark Complete",
                "Duration: ${duration}ms - Score: ${calculateCpuScore(duration)}"
            )

            isRunning = false
            stopSelf()
        }
    }

    private fun startGpuBenchmark() {
        if (isRunning) return
        isRunning = true

        startForeground(NOTIFICATION_ID, createBenchmarkNotification("GPU Benchmark", "Running GPU rendering test..."))

        CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            var progress = 0

            // Simulate GPU rendering operations
            for (i in 0..100) {
                if (!isRunning) break

                // Simulate rendering operations
                val frames = Array(1000) { Random.nextFloat() * 255 }
                frames.sort()

                progress = i
                updateNotification("GPU Benchmark", "Progress: $progress% - Frames processed: ${frames.size}")
                Thread.sleep(30)
            }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            showResultNotification(
                "GPU Benchmark Complete",
                "Duration: ${duration}ms - FPS Score: ${calculateGpuScore(duration)}"
            )

            isRunning = false
            stopSelf()
        }
    }

    private fun startStorageBenchmark() {
        if (isRunning) return
        isRunning = true

        startForeground(NOTIFICATION_ID, createBenchmarkNotification("Storage Benchmark", "Testing storage speed..."))

        CoroutineScope(Dispatchers.IO).launch {
            val startTime = System.currentTimeMillis()
            var progress = 0

            try {
                // Test write speed
                val testFile = java.io.File(filesDir, "benchmark_test.dat")
                val data = ByteArray(1024 * 1024) // 1MB chunks

                for (i in 0..50) {
                    if (!isRunning) break

                    testFile.writeBytes(data)
                    progress = i * 2
                    updateNotification("Storage Benchmark", "Write test: $progress%")
                    Thread.sleep(20)
                }

                // Test read speed
                for (i in 0..50) {
                    if (!isRunning) break

                    testFile.readBytes()
                    progress = 50 + i
                    updateNotification("Storage Benchmark", "Read test: ${progress - 50}%")
                    Thread.sleep(20)
                }

                testFile.delete()

                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime

                showResultNotification(
                    "Storage Benchmark Complete",
                    "Duration: ${duration}ms - Speed Score: ${calculateStorageScore(duration)}"
                )

            } catch (e: Exception) {
                showResultNotification(
                    "Storage Benchmark Failed",
                    "Error: ${e.message}"
                )
            }

            isRunning = false
            stopSelf()
        }
    }

    private fun createBenchmarkNotification(title: String, message: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DevInfoApplication.BENCHMARK_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_benchmark)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, message: String) {
        val notification = createBenchmarkNotification(title, message)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showResultNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DevInfoApplication.BENCHMARK_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_benchmark)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun calculateCpuScore(duration: Long): Int {
        // Lower duration = higher score
        return maxOf(1000 - (duration / 10).toInt(), 100)
    }

    private fun calculateGpuScore(duration: Long): Int {
        // Simulate FPS calculation
        return maxOf(60000 / duration.toInt(), 10)
    }

    private fun calculateStorageScore(duration: Long): Int {
        // MB/s calculation (simulated)
        return maxOf(100000 / duration.toInt(), 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    companion object {
        const val ACTION_CPU_BENCHMARK = "CPU_BENCHMARK"
        const val ACTION_GPU_BENCHMARK = "GPU_BENCHMARK"
        const val ACTION_STORAGE_BENCHMARK = "STORAGE_BENCHMARK"
        const val ACTION_STOP = "STOP_BENCHMARK"
        private const val NOTIFICATION_ID = 1003
    }
}
