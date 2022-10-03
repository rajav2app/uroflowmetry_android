package com.vtitan.uroflowmetry.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "uro_info")
data class UroInfoModel(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "sec") val sec: Int,
    @ColumnInfo(name = "sequence_no") val sequence_no: Int,
    @ColumnInfo(name = "volume_info") val volume_info: Double?,
    @ColumnInfo(name = "flow_rate") val flow_rate: Double?,
    @ColumnInfo(name = "adc_value") val adc_value: Int?,
    @ColumnInfo(name = "cumulative_volume") val cumulative_volume: Double?,
    @ColumnInfo(name = "pat_id") val pat_id: String?,
    @ColumnInfo(name = "test_id") val test_id: Long?): Serializable

