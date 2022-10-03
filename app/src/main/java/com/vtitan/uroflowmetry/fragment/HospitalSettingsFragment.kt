package com.vtitan.uroflowmetry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.adapter.HospitalRecyclerAdapter
import com.vtitan.uroflowmetry.databinding.FragmentHospitalSettingsBinding


class HospitalSettingsFragment : Fragment() {

    private lateinit var binding: FragmentHospitalSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHospitalSettingsBinding.inflate(layoutInflater)

        val recyclerView = binding.hospitalsDetailRecycler
        val adapter = HospitalRecyclerAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL,false)


        binding.addHospitalDetail.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, HospitalSettingsDetailFragment())?.commit()
        }


        return binding.root
    }


}