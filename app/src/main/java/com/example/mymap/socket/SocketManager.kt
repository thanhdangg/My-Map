package com.example.mymap.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class SocketManager {
    private var socket: Socket? = null

    fun connect() {
        try {
//            socket = IO.socket("http://call-theme.dtppub.com")
            socket = IO.socket("http://192.168.1.216:5000")

            socket?.connect()
            register(1)
        }
        catch (e: Exception) {
            Log.d("Exception Socket", "Connection error: ${e.message}")
        }

        socket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
            // Handle connection
            Log.d("Sockettttt", "Connected")
        })
        socket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            // Handle disconnection
            Log.d("Socket", "Disconnected")
        })
        socket?.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener { args ->
            val error = args[0] as Exception
            // Handle connection error
            Log.d("Sockettttt", "Connection error: ${error.message}")
        })

        socket?.on("friend-request", Emitter.Listener { args ->
            val data = args[0] as JSONObject
            val final = data.getJSONObject("final")
            val userId = final.getString("userId")
            val userName = final.getString("userName")
            val phoneNumber = final.getString("phoneNumber")
            val locationX = final.getDouble("locationX")
            val locationY = final.getDouble("locationY")
            // Handle friend request
        })

        socket?.on("friend-accepted", Emitter.Listener { args ->
            val data = args[0] as JSONObject
            val receiverInfo = data.getJSONObject("receiverInfo")
            val userId = receiverInfo.getString("userId")
            val userName = receiverInfo.getString("userName")
            val phoneNumber = receiverInfo.getString("phoneNumber")
            val locationX = receiverInfo.getDouble("locationX")
            val locationY = receiverInfo.getDouble("locationY")
            // Handle friend accepted
        })
    }

    fun register(userId: Int) {
        socket?.emit("register", userId)
    }

    fun sendFriendRequest(senderId: String, receiverId: String) {
        val data = JSONObject()
        data.put("senderId", senderId)
        data.put("receiverId", receiverId)
        socket?.emit("send-friend-request", data)
    }

    fun acceptFriendRequest(senderId: String, receiverId: String) {
        val data = JSONObject()
        data.put("senderId", senderId)
        data.put("receiverId", receiverId)
        socket?.emit("accept-friend-request", data)
    }

    fun disconnect() {
        socket?.disconnect()
    }
}