package com.example.mymap.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zone_alerts")
data class ZoneAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val zoneName: String,
    val status: String,
    val onEnter: Boolean,
    val onLeave: Boolean,
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)
