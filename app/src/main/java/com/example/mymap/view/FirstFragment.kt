package com.example.mymap.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.mymap.R
import com.example.mymap.databinding.FragmentFirstBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    val LOCATION_PERMISSION_REQUEST_CODE  = 101

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
            googleMap.clear()
            val currentLocation = LatLng(location.latitude, location.longitude)
            googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
//            Log.d("Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
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
        binding.fab.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions if not granted
                Log.d("Location", "Requesting location permission")
                requestLocationPermission()

                return@setOnClickListener
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                Log.d("Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            }
            else {
                Log.d("Location", "Location not available")
            }
        }
        return binding.root

    }
    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user *asynchronously*
            // After the user sees the explanation, try again to request the permission.
        } else {
            // No explanation needed; request the permission
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
                    // Permission was granted, yay! Do the location-related task you need to do.
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
                    // Permission denied, boo! Disable the functionality that depends on this permission.
                }
                return
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
    }

    override fun onDestroyView() {
            super.onDestroyView()
        locationManager.removeUpdates(locationListener)

        _binding = null
        }
}