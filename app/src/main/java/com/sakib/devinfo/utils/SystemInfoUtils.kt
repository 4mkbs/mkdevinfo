package com.sakib.devinfo.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import java.io.File
import java.io.RandomAccessFile
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap

object SystemInfoUtils {
    
    private val temperatureCache = ConcurrentHashMap<String, Pair<Float, Long>>()
    private const val CACHE_DURATION = 5000L // 5 seconds cache
    
    fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()
            
            val toks = load.split(" +".toRegex()).toTypedArray()
            val idle1 = toks[4].toLong()
            val cpu1 = toks[2].toLong() + toks[3].toLong() + toks[5].toLong() + toks[6].toLong() + toks[7].toLong() + toks[8].toLong()
            
            Thread.sleep(360)
            
            val reader2 = RandomAccessFile("/proc/stat", "r")
            val load2 = reader2.readLine()
            reader2.close()
            
            val toks2 = load2.split(" +".toRegex()).toTypedArray()
            val idle2 = toks2[4].toLong()
            val cpu2 = toks2[2].toLong() + toks2[3].toLong() + toks2[5].toLong() + toks2[6].toLong() + toks2[7].toLong() + toks2[8].toLong()
            
            ((cpu2 - cpu1).toFloat() / ((cpu2 + idle2) - (cpu1 + idle1)).toFloat()) * 100f
        } catch (ex: Exception) {
            0f
        }
    }
    
    fun getMemoryInfo(context: Context): Triple<Long, Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalMemory = memoryInfo.totalMem
        val availableMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availableMemory
        
        return Triple(totalMemory, usedMemory, availableMemory)
    }
    
    fun getStorageInfo(context: Context): Triple<Long, Long, Long> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalStorage = totalBlocks * blockSize
        val availableStorage = availableBlocks * blockSize
        val usedStorage = totalStorage - availableStorage
        
        return Triple(totalStorage, usedStorage, availableStorage)
    }
    
    fun getBatteryInfo(context: Context): Array<Any> {
        // Use intent broadcast for broader compatibility instead of unsupported BATTERY_PROPERTY_* constants
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val voltage = (intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val temperature = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        val healthText = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }
        return arrayOf(pct, statusText, healthText, voltage, temperature)
    }
    
    fun getCpuTemperature(context: Context): Float {
        val cacheKey = "cpu_temp"
        val cached = temperatureCache[cacheKey]
        val currentTime = System.currentTimeMillis()
        
        // Return cached value if still valid
        if (cached != null && (currentTime - cached.second) < CACHE_DURATION) {
            return cached.first
        }
        
        var temperature = 0f
        
        try {
            // Try thermal manager for newer Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        // Note: thermalHeadroom was removed as it's not available in all API levels
                        // Using alternative approach
                        temperature = getCurrentThermalStatus(powerManager)
                    } catch (e: Exception) {
                        // Fallback to file reading
                        temperature = readTemperatureFromFiles()
                    }
                } else {
                    temperature = readTemperatureFromFiles()
                }
            } else {
                temperature = readTemperatureFromFiles()
            }
        } catch (e: Exception) {
            temperature = 0f
        }
        
        // Cache the result
        temperatureCache[cacheKey] = Pair(temperature, currentTime)
        return temperature
    }
    
    private fun getCurrentThermalStatus(powerManager: PowerManager): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val thermalStatus = powerManager.currentThermalStatus
                // Convert thermal status to approximate temperature
                when (thermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> 35f
                    PowerManager.THERMAL_STATUS_LIGHT -> 45f
                    PowerManager.THERMAL_STATUS_MODERATE -> 55f
                    PowerManager.THERMAL_STATUS_SEVERE -> 65f
                    PowerManager.THERMAL_STATUS_CRITICAL -> 75f
                    PowerManager.THERMAL_STATUS_EMERGENCY -> 85f
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> 95f
                    else -> 40f
                }
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun readTemperatureFromFiles(): Float {
        val thermalFiles = arrayOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
            "/sys/devices/platform/s5p-tmu/temperature",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone0/temp"
        )
        
        for (file in thermalFiles) {
            try {
                val temp = File(file)
                if (temp.exists() && temp.canRead()) {
                    val tempStr = temp.readText().trim()
                    val tempValue = tempStr.toFloatOrNull()
                    if (tempValue != null && tempValue > 0) {
                        // Convert from millidegrees if necessary
                        return if (tempValue > 1000) tempValue / 1000f else tempValue
                    }
                }
            } catch (e: Exception) {
                // Continue to next file
                continue
            }
        }
        return 0f
    }
    
    fun getNetworkStatus(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return "No Connection"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No Connection"
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi Connected"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data Connected"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet Connected"
                else -> "Connected"
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return if (networkInfo?.isConnected == true) {
                when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> "WiFi Connected"
                    ConnectivityManager.TYPE_MOBILE -> "Mobile Data Connected"
                    ConnectivityManager.TYPE_ETHERNET -> "Ethernet Connected"
                    else -> "Connected"
                }
            } else {
                "No Connection"
            }
        }
    }
    
    fun getUptime(): Long {
        return SystemClock.elapsedRealtime()
    }
    
    fun getCpuFrequencies(): List<Long> {
        val frequencies = mutableListOf<Long>()
        
        try {
            val cpuCount = Runtime.getRuntime().availableProcessors()
            
            for (i in 0 until cpuCount) {
                try {
                    val freqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                    if (freqFile.exists() && freqFile.canRead()) {
                        val freqStr = freqFile.readText().trim()
                        val freq = freqStr.toLongOrNull()
                        if (freq != null) {
                            frequencies.add(freq)
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next CPU
                    continue
                }
            }
        } catch (e: Exception) {
            // Return empty list if error
        }
        
        return frequencies
    }
    
    fun formatBytes(bytes: Long): String {
        val df = DecimalFormat("#.##")
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> "${df.format(gb)} GB"
            mb >= 1 -> "${df.format(mb)} MB"
            kb >= 1 -> "${df.format(kb)} KB"
            else -> "$bytes B"
        }
    }
    
    fun formatUptime(uptimeMs: Long): String {
        val seconds = uptimeMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    fun formatFrequency(frequencyKHz: Long): String {
        val mhz = frequencyKHz / 1000.0
        val ghz = mhz / 1000.0
        
        return if (ghz >= 1) {
            "${DecimalFormat("#.##").format(ghz)} GHz"
        } else {
            "${DecimalFormat("#.#").format(mhz)} MHz"
        }
    }
}
