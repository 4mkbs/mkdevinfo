package com.sakib.devinfo.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import android.os.SystemClock
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.RandomAccessFile

object SystemInfoUtils {

    fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()

            val toks = load.split(" +".toRegex()).toTypedArray()
            val idle1 = toks[4].toLong()
            val cpu1 = toks[2].toLong() + toks[3].toLong() + toks[5].toLong() +
                    toks[6].toLong() + toks[7].toLong() + toks[8].toLong()

            Thread.sleep(360)

            val reader2 = RandomAccessFile("/proc/stat", "r")
            val load2 = reader2.readLine()
            reader2.close()

            val toks2 = load2.split(" +".toRegex()).toTypedArray()
            val idle2 = toks2[4].toLong()
            val cpu2 = toks2[2].toLong() + toks2[3].toLong() + toks2[5].toLong() +
                    toks2[6].toLong() + toks2[7].toLong() + toks2[8].toLong()

            ((cpu2 - cpu1).toFloat() / ((cpu2 + idle2) - (cpu1 + idle1))) * 100
        } catch (ex: IOException) {
            0f
        }
    }

    fun getCpuFrequencies(): List<Long> {
        val frequencies = mutableListOf<Long>()
        try {
            val coreCount = Runtime.getRuntime().availableProcessors()
            for (i in 0 until coreCount) {
                val file = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                if (file.exists()) {
                    val reader = BufferedReader(FileReader(file))
                    val freq = reader.readLine()?.toLongOrNull() ?: 0L
                    frequencies.add(freq / 1000) // Convert to MHz
                    reader.close()
                }
            }
        } catch (e: Exception) {
            // Fallback for older devices
            for (i in 0 until Runtime.getRuntime().availableProcessors()) {
                frequencies.add(0L)
            }
        }
        return frequencies
    }

    fun getCpuTemperature(): Float {
        return try {
            val thermalFiles = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp"
            )

            for (file in thermalFiles) {
                try {
                    val tempFile = File(file)
                    if (tempFile.exists()) {
                        val reader = BufferedReader(FileReader(tempFile))
                        val temp = reader.readLine()?.toFloatOrNull()
                        reader.close()
                        if (temp != null) {
                            return if (temp > 1000) temp / 1000 else temp
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            0f
        } catch (e: Exception) {
            0f
        }
    }

    fun getMemoryInfo(context: Context): Triple<Long, Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRam = memInfo.totalMem
        val availableRam = memInfo.availMem
        val usedRam = totalRam - availableRam

        return Triple(totalRam, usedRam, availableRam)
    }

    fun getStorageInfo(path: String): Triple<Long, Long, Long> {
        return try {
            val stat = StatFs(path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalSpace = totalBlocks * blockSize
            val availableSpace = availableBlocks * blockSize
            val usedSpace = totalSpace - availableSpace

            Triple(totalSpace, usedSpace, availableSpace)
        } catch (e: Exception) {
            Triple(0L, 0L, 0L)
        }
    }

    fun getUptime(): Long {
        return SystemClock.elapsedRealtime()
    }

    fun getCpuInfo(): Map<String, String> {
        val cpuInfo = mutableMapOf<String, String>()
        try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split(":")
                if (parts.size >= 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    cpuInfo[key] = value
                }
            }
            reader.close()
        } catch (e: Exception) {
            // Fallback values
            cpuInfo["processor"] = "Unknown"
            cpuInfo["model name"] = "Unknown"
            cpuInfo["cpu cores"] = Runtime.getRuntime().availableProcessors().toString()
        }

        // Add additional info
        cpuInfo["Architecture"] = Build.SUPPORTED_ABIS[0]
        cpuInfo["Cores"] = Runtime.getRuntime().availableProcessors().toString()

        return cpuInfo
    }

    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.2f %s", size, units[unitIndex])
    }

    fun formatFrequency(frequencyKHz: Long): String {
        return when {
            frequencyKHz >= 1000000 -> String.format("%.2f GHz", frequencyKHz / 1000000.0)
            frequencyKHz >= 1000 -> String.format("%.0f MHz", frequencyKHz / 1000.0)
            else -> "$frequencyKHz kHz"
        }
    }

    fun formatUptime(uptimeMs: Long): String {
        val seconds = uptimeMs / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
}
