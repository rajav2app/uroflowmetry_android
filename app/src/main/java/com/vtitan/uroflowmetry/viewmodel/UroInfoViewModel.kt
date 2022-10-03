package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.repository.UroInfoRepository
import com.vtitan.uroflowmetry.room.LocalDataBase
import kotlinx.coroutines.*

class UroInfoViewModel (private val application: Application): ViewModel() {
    private val mRepository: UroInfoRepository
    init {
        val dataDao = LocalDataBase.getDatabase(application).uroInfoDao()
        mRepository = UroInfoRepository(application.applicationContext)
    }

    fun insert(uroInfo: UroInfoModel) = GlobalScope.launch(Dispatchers.IO) {
        mRepository.insertUroInfo(uroInfo)
    }

    fun getCurrentTestDetails(patId: String?,testId: Long) : LiveData<List<UroInfoModel>>{
        var currentdata :  LiveData<List<UroInfoModel>>?=null
        runBlocking {
            viewModelScope.async { Dispatchers.IO }
            currentdata=mRepository.getCurrentTestDetails(patId,testId)
        }
        return currentdata!!
    }

    fun getTestDetails(patId: String?,testId: Long) : List<UroInfoModel>{
        var currenturodata :  List<UroInfoModel>?=null
        runBlocking {
            viewModelScope.async { Dispatchers.IO }
            currenturodata=mRepository.getTestDetails(patId,testId)
        }
        return currenturodata!!
    }

    fun getMaxFlowRate(patId : String?,testId :Long) : LiveData<Double> {
        var maxFlowRate : LiveData<Double>? =null
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                maxFlowRate = mRepository.getMaxFlowRate(patId,testId)
            }.await()
        }
        return maxFlowRate!!
    }

    fun getAvgFlowRate(patId: String?,testId: Long) : LiveData<Double>{
        var avgFlowRate : LiveData<Double>?=null
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                avgFlowRate=mRepository.getAvgFlowRate(patId,testId)
            }.await()
        }
        return avgFlowRate!!
    }

    fun getVoidVolume(patId: String?,testId: Long) : LiveData<Double>{
        var voidVolume : LiveData<Double>?=null
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                voidVolume=mRepository.getVoidVolume(patId,testId)
            }.await()
        }
        return voidVolume!!
    }

    fun getVoidTiming(patId: String?,testId: Long) : LiveData<Int>{
        var voidTiming : LiveData<Int>?=null
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                voidTiming=mRepository.getVoidingTime(patId,testId)
            }.await()
        }
        return voidTiming!!
    }

    fun getPeakTime(patId: String?,testId: Long) : LiveData<Int>{
        var peakTime : LiveData<Int>?=null
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                peakTime=mRepository.getPeakTime(patId,testId)
            }.await()
        }
        return peakTime!!
    }

    fun deleteTestInfo(testId: Long){
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
               mRepository.deleteTestInfo(testId)
            }.await()
        }
    }

}