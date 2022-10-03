package com.vtitan.uroflowmetry.repository

import androidx.annotation.WorkerThread
import androidx.room.Update
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.room.PatientDao

class PatientRepository (private val patientDao: PatientDao) {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(patientDetails: PatientDetails) {
        patientDao.insertAll(patientDetails)
    }

   fun getPatientDetails(patId :String?) : PatientDetails {
    return patientDao.getPatientDetails(patId)
   }

  fun getPatientIdList() : List<String?> =patientDao.getPatientIds()

  fun updatepatientDetails(patId:String,patName:String,patWeight:Float,patAge:Int,patGender:String,patRef:String,patExm:String,patPosition :Int,patWaitTime:Int,mode:Int,updatedTime:Long) {
      patientDao.updatepatientDetails(patId,patName,patWeight,patAge, patGender, patRef, patExm,patPosition,patWaitTime,mode,updatedTime)
  }


}