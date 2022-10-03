package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.circularreveal.CircularRevealHelper
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.repository.PatientRepository
import com.vtitan.uroflowmetry.room.LocalDataBase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.Delegates

class PatientViewModel(private val application: Application ): ViewModel() {
    private val mRepository: PatientRepository
    private var patientDetails = MutableLiveData<PatientDetails>()
    init {
        val dataDao = LocalDataBase.getDatabase(application).patDao()
        mRepository = PatientRepository(dataDao)
    }

    fun insert(patientdetails: PatientDetails) = GlobalScope.launch(Dispatchers.IO) {
        mRepository.insert(patientdetails)
    }

    fun getPatientDetails(patId : String?) : PatientDetails{
       // var patientDetails by Delegates.notNull<patientDetails>()
         var patientDetails: PatientDetails? = null
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                patientDetails = mRepository.getPatientDetails(patId)
            }.await()
        }
        return patientDetails!!
    }

    fun getPatientIds() :List<String?> {
        var patidList :List<String?> = listOf<String>()
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                patidList = mRepository.getPatientIdList()
            }.await()
        }
        return patidList
    }
    fun updatePatientdetails(patId:String,patName:String,patWeight:Float,patAge:Int,patGender:String,patRef:String,patExm:String,patPosition :Int,patWaitTime:Int,mode:Int,updatedTime:Long) = GlobalScope.launch(Dispatchers.IO) {
        mRepository.updatepatientDetails(patId, patName, patWeight, patAge, patGender, patRef, patExm,patPosition,patWaitTime,mode,updatedTime)
    }


}