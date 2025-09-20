package com.sakib.devinfo.ui.apps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sakib.devinfo.R
import java.text.SimpleDateFormat
import java.util.*

class AppAdapter(
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        val tvAppPackage: TextView = itemView.findViewById(R.id.tvAppPackage)
        val tvAppInfo: TextView = itemView.findViewById(R.id.tvAppInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        
        holder.tvAppName.text = app.name
        holder.tvAppPackage.text = app.packageName
        
        val sizeInMB = app.size / (1024 * 1024)
        val appType = if (app.isSystemApp) "System" else "User"
        val status = if (app.isEnabled) "Enabled" else "Disabled"
        holder.tvAppInfo.text = "${app.version} • ${sizeInMB} MB • $appType • $status"
        
        if (app.icon != null) {
            holder.ivAppIcon.setImageDrawable(app.icon)
        } else {
            holder.ivAppIcon.setImageResource(R.drawable.ic_android_black_24dp)
        }

        holder.itemView.setOnClickListener {
            onAppClick(app)
        }
    }

    override fun getItemCount(): Int = apps.size

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}
