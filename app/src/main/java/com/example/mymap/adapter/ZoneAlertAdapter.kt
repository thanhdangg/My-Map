package com.example.mymap.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymap.R
import com.example.mymap.model.ZoneAlert

class ZoneAlertAdapter(private val zoneAlerts: List<ZoneAlert>) : RecyclerView.Adapter<ZoneAlertAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val zoneName: TextView = view.findViewById(R.id.zone_name)
        val zoneImage: ImageView = view.findViewById(R.id.zone_image)
        val zoneDescription: TextView = view.findViewById(R.id.zone_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.zone_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val zoneAlert = zoneAlerts[position]
        holder.zoneName.text = zoneAlert.zoneName
        val color = if (zoneAlert.status == "safe") {
            Color.parseColor("#00FF00")
        } else {
            Color.parseColor("#FF0000")
        }
        holder.zoneImage.setBackgroundColor(color)
        holder.zoneDescription.text = "Location: (${zoneAlert.latitude}, ${zoneAlert.longitude}), " +
                "\nRadius: ${zoneAlert.radius}m, " +
                "\nOn Enter: ${zoneAlert.onEnter}, " +
                "On Leave: ${zoneAlert.onLeave}"

    }

    override fun getItemCount() = zoneAlerts.size
}