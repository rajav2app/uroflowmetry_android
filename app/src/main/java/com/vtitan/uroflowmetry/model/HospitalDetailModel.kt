package com.vtitan.uroflowmetry.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospital_details")
data class HospitalDetailModel(
    @PrimaryKey(autoGenerate = true) val hs_id: Long,
    @ColumnInfo(name = "hospital_name") val hospital_name: String?,
    @ColumnInfo(name = "hospital_address") val hospital_address: String?,
    @ColumnInfo(name = "hospital_logo") val hospital_logo: String?,
    @ColumnInfo(name = "contact_number") val contact_number: String?,
    @ColumnInfo(name = "web_site") val web_site: String?,
    @ColumnInfo(name = "default_hospital") val default_hospital: Int?
)
