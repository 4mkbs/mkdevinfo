package com.sakib.devinfo.ui.system

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sakib.devinfo.databinding.FragmentSystemBinding
import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SystemFragment : Fragment() {

    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DevInfo", "SystemFragment: Loading system information")
        
        loadAndroidInfo()
        loadKernelInfo()
        loadRuntimeInfo()
        loadSystemProperties()
    }

    private fun loadAndroidInfo() {
        try {
            binding.tvAndroidVersion.text = "Android Version: ${Build.VERSION.RELEASE}"
            binding.tvApiLevel.text = "API Level: ${Build.VERSION.SDK_INT}"
            
            val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else {
                "Not available"
            }
            binding.tvSecurityPatch.text = "Security Patch: $securityPatch"
            
            binding.tvBuildNumber.text = "Build Number: ${Build.DISPLAY}"
            Log.d("DevInfo", "Android info loaded: ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading Android info", e)
        }
    }

    private fun loadKernelInfo() {
        try {
            val kernelVersion = System.getProperty("os.version") ?: "Unknown"
            binding.tvKernelVersion.text = "Kernel Version: $kernelVersion"
            
            val uptimeMillis = SystemClock.elapsedRealtime()
            val uptime = formatUptime(uptimeMillis)
            binding.tvUptime.text = "System Uptime: $uptime"
            
            Log.d("DevInfo", "Kernel info loaded: $kernelVersion")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading kernel info", e)
            binding.tvKernelVersion.text = "Kernel Version: Unable to retrieve"
            binding.tvUptime.text = "System Uptime: Unable to retrieve"
        }
    }

    private fun loadRuntimeInfo() {
        try {
            val javaVersion = System.getProperty("java.version") ?: "Unknown"
            binding.tvJavaVersion.text = "Java Version: $javaVersion"
            
            val vmName = System.getProperty("java.vm.name") ?: "Unknown"
            val vmVersion = System.getProperty("java.vm.version") ?: "Unknown"
            binding.tvVmVersion.text = "VM: $vmName $vmVersion"
            
            val runtime = Runtime.getRuntime()
            val maxHeap = runtime.maxMemory()
            val heapSize = formatBytes(maxHeap)
            binding.tvVmHeapSize.text = "VM Heap Size: $heapSize"
            
            Log.d("DevInfo", "Runtime info loaded: Java $javaVersion, VM $vmName")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading runtime info", e)
            binding.tvJavaVersion.text = "Java Version: Unable to retrieve"
            binding.tvVmVersion.text = "VM Version: Unable to retrieve"
            binding.tvVmHeapSize.text = "VM Heap Size: Unable to retrieve"
        }
    }

    private fun loadSystemProperties() {
        try {
            val timeZone = TimeZone.getDefault()
            binding.tvTimeZone.text = "Time Zone: ${timeZone.displayName} (${timeZone.id})"
            
            val locale = Locale.getDefault()
            binding.tvLocale.text = "Locale: ${locale.displayName} (${locale.toString()})"
            
            // Calculate boot time
            val bootTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            val bootTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(bootTimeMillis))
            binding.tvBootTime.text = "Boot Time: $bootTime"
            
            Log.d("DevInfo", "System properties loaded: ${timeZone.id}, ${locale.toString()}")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading system properties", e)
            binding.tvTimeZone.text = "Time Zone: Unable to retrieve"
            binding.tvLocale.text = "Locale: Unable to retrieve"
            binding.tvBootTime.text = "Boot Time: Unable to retrieve"
        }
    }

    private fun formatUptime(uptimeMillis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(uptimeMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60
        
        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m ${seconds}s"
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        
        return String.format("%.1f %s", value, units[digitGroups])
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
