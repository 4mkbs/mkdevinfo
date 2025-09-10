package com.sakib.devinfo.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
    private val updateHandler = Handler(Looper.getMainLooper())
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
        
        setupObservers()
        startUpdating()
    }

    private fun setupObservers() {
        viewModel.cpuUsage.observe(viewLifecycleOwner) { usage ->
            binding.cpuProgressCircular.progress = usage.toInt()
            binding.cpuUsageText.text = "${usage.toInt()}%"
        }

        viewModel.memoryInfo.observe(viewLifecycleOwner) { (total, used, _) ->
            val percentage = if (total > 0) ((used.toFloat() / total) * 100).toInt() else 0
            binding.ramProgressCircular.progress = percentage
            binding.ramUsageText.text = "$percentage%"
            binding.ramDetailsText.text = "${SystemInfoUtils.formatBytes(used)} / ${SystemInfoUtils.formatBytes(total)}"
        }

        viewModel.storageInfo.observe(viewLifecycleOwner) { (total, used, _) ->
            val percentage = if (total > 0) ((used.toFloat() / total) * 100).toInt() else 0
            binding.storageProgressCircular.progress = percentage
            binding.storageUsageText.text = "$percentage%"
            binding.storageDetailsText.text = "${SystemInfoUtils.formatBytes(used)} / ${SystemInfoUtils.formatBytes(total)}"
        }

        viewModel.batteryInfo.observe(viewLifecycleOwner) { (level, temperature, voltage, health) ->
            binding.batteryProgressCircular.progress = level
            binding.batteryUsageText.text = "$level%"
            binding.batteryDetailsText.text = "${temperature}°C • ${voltage}mV"
        }

        viewModel.networkStatus.observe(viewLifecycleOwner) { status ->
            binding.networkStatusText.text = status
        }

        viewModel.uptime.observe(viewLifecycleOwner) { uptime ->
            binding.uptimeText.text = SystemInfoUtils.formatUptime(uptime)
        }

        viewModel.cpuFrequencies.observe(viewLifecycleOwner) { frequencies ->
            if (frequencies.isNotEmpty()) {
                val maxFreq = frequencies.maxOrNull() ?: 0L
                binding.cpuFrequencyText.text = SystemInfoUtils.formatFrequency(maxFreq * 1000)
            }
        }

        viewModel.cpuTemperature.observe(viewLifecycleOwner) { temp ->
            binding.cpuTemperatureText.text = "${temp.toInt()}°C"
        }
    }

    private fun startUpdating() {
        updateRunnable = object : Runnable {
            override fun run() {
                viewModel.updateSystemInfo(requireContext())
                updateHandler.postDelayed(this, 1000) // Update every second
            }
        }
        updateHandler.post(updateRunnable!!)
    }

    private fun stopUpdating() {
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        startUpdating()
    }

    override fun onPause() {
        super.onPause()
        stopUpdating()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopUpdating()
        _binding = null
    }
}
