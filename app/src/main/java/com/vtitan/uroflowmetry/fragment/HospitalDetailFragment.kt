package com.vtitan.uroflowmetry.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.vtitan.uroflowmetry.databinding.FragmentHospitalDetailBinding
import com.vtitan.uroflowmetry.util.SessionManager
import java.io.ByteArrayOutputStream


class HospitalDetailFragment : Fragment() {

    private lateinit var binding: FragmentHospitalDetailBinding
    private lateinit var  sessionManager:SessionManager


    private var hospitalLogo:String?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHospitalDetailBinding.inflate(layoutInflater)
        sessionManager = SessionManager(requireContext())
        binding.selectImage.setOnClickListener {
            getLogo()
        }

        if (sessionManager.getDefaultSetting()){
            binding.hospitalNameText.setText(sessionManager.getHospitalName())
            binding.hospitalAddressText.setText(sessionManager.getAddress())
            val imageBytes = Base64.decode(sessionManager.getLogo(), Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val scaledBmp = Bitmap.createScaledBitmap(decodedImage,100,100,false)
            binding.finalImage.setImageBitmap(scaledBmp)
        }

        binding.saveButton.setOnClickListener {
            if (!binding.hospitalAddressText.text.isEmpty() && !binding.hospitalNameText.text.isEmpty() && (hospitalLogo != null || sessionManager.getDefaultSetting()) ) {
                sessionManager.setHospitalName(binding.hospitalNameText.text.toString())
                sessionManager.setAddress(binding.hospitalAddressText.text.toString())
                if (hospitalLogo!=null) {
                    sessionManager.setLogo(hospitalLogo!!)
                }
                sessionManager.setDefaultSetting(true)
                Toast.makeText(requireContext(),resources.getString(R.string.hospital_details_saved),Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(),resources.getString(R.string.hospital_details_failed),Toast.LENGTH_SHORT).show()
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