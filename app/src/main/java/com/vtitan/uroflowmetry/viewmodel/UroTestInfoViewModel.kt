package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo
import com.vtitan.uroflowmetry.repository.UroInfoRepository
import com.vtitan.uroflowmetry.room.LocalDataBase
import kotlinx.coroutines.*

class UroTestInfoViewModel(private val application: Application): ViewModel() {
    private val mRepository: UroInfoRepository
    init {
        val dataDao = LocalDataBase.getDatabase(application).uroTestDao()
        mRepository = UroInfoRepository(application.applicationContext)
    }
    fun insert(uroTestInfo: UroTestModel) = GlobalScope.launch(Dispatchers.IO) {
        mRepository.insertUroTestInfo(uroTestInfo)
    }

    fun getTestId(patId : String?) : Long?{
        var testId : Long? = null
        runBlocking {
            GlobalScope.async (Dispatchers.IO){
                testId = mRepository.getTestId(patId)
            }.await()
        }
        return testId
    }
    fun getTestIdTime(patId : String?,patTime:Long) :List< Long>?{
        var testId :List< Long>? = null
        runBlocking {
            GlobalScope.async (Dispatchers.IO){
                testId = mRepository.getTestIdTime(patId, patTime )
            }.await()
        }
        return testId
    }

    fun getUroTestWithUroInfo(testId:Long):UroTestWithUroInfo{
        var testIds : UroTestWithUroInfo?= null
        runBlocking {
            GlobalScope.async (Dispatchers.IO){
                testIds = mRepository.getUroTestWithUroInfo(testId)
            }.await()
        }
        return testIds!!
    }

    fun updateTestStatus(stopTime:Long,testId: Long){
        runBlocking {
            GlobalScope.async (Dispatchers.IO){
                 mRepository.updateTestStatus(stopTime,testId)
            }.await()
        }
    }
    fun deleteTestDetails(testId: Long){
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                mRepository.deleteTestDetails(testId)
            }.await()
        }
    }

}