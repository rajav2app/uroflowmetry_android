package com.vtitan.uroflowmetry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vtitan.uroflowmetry.BuildConfig
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentAboutUsBinding

class AboutUsFragment : Fragment() {

    private lateinit var binding: FragmentAboutUsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAboutUsBinding.inflate(layoutInflater)
        binding.buildNumber.text = "${BuildConfig.VERSION_CODE}"
        binding.buildVersion.text = "${BuildConfig.VERSION_NAME}"
        // Inflate the layout for this fragment
        return binding.root
    }


}