package com.sakib.devinfo.ui.apps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sakib.devinfo.R

class PermissionAdapter(private val permissions: List<String>) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPermission: TextView = itemView.findViewById(R.id.tvPermission)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permission, parent, false)
        return PermissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = permissions[position]
        holder.tvPermission.text = permission.removePrefix("android.permission.")
    }

    override fun getItemCount(): Int = permissions.size
}
