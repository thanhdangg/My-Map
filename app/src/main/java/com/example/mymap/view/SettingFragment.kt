package com.example.mymap.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mymap.R
import com.example.mymap.databinding.FragmentSettingBinding
import com.example.mymap.model.MyApplication
import com.example.mymap.socket.SocketManager


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        binding.btnConnectServer.setOnClickListener {
            val userId = binding.userId.text.toString()
            val application = requireActivity().application as MyApplication
            application.socketManager = SocketManager()
            application.socketManager.connect()
            application.socketManager.register(userId.toInt())

        }


        return binding.root
    }


}