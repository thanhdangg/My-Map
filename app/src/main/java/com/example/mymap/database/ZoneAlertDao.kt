package com.example.mymap.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.mymap.model.ZoneAlert

@Dao
interface ZoneAlertDao {
    @Insert
    suspend fun insert(zoneAlertEntity: ZoneAlert)

//    @Delete
//    suspend fun delete(zoneAlert: ZoneAlert)

    @Query("SELECT * FROM zone_alerts")
    suspend fun getAllZoneAlerts(): List<ZoneAlert>
//
//    @Query("SELECT * FROM zone_alerts WHERE zoneName = :name")
//    suspend fun getByName(name: String): List<ZoneAlert>
}
