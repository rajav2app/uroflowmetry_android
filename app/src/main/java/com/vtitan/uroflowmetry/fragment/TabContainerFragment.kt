package com.example.uroflowmetryapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat.setBackgroundTintList
import com.example.uroflowmetryapp.util.DeviceConfigurationTabAdapter
import com.google.android.material.tabs.TabLayout
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentTabContainerBinding
import com.vtitan.uroflowmetry.fragment.AboutUsFragment
import com.vtitan.uroflowmetry.fragment.BluetoothDialogFragment
import com.vtitan.uroflowmetry.fragment.HospitalDetailFragment


class TabContainerFragment : Fragment() {

    private lateinit var binding: FragmentTabContainerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTabContainerBinding.inflate(layoutInflater)
        val bundle = arguments
        val tabSelection=bundle?.getInt(resources.getString(R.string.key_tab_frag_change))

        /*  Checking whether the fragment selection is device setup,calibration or hospital details
         1 .Device setup.(if the use not done the device setup and start the test redirect to device setup tab.)
         2.Calibration.(If the device is not calibirated redirect the user to calibration tab.)
         3.Hospital Information.(If the user not fill the hospital information trying to download the report redirect to the hospital information tab.)
          by default it should be device setup tab
         */
        if(tabSelection == 1){
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
           // binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                BluetoothDialogFragment()
            )?.commit()
        }else if(tabSelection == 2){
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
           // binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                DetailSetupCalibrationFragment()
            )?.commit()
        }/*else if(tabSelection == 3){
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                HospitalDetailFragment()
            )?.commit()
        }*/else{
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
           // binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                BluetoothDialogFragment()
            )?.commit()
        }
        /**************    Tab selection listener *******************/
        binding.btnDeviceSetup.setOnClickListener {
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            //binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                BluetoothDialogFragment()
            )?.commit()
        }

        binding.btnCalibration.setOnClickListener {
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
           // binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                DetailSetupCalibrationFragment()
            )?.commit()
        }

        /*binding.btnHospitalDetails.setOnClickListener {
            binding.btnDeviceSetup.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            binding.btnCalibration.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_unselected))
            binding.btnHospitalDetails.setBackgroundTintList(resources.getColorStateList(R.color.button_tin_bg_selected))
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.device_setup_container,
                HospitalDetailFragment()
            )?.commit()
        }*/

        // Inflate the layout for this fragment
        return binding.root
    }


}