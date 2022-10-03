package com.vtitan.uroflowmetry.repository

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.vtitan.uroflowmetry.model.BleDeviceListModel
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.room.DeviceListDao
import com.vtitan.uroflowmetry.room.LocalDataBase
import com.vtitan.uroflowmetry.room.PatientDao
import com.vtitan.uroflowmetry.room.UroInfoDao

class DeviceInfoRepository(private val mcontext : Context) {

    val deviceDao : DeviceListDao
    init {
        val db : LocalDataBase = LocalDataBase.getDatabase(mcontext)
        deviceDao=db.deviceDao()
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertDeviceData(devicedata: BleDeviceListModel) {
        deviceDao.insertAllDC(devicedata)
    }

    fun getAllDevice(): List<BleDeviceListModel>? {
        return deviceDao.getAllDCs();
    }
    fun deviceSQLOperation(insertlist: List<BleDeviceListModel?>, updateList: List<BleDeviceListModel?>, deleteList: List<String?>){
        deviceDao.deviceSQLOperation(insertlist,updateList,deleteList)
    }

    fun getAllDeviceList() : LiveData<List<BleDeviceListModel>>? {
        return deviceDao.getAllDevices()
    }

    fun updateConnectionStatus(status:Int,id:String?){
        deviceDao.updateConnectionStatus(status,id)
    }

}