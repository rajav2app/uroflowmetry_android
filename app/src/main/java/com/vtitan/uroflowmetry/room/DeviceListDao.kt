package com.vtitan.uroflowmetry.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vtitan.uroflowmetry.model.BleDeviceListModel

@Dao
interface DeviceListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllDC(vararg bleDeviceList: BleDeviceListModel)

    @Query("SELECT * FROM device")
    fun getAllDCs(): List<BleDeviceListModel>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(dropCounters: List<BleDeviceListModel?>)

    @Query("delete from device where dev_id in (:idList)")
    fun deleteDevice(idList: List<String?>)

    @Transaction
    fun deviceSQLOperation(insertlist: List<BleDeviceListModel?>, updateList: List<BleDeviceListModel?>, deleteList: List<String?>) {
        deleteDevice(deleteList)
        insertAll(insertlist)
        updateDevice(updateList)
    }

    @Transaction
    fun updateDevice(updateList: List<BleDeviceListModel?>) {
        updateList.forEach {
            updateDCInfo(it?.devId,it?.connection_status)
        }
    }

    @Query("UPDATE device SET dev_id =:id connection_status =:status WHERE dev_id =:id")
    fun updateDCInfo(id: String?,status: Int?)

    @Query("SELECT * FROM device")
    fun getAllDevices(): LiveData<List<BleDeviceListModel>>?

    @Query("UPDATE device SET connection_status =:status WHERE dev_id =:id")
    fun updateConnectionStatus(status:Int,id: String?)



}
