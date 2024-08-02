package com.example.mymap.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mymap.MainActivity
import com.example.mymap.R
import com.example.mymap.database.AppDatabase
import com.example.mymap.databinding.ActivityZoneBinding
import com.example.mymap.model.ZoneAlert
import com.example.mymap.socket.SocketManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ZoneActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityZoneBinding
    private var zoneStatus = "safe"
    private var onEnter = false
    private var onLeave = false
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var socketManager: SocketManager

    private var zoneLocation: LatLng? = null
    private val locationListener: LocationListener = LocationListener { location ->
        googleMap.clear()
        val currentLocation = LatLng(location.latitude, location.longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZoneBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locationListener)
        }
        binding.fab.setOnClickListener { view ->
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            }
        }

        setSupportActionBar(binding.toolbar)
        binding.icBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
            Log.d("ZoneActivity", "on back clicked")})

        binding.icDone.setOnClickListener(View.OnClickListener {
            Log.d("ZoneActivity", "on done clicked")
            val zoneName = binding.zoneName.text.toString()
            val status = zoneStatus
            val onEnter = binding.alertOnEnter.isChecked
            val onLeave = binding.alertOnLeave.isChecked


            zoneLocation = googleMap.cameraPosition.target
            val currentZoneLocation = zoneLocation

            val radiusInPixels = binding.zoneView.width / 2

            // Chuyển đổi bán kính từ pixel sang mét
            val radiusInMeters = getRadiusInMeters(googleMap, zoneLocation!!, radiusInPixels)

            Log.d("ZoneActivity", "zoneName: $zoneName, status: $status, onEnter: $onEnter, onLeave: $onLeave, zoneLocation: $zoneLocation, radius: $radiusInMeters")

            val db = AppDatabase.getDatabase(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                db.zoneAlertDao().insert(
                    ZoneAlert(
                        zoneName = zoneName,
                        status = status,
                        onEnter = onEnter,
                        onLeave = onLeave,
                        latitude = currentZoneLocation?.latitude ?: 0.0,
                        longitude = currentZoneLocation?.longitude ?: 0.0,
                        radius = radiusInMeters
                    )
                )
            }


            val intent = Intent(this, MainActivity::class.java).apply {
            }
            startActivity(intent)

        })
        binding.statusGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.status_safe -> {
                    binding.zoneView.setBackgroundResource(R.drawable.gradient_circle_safe)
                    zoneStatus = "safe"
                }
                R.id.status_danger -> {
                    binding.zoneView.setBackgroundResource(R.drawable.gradient_circle_danger)
                    zoneStatus = "danger"
                }
            }
        }
        binding.alertOnEnter.setOnCheckedChangeListener { _, isChecked ->
            onEnter = isChecked
        }
        binding.alertOnLeave.setOnCheckedChangeListener { _, isChecked ->
            onLeave = isChecked
        }

        binding.searchLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val locationName = query.toString()

                // Use a Geocoder to get a list of Address objects from the input text
                val geocoder = Geocoder(this@ZoneActivity)
                val addresses = geocoder.getFromLocationName(locationName, 1)

                // Check if the list is not empty
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        // Get the LatLng from the first Address in the list
                        val address = addresses[0]
                        val latLng = LatLng(address.latitude, address.longitude)

                        // Move the camera to the LatLng
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    } else {
                        Toast.makeText(this@ZoneActivity, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }

                // Consume the event
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Do something when the query text changes
                return false
            }
        })

    }

    private fun getRadiusInMeters(map: GoogleMap, center: LatLng, radiusInPixels: Int): Double {
        // Lấy độ dài một pixel theo mét tại vị trí cụ thể
        val metersPerPixel = getMetersPerPixel(map, center)

        // Tính bán kính thực tế
        return radiusInPixels * metersPerPixel
    }

    private fun getMetersPerPixel(map: GoogleMap, center: LatLng): Double {
        val projection = map.projection
        val zoomLevel = map.cameraPosition.zoom
        val equatorLength = 40075004.0 // Chu vi Trái Đất theo mét

        val latLng1 = projection.fromScreenLocation(Point(0, 0))
        val latLng2 = projection.fromScreenLocation(Point(0, 1))

        val distance = FloatArray(1)
        Location.distanceBetween(
            latLng1.latitude, latLng1.longitude,
            latLng2.latitude, latLng2.longitude,
            distance
        )
        return distance[0].toDouble()

//        return distance[0].toDouble() / equatorLength / Math.pow(2.0, zoomLevel.toDouble())
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isTiltGesturesEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.isMyLocationEnabled = true


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locationListener)
        }

    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.map.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding.map.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }
}