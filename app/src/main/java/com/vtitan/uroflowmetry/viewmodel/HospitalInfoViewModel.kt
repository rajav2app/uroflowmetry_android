package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.vtitan.uroflowmetry.repository.HospitalInfoRepository
import com.vtitan.uroflowmetry.room.LocalDataBase

class HospitalInfoViewModel(private val application: Application): ViewModel() {
    private val mRepository: HospitalInfoRepository

    init {
        val dataDao = LocalDataBase.getDatabase(application).hospitalDao()
        mRepository = HospitalInfoRepository(dataDao)
    }
}