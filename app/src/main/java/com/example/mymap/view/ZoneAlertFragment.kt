package com.example.mymap.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.mymap.R
import com.example.mymap.databinding.FragmentSettingBinding
import com.example.mymap.databinding.FragmentZoneAlertBinding

class ZoneAlertFragment : Fragment() {
    private lateinit var binding: FragmentZoneAlertBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentZoneAlertBinding.inflate(inflater, container, false)

        binding.btnCreateZoneAlert.setOnClickListener {
//            val transaction = parentFragmentManager.beginTransaction()
//            transaction.replace(R.id.nav_host_fragment_content_main, ZoneFragment())
//            transaction.addToBackStack(null)
//            transaction.commit()
//            findNavController().navigate(R.id.zoneFragment)

            val intent = Intent(activity, ZoneActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


}