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
import com.example.mymap.R
import com.example.mymap.databinding.FragmentFriendBinding
import com.example.mymap.model.MyApplication
import com.example.mymap.socket.SocketManager
import org.json.JSONObject


class FriendFragment : Fragment() {

    private lateinit var socketManager: SocketManager
    private lateinit var binding: FragmentFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendBinding.inflate(inflater, container, false)

        val application = context?.applicationContext as? MyApplication
        if (application != null) {
            socketManager = application.socketManager
        }
        socketManager.on("friend-accepted") { args ->
            activity?.runOnUiThread {
                binding.btnSendRequest.text = "Accepted"
            }
        }
        binding.btnSendRequest.setOnClickListener {
            binding.btnSendRequest.text = "Sent"
            binding.btnSendRequest.background = ColorDrawable(Color.parseColor("#00BFFF"))
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            val senderId = sharedPref.getString("userId", null)

            if (senderId == null) {
                Log.d("Tracking_FriendFragment", "SenderId is null")
                return@setOnClickListener
            }

            val receiverId = binding.receiverId.text.toString()

            socketManager.sendFriendRequest(senderId, receiverId)

        }

        return binding.root
    }

}