package com.vtitan.uroflowmetry.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtitan.uroflowmetry.model.BleDeviceListModel
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.repository.DeviceInfoRepository
import com.vtitan.uroflowmetry.repository.UroInfoRepository
import com.vtitan.uroflowmetry.room.LocalDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class DeviceInfoViewModel(private val application: Application): ViewModel() {
    private val mRepository: DeviceInfoRepository

    init {
        val dataDao = LocalDataBase.getDatabase(application).deviceDao()
        mRepository = DeviceInfoRepository(application.applicationContext)
    }


    fun getAllDevice() : List<BleDeviceListModel>{
        lateinit var currentdata :  List<BleDeviceListModel>
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                currentdata= mRepository.getAllDevice()!!
            }.await()
        }
        return currentdata
    }

    fun deviceSQLOperation(insertlist: List<BleDeviceListModel?>, updateList: List<BleDeviceListModel?>, deleteList: List<String?>){
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                 mRepository.deviceSQLOperation(insertlist,updateList,deleteList)
            }.await()
        }
    }

    fun getAllDeviceList() : LiveData<List<BleDeviceListModel>>{
        lateinit var currentdata : LiveData<List<BleDeviceListModel>>
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                currentdata= mRepository.getAllDeviceList()!!
            }.await()
        }
        return currentdata
    }
    fun updateConnectionStatus(status:Int,id:String?){
        runBlocking {
            viewModelScope.async(Dispatchers.IO) {
                mRepository.updateConnectionStatus(status, id)
            }.await()
        }
    }

}