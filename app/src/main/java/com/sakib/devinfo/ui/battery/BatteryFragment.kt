package com.sakib.devinfo.ui.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sakib.devinfo.R
import com.sakib.devinfo.databinding.FragmentBatteryBinding
import java.text.SimpleDateFormat
import java.util.*

class BatteryFragment : Fragment() {

    private var _binding: FragmentBatteryBinding? = null
    private val binding get() = _binding!!
    
    private var batteryReceiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DevInfo", "BatteryFragment: Loading battery information")
        
        setupBatteryReceiver()
        loadBatteryInfo()
        loadPowerManagementInfo()
    }

    private fun setupBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { updateBatteryInfo(it) }
            }
        }
    }

    private fun loadBatteryInfo() {
        try {
            // Register for battery updates
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = requireContext().registerReceiver(null, intentFilter)
            
            batteryStatus?.let { intent ->
                updateBatteryInfo(intent)
                loadBatterySpecs(intent)
            }
            
            Log.d("DevInfo", "Battery info loaded successfully")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading battery info", e)
            showErrorState()
        }
    }

    private fun updateBatteryInfo(intent: Intent) {
        try {
            // Battery level and scale
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else 0

            // Update UI
            binding.tvBatteryLevel.text = "Level: $batteryPct%"
            binding.progressBattery.progress = batteryPct
            
            // Set progress bar color based on battery level
            val progressColor = when {
                batteryPct > 60 -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                batteryPct > 20 -> ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            }
            binding.progressBattery.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)

            // Battery status
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                else -> "Unknown"
            }
            binding.tvBatteryStatus.text = statusText
            
            // Set status background color
            val statusColor = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                BatteryManager.BATTERY_STATUS_FULL -> ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                BatteryManager.BATTERY_STATUS_DISCHARGING -> ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            }
            binding.tvBatteryStatus.setBackgroundColor(statusColor)

            // Charging status and power source
            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val powerSource = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Battery"
            }
            
            val chargingStatus = if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                "Charging via $powerSource"
            } else {
                "Not charging"
            }
            binding.tvChargingStatus.text = "Charging Status: $chargingStatus"
            binding.tvPowerSource.text = "Power Source: $powerSource"

            // Battery health
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthText = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }
            binding.tvBatteryHealth.text = "Health: $healthText"

            // Battery temperature
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            val tempCelsius = if (temperature > 0) temperature / 10.0 else 0.0
            binding.tvBatteryTemperature.text = "Temperature: ${String.format("%.1f", tempCelsius)}°C"

            // Battery voltage
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            binding.tvBatteryVoltage.text = "Voltage: $voltage mV"

            // Update timestamp
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            binding.tvLastChargeTime.text = "Last Update: $currentTime"

        } catch (e: Exception) {
            Log.e("DevInfo", "Error updating battery info", e)
        }
    }

    private fun loadBatterySpecs(intent: Intent) {
        try {
            // Battery technology
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
            binding.tvBatteryTechnology.text = "Technology: $technology"

            // Battery scale
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            binding.tvBatteryScale.text = "Scale: $scale"

            // Try to get battery capacity (requires API 21+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val batteryManager = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                    val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    val energyCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)

                    val capacityText = if (capacity > 0) "$capacity%" else "Unknown"
                    binding.tvBatteryCapacity.text = "Current Capacity: $capacityText"

                    Log.d("DevInfo", "Battery detailed info: capacity=$capacity, chargeCounter=$chargeCounter, currentNow=$currentNow")
                } catch (e: Exception) {
                    binding.tvBatteryCapacity.text = "Capacity: Not available"
                    Log.w("DevInfo", "Could not get detailed battery info", e)
                }
            } else {
                binding.tvBatteryCapacity.text = "Capacity: Not available (API < 21)"
            }

        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading battery specs", e)
        }
    }

    private fun loadPowerManagementInfo() {
        try {
            val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Power save mode
            val powerSaveMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                powerManager.isPowerSaveMode
            } else {
                false
            }
            binding.tvPowerSaveMode.text = "Power Save Mode: ${if (powerSaveMode) "Enabled" else "Disabled"}"

            // Battery optimization (API 23+)
            val batteryOptimization = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = requireContext().packageName
                !powerManager.isIgnoringBatteryOptimizations(packageName)
            } else {
                false
            }
            binding.tvBatteryOptimization.text = "Battery Optimization: ${if (batteryOptimization) "Enabled" else "Disabled"}"

            Log.d("DevInfo", "Power management info loaded: powerSave=$powerSaveMode, optimization=$batteryOptimization")
        } catch (e: Exception) {
            Log.e("DevInfo", "Error loading power management info", e)
            binding.tvPowerSaveMode.text = "Power Save Mode: Unknown"
            binding.tvBatteryOptimization.text = "Battery Optimization: Unknown"
        }
    }

    private fun showErrorState() {
        binding.tvBatteryLevel.text = "Level: Unable to retrieve"
        binding.tvBatteryStatus.text = "Unknown"
        binding.tvChargingStatus.text = "Charging Status: Unknown"
        binding.tvPowerSource.text = "Power Source: Unknown"
        binding.tvBatteryHealth.text = "Health: Unknown"
        binding.tvBatteryTemperature.text = "Temperature: Unknown"
        binding.tvBatteryVoltage.text = "Voltage: Unknown"
        binding.tvBatteryTechnology.text = "Technology: Unknown"
        binding.tvBatteryCapacity.text = "Capacity: Unknown"
        binding.tvBatteryScale.text = "Scale: Unknown"
        binding.tvPowerSaveMode.text = "Power Save Mode: Unknown"
        binding.tvBatteryOptimization.text = "Battery Optimization: Unknown"
        binding.tvLastChargeTime.text = "Last Update: Error"
    }

    override fun onResume() {
        super.onResume()
        // Refresh battery info when fragment becomes visible
        loadBatteryInfo()
        loadPowerManagementInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        batteryReceiver?.let {
            try {
                requireContext().unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w("DevInfo", "Battery receiver already unregistered")
            }
        }
        _binding = null
    }
}
