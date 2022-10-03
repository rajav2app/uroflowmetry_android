package com.vtitan.uroflowmetry.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "patient",indices = arrayOf(Index(value = arrayOf("pat_id"), unique = true)))
data class PatientDetails(
    @PrimaryKey(autoGenerate = true) val pid: Int,
    @ColumnInfo(name = "pat_id") val patId: String?,
    @ColumnInfo(name = "pat_name") val patName: String?,
    @ColumnInfo(name = "pat_weight") val patWeight: Float?,
    @ColumnInfo(name = "pat_age") val patAge: Int?,
    @ColumnInfo(name = "pat_gender") val patGender: String?,
    @ColumnInfo(name = "pat_referred_by") val patReffered: String?,
    @ColumnInfo(name = "pat_examined_by") val patExamined: String?,
    @ColumnInfo(name = "position") val position: Int?,
    @ColumnInfo(name = "wait_time") val wait_time: Int?,
    @ColumnInfo(name = "test_mode") val test_mode: Int?,
    @ColumnInfo(name = "last_updated_time") val lastupdatedTime: Long?

)
