package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class DeviceInfoViewModelFactory (private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceInfoViewModel::class.java)) {
            return DeviceInfoViewModel(application) as T
        }
        throw IllegalArgumentException("Uknown ViewModel Class")
    }
}