package com.vtitan.uroflowmetry.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo

@Dao
interface UroTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUroTestInfo(vararg uroTestInfoModel: UroTestModel)

    @Query("SELECT MAX(test_id) FROM test_info WHERE pat_id =:patId")
    fun getTestId(patId: String?) : Long

    @Query("SELECT test_id FROM test_info WHERE pat_id =:patId AND start_time >=:patTime ORDER BY start_time DESC")
    fun getTestIdTime(patId: String?,patTime:Long) : List<Long>

    @Transaction
    @Query("SELECT * FROM test_info WHERE test_id = :testId ")
    fun getUroTestWithUroInfo(testId:Long): UroTestWithUroInfo

    @Query("UPDATE test_info SET stop_time = :stopTime WHERE test_id = :testId")
    fun updateTestStatus(stopTime:Long,testId: Long)

    @Query("DELETE FROM test_info WHERE test_id = :testId")
    fun deleteTestDetails(testId: Long)
}