package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel
import java.lang.IllegalArgumentException

class UroTestInfoViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UroTestInfoViewModel::class.java)){
            return UroTestInfoViewModel(application) as T
        }
        throw IllegalArgumentException("Uknown VieModel Class")
    }
}