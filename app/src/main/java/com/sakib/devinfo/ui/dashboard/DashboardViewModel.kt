package com.sakib.devinfo.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sakib.devinfo.utils.SystemInfoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _cpuUsage = MutableLiveData<Float>()
    val cpuUsage: LiveData<Float> = _cpuUsage

    private val _cpuFrequencies = MutableLiveData<List<Long>>()
    val cpuFrequencies: LiveData<List<Long>> = _cpuFrequencies

    private val _cpuTemperature = MutableLiveData<Float>()
    val cpuTemperature: LiveData<Float> = _cpuTemperature

    private val _memoryInfo = MutableLiveData<Triple<Long, Long, Long>>()
    val memoryInfo: LiveData<Triple<Long, Long, Long>> = _memoryInfo

    private val _storageInfo = MutableLiveData<Triple<Long, Long, Long>>()
    val storageInfo: LiveData<Triple<Long, Long, Long>> = _storageInfo

    private val _batteryInfo = MutableLiveData<Quadruple<Int, Float, Int, String>>()
    val batteryInfo: LiveData<Quadruple<Int, Float, Int, String>> = _batteryInfo

    private val _networkStatus = MutableLiveData<String>()
    val networkStatus: LiveData<String> = _networkStatus

    private val _uptime = MutableLiveData<Long>()
    val uptime: LiveData<Long> = _uptime

    fun updateSystemInfo(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            // CPU Info
            val cpuUsageValue = SystemInfoUtils.getCpuUsage()
            val frequencies = SystemInfoUtils.getCpuFrequencies()
            val temperature = SystemInfoUtils.getCpuTemperature(context)

            // Memory Info
            val memInfo = SystemInfoUtils.getMemoryInfo(context)

            // Storage Info
            val storageInfo = SystemInfoUtils.getStorageInfo(context)

            // Battery Info
            val batteryInfo = getBatteryInfo(context)

            // Network Status
            val networkStatus = getNetworkStatus(context)

            // Uptime
            val uptime = SystemInfoUtils.getUptime()

            // Update UI on main thread
            CoroutineScope(Dispatchers.Main).launch {
                _cpuUsage.value = cpuUsageValue
                _cpuFrequencies.value = frequencies
                _cpuTemperature.value = temperature
                _memoryInfo.value = memInfo
                _storageInfo.value = storageInfo
                _batteryInfo.value = batteryInfo
                _networkStatus.value = networkStatus
                _uptime.value = uptime
            }
        }
    }

    private fun getBatteryInfo(context: Context): Quadruple<Int, Float, Int, String> {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val temperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0f
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

        val batteryLevel = if (scale > 0) (level * 100 / scale) else 0
        val healthString = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        return Quadruple(batteryLevel, temperature, voltage, healthString)
    }

    private fun getNetworkStatus(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "Not Connected"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Not Connected"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi Connected"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
