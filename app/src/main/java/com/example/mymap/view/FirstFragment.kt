package com.example.mymap.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.example.mymap.R
import com.example.mymap.databinding.FragmentFirstBinding
import com.example.mymap.model.MyApplication
import com.example.mymap.socket.SocketManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var socketManager: SocketManager

    private lateinit var handler: Handler
    private var runnable: Runnable? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 101

    private val locationListener: LocationListener = LocationListener { location ->
        googleMap.clear()
        val currentLocation = LatLng(location.latitude, location.longitude)
        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))

        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("userId", "")
        val userName = sharedPreferences?.getString("userName", "")
        val phoneNumber = sharedPreferences?.getString("phoneNumber", "")

        if (userId != null && userName != null && phoneNumber != null) {
//            Log.d("Tracking_Location", "onLocationChanged User info: $userId, $userName, $phoneNumber")
            (activity?.application as? MyApplication)?.socketManager?.sendTrackingInfo(
                userId,
                userName,
                phoneNumber,
                location.latitude,
                location.longitude
            )
        } else {
            Log.d("Tracking_Location", "User info not available")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val application = context.applicationContext as MyApplication
        socketManager = application.socketManager

        socketManager.onLocationUpdateReceived = { data ->
            Log.d("Tracking_FirstFragment", "Received location update: $data")
            handleFriendLocationUpdate(data)
        }
        socketManager.onFindFriendResult = { result ->
            Log.d("Tracking_FirstFragment", "onFindFriendResult: $result")
            activity?.runOnUiThread {
                val id = result.getString("id")
                val phoneNumber = result.getString("phoneNumber")
                val userName = result.getString("userName")
                val locationX = result.getString("locationX")
                val locationY = result.getString("locationY")
                val friendId = result.getString("friendId")
                val role = result.getString("role")

                Log.d("Tracking_FirstFragment", "Received data: id=$id, phoneNumber=$phoneNumber, userName=$userName, locationX=$locationX, locationY=$locationY, friendId=$friendId, role=$role")

                val friendLocation = LatLng(locationX.toDouble(), locationY.toDouble())
                val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                googleMap.addMarker(MarkerOptions().position(friendLocation).title("Friend's Location").icon(markerColor))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocation, 15f))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { map ->
            googleMap = map
        }

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        handler = Handler(Looper.getMainLooper())

        binding.fab.setOnClickListener {
            requestLocationUpdates()
        }

        binding.btnLocationFriend.setOnClickListener {
            requestFriendLocation()
        }

        return binding.root
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Tracking_Location", "Requesting location permission")
            requestLocationPermission()
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        startPeriodicLocationUpdate()
    }

    private fun startPeriodicLocationUpdate() {
        runnable = Runnable {
            val location: Location? = if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                null
            }
            if (location != null) {
                updateMapLocation(location)
                sendLocationToServer(location)
            }
            handler.postDelayed(runnable!!, 10000)
        }
        handler.post(runnable!!)
    }

    private fun updateMapLocation(location: Location) {
        googleMap.clear()
        val currentLocation = LatLng(location.latitude, location.longitude)
        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
        Log.d("Tracking_Location", "updateMapLocation Latitude: ${location.latitude}, Longitude: ${location.longitude}")
    }

    private fun sendLocationToServer(location: Location) {
        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("userId", "")
        val userName = sharedPreferences?.getString("userName", "")
        val phoneNumber = sharedPreferences?.getString("phoneNumber", "")

        if (userId != null && userName != null && phoneNumber != null) {
            Log.d("Tracking_Location", "Sending location to server: $userId, $userName, $phoneNumber")
            socketManager.sendTrackingInfo(
                userId,
                userName,
                phoneNumber,
                location.latitude,
                location.longitude
            )
        } else {
            Log.d("Tracking_Location", "User info not available")
        }
    }
    private fun requestFriendLocation() {
        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("friend_userId", "")
        val phoneNumber = sharedPreferences?.getString("friend_phoneNumber", "")
        Log.d("Tracking_Location", "Friend info: $userId, $phoneNumber")

        if (userId != null) {
            phoneNumber?.let { socketManager.findFriend(it, userId) }
        } else {
            Log.d("Tracking_Location", "User ID not available")
        }
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user *asynchronously*
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                } else {
                    // Permission denied, disable the functionality that depends on this permission.
                }
                return
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationManager.removeUpdates(locationListener)
        runnable?.let {
            handler.removeCallbacks(it)
        }
        _binding = null
    }

    private fun handleFriendLocationUpdate(data: JSONArray) {
        for (i in 0 until data.length()) {
            val friendLocation = data.getJSONObject(i)
            val locationX = friendLocation.getDouble("locationX")
            val locationY = friendLocation.getDouble("locationY")
            val friendLocationLatLng = LatLng(locationX, locationY)
            val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            googleMap.addMarker(MarkerOptions().position(friendLocationLatLng).title("Friend's Location").icon(markerColor))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocationLatLng, 15f))
            Log.d("Tracking_FirstFragment", "Friend's Location: $friendLocation")

        }
    }
}
