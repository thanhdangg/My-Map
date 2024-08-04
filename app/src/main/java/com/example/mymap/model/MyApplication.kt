package com.example.mymap.model

import android.app.Application
import com.example.mymap.notification.NotificationManager
import com.example.mymap.socket.SocketManager

class MyApplication : Application() {
    lateinit var socketManager: SocketManager
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        socketManager = SocketManager(this)
        notificationManager = NotificationManager(this)

    }
}