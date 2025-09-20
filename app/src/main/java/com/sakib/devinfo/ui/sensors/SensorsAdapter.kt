package com.sakib.devinfo.ui.sensors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sakib.devinfo.R

class SensorsAdapter : ListAdapter<SensorItem, SensorsAdapter.SensorViewHolder>(Diff()) {

    class SensorViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvSensorName: TextView = view.findViewById(R.id.tvSensorName)
        val tvSensorType: TextView = view.findViewById(R.id.tvSensorType)
        val tvSensorVendor: TextView = view.findViewById(R.id.tvSensorVendor)
        val tvSensorVersion: TextView = view.findViewById(R.id.tvSensorVersion)
        val tvSensorPower: TextView = view.findViewById(R.id.tvSensorPower)
        val tvSensorResolution: TextView = view.findViewById(R.id.tvSensorResolution)
        val tvSensorMaxRange: TextView = view.findViewById(R.id.tvSensorMaxRange)
        val tvSensorValues: TextView = view.findViewById(R.id.tvSensorValues)
    }

    class Diff : DiffUtil.ItemCallback<SensorItem>() {
        override fun areItemsTheSame(oldItem: SensorItem, newItem: SensorItem): Boolean =
            oldItem.sensorTypeInt == newItem.sensorTypeInt
        override fun areContentsTheSame(oldItem: SensorItem, newItem: SensorItem): Boolean =
            oldItem.valueHash == newItem.valueHash && oldItem.values == newItem.values
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = getItem(position)
        holder.tvSensorName.text = sensor.name
        holder.tvSensorType.text = "Type: ${sensor.type}"
        holder.tvSensorVendor.text = "Vendor: ${sensor.vendor}"
        holder.tvSensorVersion.text = "Version: ${sensor.version}"
        holder.tvSensorPower.text = "Power: ${sensor.power}"
        holder.tvSensorResolution.text = "Resolution: ${sensor.resolution}"
        holder.tvSensorMaxRange.text = "Max Range: ${sensor.maxRange}"
        holder.tvSensorValues.text = "Values: ${sensor.values}"
    }
}
