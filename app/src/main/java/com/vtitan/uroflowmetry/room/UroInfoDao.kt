package com.vtitan.uroflowmetry.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel

@Dao
interface UroInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUroInfo(vararg uroInfoModel: UroInfoModel)

    @Query("SELECT * FROM uro_info WHERE pat_id =:patId AND test_id =:testId")
    fun getCurrentTestDetails(patId: String?,testId :Long) : LiveData<List<UroInfoModel>>

    @Query("SELECT MAX(flow_rate) FROM uro_info WHERE pat_id =:patId AND test_id =:testId")
    fun getMaxFlowRate(patId: String?,testId :Long) : LiveData<Double>

    @Query("SELECT AVG(flow_rate) FROM uro_info WHERE pat_id =:patId AND test_id =:testId")
    fun getAvgFlowRate(patId: String?,testId :Long) : LiveData<Double>

    @Query("SELECT MAX(volume_info) FROM uro_info WHERE pat_id =:patId AND test_id =:testId")
    fun getVoidVolume(patId: String?,testId :Long) : LiveData<Double>

    @Query("SELECT sec FROM uro_info WHERE flow_rate = (SELECT Max(flow_rate) FROM uro_info WHERE pat_id =:patId AND test_id =:testId)")
    fun getPeakTime(patId: String?,testId :Long) : LiveData<Int>

    @Query("SELECT sec FROM uro_info WHERE volume_info >0 AND pat_id =:patId AND test_id =:testId")
    fun getVoidTime(patId: String?,testId :Long) : LiveData<Int>

    @Query("SELECT * FROM uro_info WHERE pat_id =:patId AND test_id =:testId")
    fun getTestDetails(patId: String?,testId :Long) : List<UroInfoModel>

    @Query("DELETE FROM uro_info WHERE test_id =:testId")
    fun deleteTestInfo(testId: Long)

}