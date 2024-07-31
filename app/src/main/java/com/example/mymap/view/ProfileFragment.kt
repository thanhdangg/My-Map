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
import com.example.mymap.databinding.FragmentProfileBinding
import com.example.mymap.model.MyApplication
import com.example.mymap.socket.SocketManager
import org.json.JSONObject


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private  var socketManager =  SocketManager()


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Tracking_ProfileFragment", "onCreate")

        socketManager.onFrinedRequest { userId ->
            Log.d("Tracking_ProfileFragment", "onFriendRequestReceived userId: $userId")
            activity?.runOnUiThread {
                binding.senderId.text = userId
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val application = context?.applicationContext as? MyApplication
        if (application != null) {
            socketManager = application.socketManager
        }

        socketManager.onFrinedRequest { userId ->
            Log.d("Tracking_ProfileFragment", "onFriendRequestReceived userId: $userId")
            activity?.runOnUiThread {
                binding.senderId.text = userId
            }
        }

        binding.btnAccept.setOnClickListener {
            binding.btnAccept.text = "Accepted"
            binding.btnAccept.background = ColorDrawable(Color.parseColor("#00BFFF"))

            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            val userId = sharedPref.getString("userId", null)
            if (userId == null) {
                Log.d("Tracking_ProfileFragment", "UserId is null")
                return@setOnClickListener
            }
            else
            {
                Log.d("Tracking_ProfileFragment", "UserId: $userId")
            }
            val receiverId = binding.senderId.text.toString()
//            var receiverId = ""
//
//            if ( userId == "1"){
//                receiverId = "2"
//            }
//            else{
//                receiverId = "1"
//            }

            Log.d("Tracking_ProfileFragment", "userId $userId receiverId $receiverId")
            socketManager.acceptFriendRequest(userId,receiverId)
        }

        return binding.root
    }

}