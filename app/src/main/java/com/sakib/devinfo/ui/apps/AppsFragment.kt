package com.sakib.devinfo.ui.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.sakib.devinfo.R
import kotlinx.coroutines.*
import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.os.Build
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*

class AppsFragment : Fragment() {

    private lateinit var tvTotalApps: TextView
    private lateinit var tvUserApps: TextView
    private lateinit var tvSystemApps: TextView
    private lateinit var etSearchApps: TextInputEditText
    private lateinit var tabLayout: TabLayout
    private lateinit var rvAppsList: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var appAdapter: AppAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var currentFilter = AppFilter.ALL
    private var searchQuery = ""

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    enum class AppFilter { ALL, USER, SYSTEM }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_apps, container, false)
        
        initViews(root)
        setupRecyclerView()
        setupTabLayout()
        setupSearch()
        loadAllApps()
        
        return root
    }

    private fun initViews(root: View) {
        tvTotalApps = root.findViewById(R.id.tvTotalApps)
        tvUserApps = root.findViewById(R.id.tvUserApps)
        tvSystemApps = root.findViewById(R.id.tvSystemApps)
        etSearchApps = root.findViewById(R.id.etSearchApps)
        tabLayout = root.findViewById(R.id.tabLayout)
        rvAppsList = root.findViewById(R.id.rvAppsList)
        progressBar = root.findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        appAdapter = AppAdapter(emptyList()) { app ->
            showAppDetails(app)
        }
        rvAppsList.layoutManager = LinearLayoutManager(requireContext())
        rvAppsList.adapter = appAdapter
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> AppFilter.ALL
                    1 -> AppFilter.USER
                    2 -> AppFilter.SYSTEM
                    else -> AppFilter.ALL
                }
                filterAndUpdateApps()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        etSearchApps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.lowercase() ?: ""
                filterAndUpdateApps()
            }
        })
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun loadAllApps() {
        coroutineScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val apps = withContext(Dispatchers.IO) {
                    val packageManager = requireContext().packageManager
                    val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                    
                    installedPackages.mapNotNull { packageInfo ->
                        try {
                            createAppInfo(packageInfo, packageManager)
                        } catch (e: Exception) {
                            null // Skip apps that cause errors
                        }
                    }.sortedBy { it.name.lowercase() }
                }
                
                allApps = apps
                updateStatistics()
                filterAndUpdateApps()
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                // Handle error
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun createAppInfo(packageInfo: PackageInfo, packageManager: PackageManager): AppInfo {
        val appInfo = packageInfo.applicationInfo
        
        return AppInfo(
            name = appInfo.loadLabel(packageManager).toString(),
            packageName = packageInfo.packageName,
            version = packageInfo.versionName ?: "Unknown",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            },
            icon = try { appInfo.loadIcon(packageManager) } catch (e: Exception) { null },
            size = getAppSize(appInfo.sourceDir),
            installTime = packageInfo.firstInstallTime,
            updateTime = packageInfo.lastUpdateTime,
            isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
            isEnabled = appInfo.enabled,
            targetSdkVersion = appInfo.targetSdkVersion,
            minSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appInfo.minSdkVersion
            } else {
                0
            },
            permissions = packageInfo.requestedPermissions?.toList() ?: emptyList(),
            sourceDir = appInfo.sourceDir,
            dataDir = appInfo.dataDir,
            uid = appInfo.uid
        )
    }

    private fun getAppSize(sourceDir: String): Long {
        return try {
            java.io.File(sourceDir).length()
        } catch (e: Exception) {
            0L
        }
    }

    private fun updateStatistics() {
        val total = allApps.size
        val user = allApps.count { !it.isSystemApp }
        val system = allApps.count { it.isSystemApp }
        
        tvTotalApps.text = "Total: $total"
        tvUserApps.text = "User: $user"
        tvSystemApps.text = "System: $system"
    }

    private fun filterAndUpdateApps() {
        val filteredApps = allApps.filter { app ->
            val matchesFilter = when (currentFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
            }
            
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                app.name.lowercase().contains(searchQuery) || 
                app.packageName.lowercase().contains(searchQuery)
            }
            
            matchesFilter && matchesSearch
        }
        
        appAdapter.updateApps(filteredApps)
    }

    private fun showAppDetails(app: AppInfo) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_app_details, null)
        
        // Populate dialog views
        val ivAppIcon = dialogView.findViewById<ImageView>(R.id.ivAppIcon)
        val tvAppName = dialogView.findViewById<TextView>(R.id.tvAppName)
        val tvPackageName = dialogView.findViewById<TextView>(R.id.tvPackageName)
        val tvVersion = dialogView.findViewById<TextView>(R.id.tvVersion)
        val tvTargetSdk = dialogView.findViewById<TextView>(R.id.tvTargetSdk)
        val tvMinSdk = dialogView.findViewById<TextView>(R.id.tvMinSdk)
        val tvAppSize = dialogView.findViewById<TextView>(R.id.tvAppSize)
        val tvInstallDate = dialogView.findViewById<TextView>(R.id.tvInstallDate)
        val tvUpdateDate = dialogView.findViewById<TextView>(R.id.tvUpdateDate)
        val tvAppType = dialogView.findViewById<TextView>(R.id.tvAppType)
        val tvAppStatus = dialogView.findViewById<TextView>(R.id.tvAppStatus)
        val tvSourceDir = dialogView.findViewById<TextView>(R.id.tvSourceDir)
        val tvDataDir = dialogView.findViewById<TextView>(R.id.tvDataDir)
        val rvPermissions = dialogView.findViewById<RecyclerView>(R.id.rvPermissions)
        
        // Set values
        if (app.icon != null) {
            ivAppIcon.setImageDrawable(app.icon)
        } else {
            ivAppIcon.setImageResource(R.drawable.ic_android_black_24dp)
        }
        
        tvAppName.text = app.name
        tvPackageName.text = app.packageName
        tvVersion.text = "Version: ${app.version} (${app.versionCode})"
        tvTargetSdk.text = "Target SDK: ${app.targetSdkVersion}"
        tvMinSdk.text = "Min SDK: ${app.minSdkVersion}"
        
        val sizeInMB = app.size / (1024 * 1024)
        tvAppSize.text = "App Size: ${sizeInMB} MB"
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        tvInstallDate.text = "Installed: ${dateFormat.format(Date(app.installTime))}"
        tvUpdateDate.text = "Last Updated: ${dateFormat.format(Date(app.updateTime))}"
        
        tvAppType.text = "Type: ${if (app.isSystemApp) "System App" else "User App"}"
        tvAppStatus.text = "Status: ${if (app.isEnabled) "Enabled" else "Disabled"}"
        
        tvSourceDir.text = "APK Path: ${app.sourceDir}"
        tvDataDir.text = "Data Dir: ${app.dataDir}"
        
        // Setup permissions RecyclerView
        rvPermissions.layoutManager = LinearLayoutManager(requireContext())
        rvPermissions.adapter = PermissionAdapter(app.permissions)
        
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("App Details")
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }
}
