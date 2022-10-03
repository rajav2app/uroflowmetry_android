package com.vtitan.uroflowmetry.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vtitan.uroflowmetry.model.*

@Database(entities = [PatientDetails::class,UroInfoModel::class,UroTestModel::class,HospitalDetailModel::class,BleDeviceListModel::class], version = 2, exportSchema = false)
abstract class LocalDataBase: RoomDatabase() {
    abstract fun patDao():PatientDao
    abstract fun uroInfoDao():UroInfoDao
    abstract fun uroTestDao():UroTestDao
    abstract fun deviceDao():DeviceListDao
    abstract fun hospitalDao():HospitalDao
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: LocalDataBase? = null

        fun getDatabase(context: Context): LocalDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDataBase::class.java,
                    "uroflow"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}