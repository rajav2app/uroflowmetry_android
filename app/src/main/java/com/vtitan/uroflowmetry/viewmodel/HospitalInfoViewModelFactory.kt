package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class HospitalInfoViewModelFactory (private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HospitalInfoViewModel::class.java)) {
            return HospitalInfoViewModel(application) as T
        }
        throw IllegalArgumentException("Uknown ViewModel Class")
    }
}