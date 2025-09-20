package com.sakib.devinfo.ui.hardware

import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sakib.devinfo.databinding.FragmentHardwareBinding
import java.io.File
import java.text.DecimalFormat
import kotlin.math.pow

class HardwareFragment : Fragment() {

    private var _binding: FragmentHardwareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHardwareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DevInfo", "HardwareFragment: Loading hardware information")
        
        loadDeviceInfo()
        loadCpuInfo()
        loadMemoryInfo()
        loadStorageInfo()
    }

    private fun loadDeviceInfo() {
        try {
            binding.tvDeviceModel.text = "Model: ${Build.MODEL}"
            binding.tvDeviceManufacturer.text = "Manufacturer: ${Build.MANUFACTURER}"
            binding.tvDeviceBrand.text = "Brand: ${Build.BRAND}"
            binding.tvDeviceBoard.text = "Board: ${Build.BOARD}"
            Log.d("DevInfo", "Device info loaded successfully")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading device info", e)
        }
    }

    private fun loadCpuInfo() {
        try {
            val arch = when {
                Build.SUPPORTED_64_BIT_ABIS.isNotEmpty() -> "64-bit (${Build.SUPPORTED_64_BIT_ABIS[0]})"
                Build.SUPPORTED_32_BIT_ABIS.isNotEmpty() -> "32-bit (${Build.SUPPORTED_32_BIT_ABIS[0]})"
                else -> "Unknown"
            }
            
            val cores = Runtime.getRuntime().availableProcessors()
            val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
            
            binding.tvCpuArch.text = "Architecture: $arch"
            binding.tvCpuCores.text = "CPU Cores: $cores"
            binding.tvCpuAbi.text = "Primary ABI: $primaryAbi"
            Log.d("DevInfo", "CPU info loaded: $cores cores, $arch")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading CPU info", e)
        }
    }

    private fun loadMemoryInfo() {
        try {
            val activityManager = requireContext().getSystemService(ActivityManager::class.java)
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalMemory = memoryInfo.totalMem
            val availableMemory = memoryInfo.availMem
            val usedMemory = totalMemory - availableMemory
            
            binding.tvTotalMemory.text = "Total RAM: ${formatBytes(totalMemory)}"
            binding.tvAvailableMemory.text = "Available RAM: ${formatBytes(availableMemory)}"
            binding.tvUsedMemory.text = "Used RAM: ${formatBytes(usedMemory)}"
            Log.d("DevInfo", "Memory info loaded: ${formatBytes(totalMemory)} total")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading memory info", e)
            binding.tvTotalMemory.text = "Total RAM: Unable to retrieve"
            binding.tvAvailableMemory.text = "Available RAM: Unable to retrieve"
            binding.tvUsedMemory.text = "Used RAM: Unable to retrieve"
        }
    }

    private fun loadStorageInfo() {
        try {
            // Internal storage
            val internalPath = Environment.getDataDirectory()
            val internalStat = StatFs(internalPath.path)
            val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
            val internalFree = internalStat.availableBlocksLong * internalStat.blockSizeLong
            val internalUsed = internalTotal - internalFree
            
            binding.tvInternalStorage.text = "Internal: ${formatBytes(internalUsed)} / ${formatBytes(internalTotal)} used"
            
            // External storage
            val externalState = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == externalState) {
                val externalPath = Environment.getExternalStorageDirectory()
                val externalStat = StatFs(externalPath.path)
                val externalTotal = externalStat.blockCountLong * externalStat.blockSizeLong
                val externalFree = externalStat.availableBlocksLong * externalStat.blockSizeLong
                val externalUsed = externalTotal - externalFree
                
                binding.tvExternalStorage.text = "External: ${formatBytes(externalUsed)} / ${formatBytes(externalTotal)} used"
            } else {
                binding.tvExternalStorage.text = "External Storage: Not available"
            }
            
            Log.d("DevInfo", "Storage info loaded successfully")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading storage info", e)
            binding.tvInternalStorage.text = "Internal Storage: Unable to retrieve"
            binding.tvExternalStorage.text = "External Storage: Unable to retrieve"
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        
        val df = DecimalFormat("#.##")
        return "${df.format(value)} ${units[digitGroups]}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
