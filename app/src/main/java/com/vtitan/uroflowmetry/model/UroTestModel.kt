package com.vtitan.uroflowmetry.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "test_info")
data class UroTestModel(
    @PrimaryKey(autoGenerate = true) val tid: Long,
    @ColumnInfo(name = "test_id") val test_id: Long,
    @ColumnInfo(name = "pat_id") val pat_id: String?,
    @ColumnInfo(name = "position") val position: Int?,
    @ColumnInfo(name = "wait_time") val wait_time: Int?,
    @ColumnInfo(name = "test_mode") val test_mode: Int?,
    @ColumnInfo(name = "start_time") val startTime: Long?,
    @ColumnInfo(name = "stop_time") val stopTime: Long?,
): Serializable
