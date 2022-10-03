package com.vtitan.uroflowmetry.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentHospitalSettingsDetailBinding
import com.vtitan.uroflowmetry.util.SessionManager
import java.io.ByteArrayOutputStream

class HospitalSettingsDetailFragment : Fragment() {

    private lateinit var binding: FragmentHospitalSettingsDetailBinding
    private lateinit var  sessionManager: SessionManager


    private var hospitalLogo:String?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHospitalSettingsDetailBinding.inflate(layoutInflater)
        sessionManager = SessionManager(requireContext())


        binding.selectImage.setOnClickListener {
            getLogo()
        }

        binding.btnCancel.setOnClickListener {
            binding.hospitalAddressText.setText("")
            binding.hospitalNameText.setText("")
            binding.finalImage.setImageResource(R.drawable.ic_image)
        }

        binding.startNewTest.setOnClickListener {
            if (!binding.hospitalAddressText.text.isEmpty() && !binding.hospitalNameText.text.isEmpty() && (hospitalLogo != null || sessionManager.getDefaultSetting()) ) {
//                sessionManager.setHospitalName(binding.hospitalNameText.text.toString())
//                sessionManager.setAddress(binding.hospitalAddressText.text.toString())
//                if (hospitalLogo!=null) {
//                    sessionManager.setLogo(hospitalLogo!!)
//                }
                sessionManager.setDefaultSetting(true)
                Toast.makeText(requireContext(),resources.getString(R.string.hospital_details_saved),
                    Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(),resources.getString(R.string.hospital_details_failed),Toast.LENGTH_SHORT).show()
                if (binding.hospitalNameText.text.isEmpty()){
                    binding.hospitalNameText.error="Hospital Name should not be empty."
                }
                if (binding.hospitalAddressText.text.isEmpty()){
                    binding.hospitalAddressText.error="Hospital Address should not be empty."
                }
                if (hospitalLogo == null || !sessionManager.getDefaultSetting()){
                    binding.error.visibility = View.VISIBLE
                }
            }
        }

        return binding.root
    }


    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            if(it!=null) {
                val temp: Bitmap? =
                    MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), it)
                val baos = ByteArrayOutputStream()
                temp?.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                val scaledBmp = Bitmap.createScaledBitmap(temp!!, 100, 100, false)
                hospitalLogo = encodedImage
                binding.finalImage.setImageBitmap(scaledBmp!!)
            }
        }
    )

    private fun getLogo() {
        getImage.launch("image/*")

    }


}