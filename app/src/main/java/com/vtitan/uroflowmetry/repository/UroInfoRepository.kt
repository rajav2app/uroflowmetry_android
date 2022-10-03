package com.vtitan.uroflowmetry.repository

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo
import com.vtitan.uroflowmetry.room.LocalDataBase
import com.vtitan.uroflowmetry.room.UroInfoDao
import com.vtitan.uroflowmetry.room.UroTestDao

class UroInfoRepository(private val mcontext :Context) {

    val uroInfoDao : UroInfoDao
    val uroTestInfoDao : UroTestDao
    init {
        val db :LocalDataBase= LocalDataBase.getDatabase(mcontext)
        uroInfoDao=db.uroInfoDao()
        uroTestInfoDao=db.uroTestDao()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertUroInfo(uroInfo: UroInfoModel) {
        uroInfoDao.insertUroInfo(uroInfo)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertUroTestInfo(uroTestInfo: UroTestModel) {
        uroTestInfoDao.insertUroTestInfo(uroTestInfo)
    }

    fun getTestId(patId :String?) : Long {
        return uroTestInfoDao.getTestId(patId)
    }

    fun getTestIdTime(patId :String?,patTime:Long) : List<Long> {
        return uroTestInfoDao.getTestIdTime(patId,patTime)
    }

    fun getCurrentTestDetails(patId: String?,testId: Long) : LiveData<List<UroInfoModel>>{
        return uroInfoDao.getCurrentTestDetails(patId,testId)
    }

    fun getTestDetails(patId: String?,testId: Long) : List<UroInfoModel>{
        return uroInfoDao.getTestDetails(patId,testId)
    }

    fun getMaxFlowRate(patId: String?,testId:Long) : LiveData<Double>{
        return uroInfoDao.getMaxFlowRate(patId,testId)
    }
    fun getAvgFlowRate(patId: String?,testId:Long) : LiveData<Double>{
        return uroInfoDao.getAvgFlowRate(patId,testId)
    }
    fun getVoidVolume(patId: String?,testId:Long) : LiveData<Double>{
        return uroInfoDao.getVoidVolume(patId,testId)
    }

    fun getVoidingTime(patId: String?,testId:Long) : LiveData<Int>{
     return uroInfoDao.getVoidTime(patId,testId)
    }

    fun getPeakTime(patId: String?,testId:Long) : LiveData<Int>{
        return uroInfoDao.getPeakTime(patId,testId)
    }

    suspend fun getUroTestWithUroInfo(testId:Long): UroTestWithUroInfo {
        return uroTestInfoDao.getUroTestWithUroInfo(testId)
    }

    fun updateTestStatus(stopTime:Long,testId: Long){
        uroTestInfoDao.updateTestStatus(stopTime,testId)
    }
    fun deleteTestDetails(testId: Long){
        uroTestInfoDao.deleteTestDetails(testId)
    }
    fun deleteTestInfo(testId: Long){
        uroInfoDao.deleteTestInfo(testId)
    }

}