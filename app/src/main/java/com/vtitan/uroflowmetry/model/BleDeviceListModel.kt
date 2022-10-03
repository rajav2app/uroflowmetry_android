package com.vtitan.uroflowmetry.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "device",indices = arrayOf(Index(value = arrayOf("dev_id"), unique = true)))
data class BleDeviceListModel(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "dev_id") val devId: String,
    @ColumnInfo(name = "dev_name") val devName: String?,
    @ColumnInfo(name = "connection_status") val connection_status: Int
)
