package com.example.mymap.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mymap.databinding.FragmentSettingBinding
import com.example.mymap.model.MyApplication
import com.example.mymap.socket.SocketManager

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        binding.btnConnectServer.setOnClickListener {
            val userId = binding.userId.text.toString()
            val application = context?.applicationContext as? MyApplication
            if (application == null) {
                Log.d("Tracking_SettingFragment", "Application is null")
                return@setOnClickListener
            }

            if (application.socketManager == null) {
                application.socketManager = SocketManager(requireContext())
                Log.d("Tracking_SettingFragment", "Socket null")

            } else {
                application.socketManager.connectToServer()
                Log.d("Tracking_SettingFragment", "Socket connected")
            }

            try {
                application.socketManager.register(userId.toInt())
                val sharedPref = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                sharedPref?.edit()?.apply {
                    putString("userId", userId)
                    apply()
                }
                Log.d("Tracking_SettingFragment", "User info saved: $userId")

                application.socketManager.onUserInfoReceived = { userInfo ->
                    Log.d("Tracking_SettingFragment", "Received user info: $userInfo")

                    val userName = userInfo.getString("userName")
                    val phoneNumber = userInfo.getString("phoneNumber")
                    Log.d("Tracking_SettingFragment", "User info: $userId, $userName, $phoneNumber")

                    val sharedPref = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    sharedPref?.edit()?.apply {
                        putString("userName", userName)
                        putString("phoneNumber", phoneNumber)
                        Log.d("Tracking_SettingFragment", "User info saved: $userId, $userName, $phoneNumber")
                        apply()
                    }
                }
                application.socketManager.getUserInfo(userId.toInt())
                binding.btnConnectServer.text = "Connected to server"
                binding.btnConnectServer.background = ColorDrawable(Color.parseColor("#00BFFF"))
                binding.btnConnectServer.isEnabled = false
            } catch (e: Exception) {
                Log.d("Tracking_SettingFragment", "Exception Error: ${e.message}")
            }
        }

        return binding.root
    }
}