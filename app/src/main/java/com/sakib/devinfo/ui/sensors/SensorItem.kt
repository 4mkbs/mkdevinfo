package com.sakib.devinfo.ui.sensors

data class SensorItem(
    val name: String,
    val type: String,
    val vendor: String,
    val version: String,
    val power: String,
    val resolution: String,
    val maxRange: String,
    val sensorTypeInt: Int,
    val valueHash: Int = 0,
    val values: String = "No data"
)
