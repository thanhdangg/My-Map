package com.example.mymap.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymap.R
import com.example.mymap.adapter.ZoneAlertAdapter
import com.example.mymap.database.AppDatabase
import com.example.mymap.databinding.FragmentSettingBinding
import com.example.mymap.databinding.FragmentZoneAlertBinding
import com.example.mymap.model.ZoneAlert
import kotlinx.coroutines.launch

class ZoneAlertFragment : Fragment() {
    private lateinit var binding: FragmentZoneAlertBinding
    private lateinit var zoneAlertAdapter: ZoneAlertAdapter
    private val zoneAlerts = mutableListOf<ZoneAlert>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZoneAlertBinding.inflate(inflater, container, false)
        zoneAlertAdapter = ZoneAlertAdapter(zoneAlerts)
        binding.rvZoneAlert.adapter = zoneAlertAdapter
        binding.rvZoneAlert.layoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        loadZoneAlerts()


        binding.btnCreateZoneAlert.setOnClickListener {
            val intent = Intent(activity, ZoneActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addZoneAlert(zoneAlert: ZoneAlert) {
//        zoneAlerts.add(zoneAlert)
//        zoneAlertAdapter.notifyDataSetChanged()
        val db = AppDatabase.getDatabase(requireContext())
        val zoneAlertEntity = ZoneAlert(
            zoneName = zoneAlert.zoneName,
            status = zoneAlert.status,
            onEnter = zoneAlert.onEnter,
            onLeave = zoneAlert.onLeave,
            latitude = zoneAlert.latitude,
            longitude = zoneAlert.longitude,
            radius = zoneAlert.radius
        )
        lifecycleScope.launch {
            db.zoneAlertDao().insert(zoneAlertEntity)
            loadZoneAlerts()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun loadZoneAlerts() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val alerts = db.zoneAlertDao().getAllZoneAlerts()
            zoneAlerts.clear()
            zoneAlerts.addAll(alerts.map {
                ZoneAlert(
                    zoneName = it.zoneName,
                    status = it.status,
                    onEnter = it.onEnter,
                    onLeave = it.onLeave,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    radius = it.radius
                )
            })
            zoneAlertAdapter.notifyDataSetChanged()
        }
    }


}