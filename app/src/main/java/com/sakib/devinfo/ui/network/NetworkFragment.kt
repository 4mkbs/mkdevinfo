package com.sakib.devinfo.ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sakib.devinfo.R
import java.net.NetworkInterface
import java.util.*
import android.bluetooth.BluetoothAdapter
import android.annotation.SuppressLint
import android.os.Build

class NetworkFragment : Fragment() {

    private lateinit var tvConnectionStatus: TextView
    private lateinit var tvNetworkType: TextView
    private lateinit var tvNetworkOperator: TextView
    private lateinit var tvWifiStatus: TextView
    private lateinit var tvWifiSSID: TextView
    private lateinit var tvWifiSignalStrength: TextView
    private lateinit var tvWifiFrequency: TextView
    private lateinit var tvCellularType: TextView
    private lateinit var tvSignalStrength: TextView
    private lateinit var tvCarrierName: TextView
    private lateinit var tvCellularData: TextView
    private lateinit var tvLocalIP: TextView
    private lateinit var tvSubnetMask: TextView
    private lateinit var tvGateway: TextView
    private lateinit var tvDNS: TextView
    private lateinit var tvMacAddress: TextView
    private lateinit var tvBluetoothStatus: TextView
    private lateinit var tvDataUsage: TextView

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiManager: WifiManager
    private lateinit var telephonyManager: TelephonyManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_network, container, false)
        
        initViews(root)
        initManagers()
        setupNetworkInfo()
        
        return root
    }

    private fun initViews(root: View) {
        tvConnectionStatus = root.findViewById(R.id.tvConnectionStatus)
        tvNetworkType = root.findViewById(R.id.tvNetworkType)
        tvNetworkOperator = root.findViewById(R.id.tvNetworkOperator)
        tvWifiStatus = root.findViewById(R.id.tvWifiStatus)
        tvWifiSSID = root.findViewById(R.id.tvWifiSSID)
        tvWifiSignalStrength = root.findViewById(R.id.tvWifiSignalStrength)
        tvWifiFrequency = root.findViewById(R.id.tvWifiFrequency)
        tvCellularType = root.findViewById(R.id.tvCellularType)
        tvSignalStrength = root.findViewById(R.id.tvSignalStrength)
        tvCarrierName = root.findViewById(R.id.tvCarrierName)
        tvCellularData = root.findViewById(R.id.tvCellularData)
        tvLocalIP = root.findViewById(R.id.tvLocalIP)
        tvSubnetMask = root.findViewById(R.id.tvSubnetMask)
        tvGateway = root.findViewById(R.id.tvGateway)
        tvDNS = root.findViewById(R.id.tvDNS)
        tvMacAddress = root.findViewById(R.id.tvMacAddress)
        tvBluetoothStatus = root.findViewById(R.id.tvBluetoothStatus)
        tvDataUsage = root.findViewById(R.id.tvDataUsage)
    }

    private fun initManagers() {
        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @SuppressLint("MissingPermission")
    private fun setupNetworkInfo() {
        setupConnectionStatus()
        setupWifiInfo()
        setupCellularInfo()
        setupNetworkDetails()
        setupDeviceNetworkInfo()
    }

    private fun setupConnectionStatus() {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (networkCapabilities != null) {
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                tvConnectionStatus.text = "Status: ${if (isConnected) "Connected" else "Disconnected"}"
                
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        tvNetworkType.text = "Network Type: WiFi"
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        tvNetworkType.text = "Network Type: Cellular"
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        tvNetworkType.text = "Network Type: Ethernet"
                    }
                    else -> {
                        tvNetworkType.text = "Network Type: Other"
                    }
                }
            } else {
                tvConnectionStatus.text = "Status: No active connection"
                tvNetworkType.text = "Network Type: None"
            }
            
            // Network operator
            try {
                val operatorName = telephonyManager.networkOperatorName
                tvNetworkOperator.text = "Operator: ${if (operatorName.isNotEmpty()) operatorName else "Unknown"}"
            } catch (e: Exception) {
                tvNetworkOperator.text = "Operator: Permission required"
            }
            
        } catch (e: Exception) {
            tvConnectionStatus.text = "Status: Error getting status"
            tvNetworkType.text = "Network Type: Error"
            tvNetworkOperator.text = "Operator: Error"
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupWifiInfo() {
        try {
            val wifiInfo = wifiManager.connectionInfo
            
            if (wifiManager.isWifiEnabled) {
                tvWifiStatus.text = "WiFi Status: Enabled"
                
                if (wifiInfo != null && wifiInfo.networkId != -1) {
                    // Connected to WiFi
                    val ssid = wifiInfo.ssid?.removePrefix("\"")?.removeSuffix("\"") ?: "Unknown"
                    tvWifiSSID.text = "SSID: $ssid"
                    
                    val rssi = wifiInfo.rssi
                    val signalLevel = WifiManager.calculateSignalLevel(rssi, 5)
                    tvWifiSignalStrength.text = "Signal Strength: $rssi dBm (Level: $signalLevel/4)"
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val frequency = wifiInfo.frequency
                        tvWifiFrequency.text = "Frequency: $frequency MHz"
                    } else {
                        tvWifiFrequency.text = "Frequency: Not available (API < 21)"
                    }
                } else {
                    tvWifiSSID.text = "SSID: Not connected"
                    tvWifiSignalStrength.text = "Signal Strength: Not connected"
                    tvWifiFrequency.text = "Frequency: Not connected"
                }
            } else {
                tvWifiStatus.text = "WiFi Status: Disabled"
                tvWifiSSID.text = "SSID: WiFi disabled"
                tvWifiSignalStrength.text = "Signal Strength: WiFi disabled"
                tvWifiFrequency.text = "Frequency: WiFi disabled"
            }
        } catch (e: Exception) {
            tvWifiStatus.text = "WiFi Status: Error getting info"
            tvWifiSSID.text = "SSID: Error"
            tvWifiSignalStrength.text = "Signal Strength: Error"
            tvWifiFrequency.text = "Frequency: Error"
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupCellularInfo() {
        try {
            // Network type
            val networkType = when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                else -> "Unknown (${telephonyManager.networkType})"
            }
            tvCellularType.text = "Type: $networkType"
            
            // Signal strength - simplified for now
            tvSignalStrength.text = "Signal Strength: Monitoring..."
            
            // Carrier name
            val carrierName = telephonyManager.networkOperatorName
            tvCarrierName.text = "Carrier: ${if (carrierName.isNotEmpty()) carrierName else "Unknown"}"
            
            // Data connection status
            val dataState = when (telephonyManager.dataState) {
                TelephonyManager.DATA_CONNECTED -> "Connected"
                TelephonyManager.DATA_CONNECTING -> "Connecting"
                TelephonyManager.DATA_DISCONNECTED -> "Disconnected"
                TelephonyManager.DATA_SUSPENDED -> "Suspended"
                else -> "Unknown"
            }
            tvCellularData.text = "Data Connection: $dataState"
            
        } catch (e: SecurityException) {
            tvCellularType.text = "Type: Permission required"
            tvSignalStrength.text = "Signal Strength: Permission required"
            tvCarrierName.text = "Carrier: Permission required"
            tvCellularData.text = "Data Connection: Permission required"
        } catch (e: Exception) {
            tvCellularType.text = "Type: Error"
            tvSignalStrength.text = "Signal Strength: Error"
            tvCarrierName.text = "Carrier: Error"
            tvCellularData.text = "Data Connection: Error"
        }
    }

    private fun setupNetworkDetails() {
        try {
            // Get local IP address
            val localIP = getLocalIPAddress()
            tvLocalIP.text = "Local IP: ${localIP ?: "Unknown"}"
            
            // Get WiFi network details if connected
            if (wifiManager.isWifiEnabled) {
                val dhcpInfo = wifiManager.dhcpInfo
                
                if (dhcpInfo != null) {
                    val gateway = intToIp(dhcpInfo.gateway)
                    val netmask = intToIp(dhcpInfo.netmask)
                    val dns1 = intToIp(dhcpInfo.dns1)
                    val dns2 = intToIp(dhcpInfo.dns2)
                    
                    tvGateway.text = "Gateway: $gateway"
                    tvSubnetMask.text = "Subnet Mask: $netmask"
                    
                    val dnsServers = mutableListOf<String>()
                    if (dns1 != "0.0.0.0") dnsServers.add(dns1)
                    if (dns2 != "0.0.0.0") dnsServers.add(dns2)
                    
                    tvDNS.text = "DNS Servers: ${if (dnsServers.isNotEmpty()) dnsServers.joinToString(", ") else "Unknown"}"
                } else {
                    tvGateway.text = "Gateway: Not available"
                    tvSubnetMask.text = "Subnet Mask: Not available"
                    tvDNS.text = "DNS Servers: Not available"
                }
            } else {
                tvGateway.text = "Gateway: WiFi disabled"
                tvSubnetMask.text = "Subnet Mask: WiFi disabled"
                tvDNS.text = "DNS Servers: WiFi disabled"
            }
            
        } catch (e: Exception) {
            tvLocalIP.text = "Local IP: Error"
            tvGateway.text = "Gateway: Error"
            tvSubnetMask.text = "Subnet Mask: Error"
            tvDNS.text = "DNS Servers: Error"
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupDeviceNetworkInfo() {
        try {
            // MAC Address (Note: Restricted on newer Android versions)
            val macAddress = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    "MAC Address restricted (Android 6.0+)"
                } else {
                    wifiManager.connectionInfo.macAddress ?: "Unknown"
                }
            } catch (e: Exception) {
                "Permission required"
            }
            tvMacAddress.text = "MAC Address: $macAddress"
            
            // Bluetooth status
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothStatus = when {
                bluetoothAdapter == null -> "Not supported"
                bluetoothAdapter.isEnabled -> "Enabled"
                else -> "Disabled"
            }
            tvBluetoothStatus.text = "Bluetooth: $bluetoothStatus"
            
            // Data roaming
            val isRoaming = try {
                telephonyManager.isNetworkRoaming
            } catch (e: Exception) {
                false
            }
            tvDataUsage.text = "Data Roaming: ${if (isRoaming) "Enabled" else "Disabled"}"
            
        } catch (e: Exception) {
            tvMacAddress.text = "MAC Address: Error"
            tvBluetoothStatus.text = "Bluetooth: Error"
            tvDataUsage.text = "Data Roaming: Error"
        }
    }

    private fun getLocalIPAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val hostAddress = address.hostAddress
                        if (hostAddress != null && hostAddress.indexOf(':') < 0) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun intToIp(ip: Int): String {
        return String.format(
            Locale.getDefault(),
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }
}
