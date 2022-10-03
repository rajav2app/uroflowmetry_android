package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.repository.PatientRepository
import java.lang.IllegalArgumentException

class PatientViewModelFactory(private val application: Application):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientViewModel::class.java)){
            return PatientViewModel(application) as T
        }
        throw IllegalArgumentException("Uknown ViewModel Class")
    }
}