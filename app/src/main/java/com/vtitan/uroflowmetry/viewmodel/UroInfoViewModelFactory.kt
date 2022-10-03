package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtitan.uroflowmetry.model.UroInfoModel
import java.lang.IllegalArgumentException

class UroInfoViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UroInfoViewModel::class.java)){
            return UroInfoViewModel(application) as T
        }
        throw IllegalArgumentException("Uknown VieModel Class")
    }
}