package com.example.uroflowmetryapp.util

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.uroflowmetryapp.fragment.DetailSetupCalibrationFragment
import com.vtitan.uroflowmetry.fragment.BluetoothDialogFragment
import com.vtitan.uroflowmetry.fragment.HospitalDetailFragment

internal class DeviceConfigurationTabAdapter(
    var context: Context,
    var fm:FragmentManager,
    var totalTabs:Int
) :FragmentPagerAdapter(fm){
    override fun getCount(): Int {
        return totalTabs
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                BluetoothDialogFragment()
            }
            1 -> {
                DetailSetupCalibrationFragment()
            }
            2 ->{
                HospitalDetailFragment()
            }

            else -> getItem(position)
        }
    }

}