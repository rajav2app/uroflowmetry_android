package com.vtitan.uroflowmetry.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.vtitan.uroflowmetry.model.HospitalDetailModel
import com.vtitan.uroflowmetry.model.UroInfoModel

@Dao
interface HospitalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHospitalInfo(vararg hospitalInfo: HospitalDetailModel)

}