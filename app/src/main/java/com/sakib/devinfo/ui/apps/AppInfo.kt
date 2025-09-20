package com.sakib.devinfo.ui.apps

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val version: String,
    val versionCode: Long,
    val icon: Drawable?,
    val size: Long,
    val installTime: Long,
    val updateTime: Long,
    val isSystemApp: Boolean,
    val isEnabled: Boolean,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val permissions: List<String>,
    val sourceDir: String,
    val dataDir: String,
    val uid: Int
)
