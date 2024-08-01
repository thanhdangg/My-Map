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
import androidx.navigation.fragment.findNavController
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
        socketManager.onFindFriendResult = { result ->
            Log.d("Tracking_ProfileFragment", "onFindFriendResult: $result")
            activity?.runOnUiThread {
                val id = result.getString("id")
                val phoneNumber = result.getString("phoneNumber")
                val userName = result.getString("userName")
                val locationX = result.getString("locationX")
                val locationY = result.getString("locationY")
                val friendId = result.getString("friendId")
                val role = result.getString("role")

                saveUserInfo(requireContext(), id, userName, phoneNumber, locationX.toDouble(), locationY.toDouble())

                val friendInfo = "ID: $id\nPhone Number: $phoneNumber\nUser Name: $userName\nLocation: ($locationX, $locationY)\nFriend ID: $friendId\nRole: $role"
                binding.friendInfo.visibility = View.VISIBLE
                binding.friendInfo.text = friendInfo
                binding.btnViewLocation.visibility = View.VISIBLE
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

            Log.d("Tracking_ProfileFragment", "userId $userId receiverId $receiverId")
            socketManager.acceptFriendRequest(userId,receiverId)
        }

        binding.btnFindFriend.setOnClickListener {
            val phoneNumber = binding.phoneNumberInput.text.toString()
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            val userId = sharedPref.getString("userId", null)
            if (userId == null) {
                Log.d("Tracking_ProfileFragment", "UserId is null")
                return@setOnClickListener
            }
            else
            {
                Log.d("Tracking_ProfileFragment", "UserId: $userId phoneNumber: $phoneNumber")
            }
            socketManager.findFriend(phoneNumber, userId)
        }
        binding.btnViewLocation.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_FirstFragment)
        }

        return binding.root
    }
    fun saveUserInfo(context: Context, userId: String, userName: String, phoneNumber: String, locationX: Double, locationY: Double) {
        val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("friend_userId", userId)
        editor.putString("friend_userName", userName)
        editor.putString("friend_phoneNumber", phoneNumber)
        editor.putString("friend_locationX", locationX.toString())
        editor.putString("friend_locationY", locationY.toString())
        editor.apply()
    }

}