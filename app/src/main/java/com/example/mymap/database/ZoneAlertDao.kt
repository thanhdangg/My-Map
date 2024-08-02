package com.example.mymap.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mymap.model.ZoneAlertEntity

@Dao
interface ZoneAlertDao {
    @Insert
    suspend fun insert(zoneAlertEntity: ZoneAlertEntity)

    @Query("SELECT * FROM zone_alerts")
    suspend fun getAllZoneAlerts(): List<ZoneAlertEntity>
}
