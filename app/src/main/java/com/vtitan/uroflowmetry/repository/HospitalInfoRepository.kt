package com.vtitan.uroflowmetry.repository

import androidx.annotation.WorkerThread
import com.vtitan.uroflowmetry.model.HospitalDetailModel
import com.vtitan.uroflowmetry.room.HospitalDao

class HospitalInfoRepository(private val hospitalDao: HospitalDao) {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertHospitalInfo(hospitalDetails: HospitalDetailModel) {
        hospitalDao.insertHospitalInfo(hospitalDetails)
    }
}