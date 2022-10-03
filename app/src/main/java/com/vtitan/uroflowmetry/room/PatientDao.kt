package com.vtitan.uroflowmetry.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vtitan.uroflowmetry.model.PatientDetails
import java.util.concurrent.Flow

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg patientDetails: PatientDetails)

    @Query("SELECT pat_id FROM patient")
    fun getPatientIds():List<String?>

    @Query("SELECT * FROM patient WHERE pat_id= :patId")
    fun getPatientDetails(patId: String?) : PatientDetails

    @Query("UPDATE patient SET pat_name =:patName,pat_weight =:patWeight,pat_age =:patAge," +
            "pat_gender =:patGender,pat_referred_by =:patRef,pat_examined_by =:patExm,position =:patPosition,wait_time =:patWaitTime,test_mode =:mode,last_updated_time =:updatedTime WHERE pat_id =:patId")
    fun updatepatientDetails(patId:String,patName:String,patWeight:Float,patAge:Int,patGender:String,patRef:String,patExm:String,patPosition :Int,patWaitTime:Int,mode:Int,updatedTime:Long)

}