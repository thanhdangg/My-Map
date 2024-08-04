package com.example.mymap.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.mymap.R
import com.example.mymap.database.AppDatabase
import com.example.mymap.model.ZoneAlert
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class NotificationManager(private val context: Context) {
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 102

    fun requestNotificationPermission(activity: androidx.fragment.app.FragmentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("Tracking_Notification", "Requesting notification permission")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
            Log.d("Tracking_Notification", "Notification permission requested")
        }
    }

    fun sendNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "ZONE_ALERT_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(notificationChannelId, "Zone Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        Log.d("Tracking_Notification", "Sending notification: $title, $message")

        val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        Log.d("Tracking_Notification", "Notification sent")
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    fun logZoneData() {
        Log.d("Tracking_Notification", "Logging zone data")
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val zones = db.zoneAlertDao().getAllZoneAlerts()
            if (zones.isEmpty()) {
                Log.d("Tracking_Notification", "No zone data")
            }
            for (zone in zones) {
                Log.d("Tracking_Notification", "Zone: ${zone.zoneName}, Status: ${zone.status}, OnEnter: ${zone.onEnter}, OnLeave: ${zone.onLeave}, Latitude: ${zone.latitude}, Longitude: ${zone.longitude}, Radius: ${zone.radius}")
            }
        }
    }

    fun handleFriendLocationUpdate(data: JSONArray) {
        Log.d("Tracking_Notification", "Handling friend location update")
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val zones = db.zoneAlertDao().getAllZoneAlerts()
            for (i in 0 until data.length()) {
                val friendLocation = data.getJSONObject(i)
                val locationX = friendLocation.getDouble("locationX")
                val locationY = friendLocation.getDouble("locationY")
                val friendLocationLatLng = LatLng(locationX, locationY)

                for (zone in zones) {
                    if (isLocationInZone(friendLocationLatLng, zone)) {
                        if (zone.onEnter) {
                            Log.d("Tracking_Notification", "Friend entered zone: ${zone.zoneName}")
                            sendNotification("Zone Alert", "Friend entered zone: ${zone.zoneName}")
                        }
                    } else {
                        if (zone.onLeave) {
                            Log.d("Tracking_Notification", "Friend left zone: ${zone.zoneName}")
                            sendNotification("Zone Alert", "Friend left zone: ${zone.zoneName}")
                        }
                    }
                }
            }
        }
    }

    private fun isLocationInZone(location: LatLng, zone: ZoneAlert): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, zone.latitude, zone.longitude, results)
        Log.d("Tracking_Notification_Distance", "Distance: ${results[0]}")
        return results[0] <= zone.radius
    }
}