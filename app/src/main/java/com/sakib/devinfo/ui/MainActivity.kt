package com.sakib.devinfo.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sakib.devinfo.R
import com.sakib.devinfo.databinding.ActivityMainBinding
import com.sakib.devinfo.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    Log.d("DevInfo", "MainActivity onCreate start")

        preferenceManager = PreferenceManager(this)
        applyTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    Log.d("DevInfo", "MainActivity layout set")

        setupBottomNavigation()
    Log.d("DevInfo", "Bottom navigation setup complete")
    }

    private fun applyTheme() {
        when (preferenceManager.getTheme()) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setupBottomNavigation() {
    val navView: BottomNavigationView = binding.navView
    val navController = findNavController(R.id.nav_host_fragment_activity_main)
    Log.d("DevInfo", "Obtained NavController: $navController")

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_hardware,
                R.id.navigation_system,
                R.id.navigation_battery,
                R.id.navigation_network,
                R.id.navigation_apps,
                R.id.navigation_camera,
                R.id.navigation_sensors
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        Log.d("DevInfo", "NavController + BottomNav wired")

        // Log destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d(
                "DevInfo",
                "Destination changed -> ${destination.displayName} (id=${destination.id})"
            )
        }

        // Fallback: after a short delay, ensure a child fragment exists; if not, force navigation
        binding.root.postDelayed({
            try {
                val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                val children = host?.childFragmentManager?.fragments ?: emptyList()
                if (children.isEmpty()) {
                    Log.w("DevInfo", "No child fragments found in NavHost after delay; forcing navigate to dashboard")
                    navController.navigate(R.id.navigation_dashboard)
                } else {
                    Log.d("DevInfo", "Child fragments present: ${children.map { it::class.simpleName }}")
                }
            } catch (e: Exception) {
                Log.e("DevInfo", "Fallback navigation error", e)
            }
        }, 1200)
    }
}
