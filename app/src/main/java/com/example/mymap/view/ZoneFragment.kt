package com.example.mymap.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mymap.R
import com.example.mymap.databinding.FragmentZoneBinding


class ZoneFragment : Fragment() {

    private lateinit var binding: FragmentZoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentZoneBinding.inflate(inflater, container, false)
        binding.toolbar.inflateMenu(R.menu.toolbar_menu)

        return binding.root
    }


}