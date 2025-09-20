package com.sakib.devinfo.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sakib.devinfo.R
import com.sakib.devinfo.databinding.ActivityMainBinding
import com.sakib.devinfo.ui.dashboard.DashboardFragment
import com.sakib.devinfo.ui.system.SystemFragment
import com.sakib.devinfo.ui.battery.BatteryFragment
import com.sakib.devinfo.ui.network.NetworkFragment
import com.sakib.devinfo.ui.apps.AppsFragment
import com.sakib.devinfo.ui.camera.CameraFragment
import com.sakib.devinfo.ui.sensors.SensorsFragment
import com.sakib.devinfo.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewPager: ViewPager2

    private val fragments = listOf(
        DashboardFragment(),
        SystemFragment(),
        BatteryFragment(),
        NetworkFragment(),
        AppsFragment(),
        CameraFragment(),
        SensorsFragment()
    )

    private val tabTitles = listOf(
        "Dashboard",
        "System", 
        "Battery",
        "Network",
        "Apps",
        "Camera",
        "Sensors"
    )

    private val tabIcons = listOf(
        R.drawable.ic_dashboard,
        R.drawable.ic_system,
        R.drawable.ic_battery,
        R.drawable.ic_network,
        R.drawable.ic_apps,
        R.drawable.ic_camera,
        R.drawable.ic_sensors
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DevInfo", "MainActivity onCreate start")

        preferenceManager = PreferenceManager(this)
        applyTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("DevInfo", "MainActivity layout set")

        setupViewPager()
        Log.d("DevInfo", "ViewPager setup complete")
    }

    private fun applyTheme() {
        when (preferenceManager.getTheme()) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setupViewPager() {
        viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        // Set up ViewPager2 with adapter
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect ViewPager2 with TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(tabIcons[position])
        }.attach()

        // Log page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("DevInfo", "Page selected: ${tabTitles[position]}")
            }
        })

        // Start with Dashboard
        viewPager.currentItem = 0
    }

    private inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}
