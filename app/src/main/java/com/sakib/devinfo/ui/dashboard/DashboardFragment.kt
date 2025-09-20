package com.sakib.devinfo.ui.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sakib.devinfo.databinding.FragmentDashboardBinding
import com.sakib.devinfo.utils.SystemInfoUtils

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private var updateHandler: Handler? = null
    private var updateRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateHandler = Handler(Looper.getMainLooper())
        startUpdating()
    }

    private fun startUpdating() {
        updateRunnable = object : Runnable {
            override fun run() {
                if (isAdded && _binding != null) {
                    updateSystemInfo()
                    updateHandler?.postDelayed(this, 3000) // Update every 3 seconds
                }
            }
        }
        updateHandler?.post(updateRunnable!!)
    }

    private fun updateSystemInfo() {
        try {
            context?.let { ctx ->
                val cpuUsage = SystemInfoUtils.getCpuUsage()
                val memoryInfo = SystemInfoUtils.getMemoryInfo(ctx)
                val storageInfo = SystemInfoUtils.getStorageInfo(ctx)
                val batteryInfo = SystemInfoUtils.getBatteryInfo(ctx)
                val networkStatus = SystemInfoUtils.getNetworkStatus(ctx)
                val uptime = SystemInfoUtils.getUptime()

                binding.apply {
                    cpuUsageText.text = "CPU Usage: ${String.format("%.1f", cpuUsage)}%"
                    ramUsageText.text = "RAM: ${SystemInfoUtils.formatBytes(memoryInfo.second)} / ${SystemInfoUtils.formatBytes(memoryInfo.first)}"
                    storageUsageText.text = "Storage: ${SystemInfoUtils.formatBytes(storageInfo.second)} / ${SystemInfoUtils.formatBytes(storageInfo.first)}"
                    batteryUsageText.text = "Battery: ${batteryInfo[0]}% (${batteryInfo[1]})"
                    networkStatusText.text = "Network: $networkStatus"
                    uptimeText.text = "Uptime: ${SystemInfoUtils.formatUptime(uptime)}"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateRunnable?.let { updateHandler?.removeCallbacks(it) }
        updateHandler = null
        _binding = null
    }
}
