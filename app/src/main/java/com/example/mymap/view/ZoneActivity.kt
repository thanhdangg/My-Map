package com.example.mymap.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mymap.R
import com.example.mymap.databinding.ActivityZoneBinding
import com.example.mymap.socket.SocketManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ZoneActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityZoneBinding
    private var zoneStatus = ""
    private var onEnter = false
    private var onLeave = false
    private var zoneName = ""
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private  var socketManager =  SocketManager()
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
        binding.zoneName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                zoneName = binding.zoneName.text.toString()
            }
        }



    }
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

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