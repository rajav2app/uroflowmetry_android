package com.vtitan.uroflowmetry.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager {
    // Shared Preferences
    private var pref: SharedPreferences

    // Editor for Shared preferences
    private var editor: SharedPreferences.Editor

    // Context
    private var _context: Context

    // Shared pref mode
    var PRIVATE_MODE = 0

    // Sharedpref file name
    private val PREF_NAME = "Pref"
    val KEY_THEAM = "light_mode"
    val KEY_MAC_ID = "mac_id"
    val KEY_DEVICE_NAME = "mac_id"
    val KEY_CONNECTION_STATUS="Connection";

    private var IS_SETTING_FILLED = "isSettingsFilled"
    private var KEY_HOSPITAL_NAME = "hospitalName"
    private var KEY_ADDRESS = "hospitalAddress"
    private var KEY_LOGO= "hospitalImage64"

    private var KEY_NO_LOAD="NoLoad"
    private var KEY_5G_LOAD="Load_5g"
    private var KEY_10G_LOAD="Load_10g"
    private var KEY_20G_LOAD="Load_20g"
    private var KEY_50G_LOAD="Load_50g"
    private var KEY_100G_LOAD="Load_100g"
    private var KEY_200G_LOAD="Load_200g"
    private var KEY_400G_LOAD="Load_400g"
    private var KEY_500G_LOAD="Load_500g"
    private var KEY_1000G_LOAD="Load_1000g"
    private var KEY_1500G_LOAD="Load_1500g"
    private var KEY_CALIBRATION_VALUE="CalibrationValue"
    private var KEY_CALIBRATION_DATE="CalibrationDate"
    constructor(context: Context) {
        _context = context
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }


    fun savelightmode(theamID: Boolean) {
        editor.putBoolean(KEY_THEAM, theamID)
        taskDone()
    }
    fun isLightModeOn(): Boolean {
        return pref.getBoolean(KEY_THEAM, false)
    }

    fun savemacId(mackId: String?) {
        editor.putString(KEY_MAC_ID, mackId)
        taskDone()
    }
    fun getMacId(): String?{
        return pref.getString(KEY_MAC_ID,"")
    }

    fun saveDeviceName(deviceName: String?) {
        editor.putString(KEY_DEVICE_NAME, deviceName)
        taskDone()
    }
    fun getDeviceName(): String?{
        return pref.getString(KEY_DEVICE_NAME,"")
    }
    fun saveConnectionStatus(connectionstatus : Int){
        editor.putInt(KEY_CONNECTION_STATUS, connectionstatus)
        taskDone()
    }

    fun connectionStatus(): Int{
        return pref.getInt(KEY_CONNECTION_STATUS, 0)
    }
    fun clearallData() {
        // Clearing all data from Shared Preferences
        editor.clear()
        editor.commit()
    }


    fun getDefaultSetting():Boolean{
        return pref.getBoolean(IS_SETTING_FILLED,false)
    }

    fun setDefaultSetting(temp:Boolean){
        editor.putBoolean(IS_SETTING_FILLED,temp)
        taskDone()
    }


    fun setHospitalName(temp:String){
        editor.putString(KEY_HOSPITAL_NAME,temp)
        taskDone()
    }

    fun getHospitalName():String{
        return pref.getString(KEY_HOSPITAL_NAME,"null")!!
    }

    fun setAddress(temp:String){
        editor.putString(KEY_ADDRESS,temp)
        taskDone()
    }

    fun getAddress():String{
        return pref.getString(KEY_ADDRESS,"null")!!
    }

    fun setLogo(temp:String){
        editor.putString(KEY_LOGO,temp)
        taskDone()
    }

    fun getLogo():String{
        return pref.getString(KEY_LOGO,"null")!!
    }

    fun saveNoLoad(noLoad: Float) {
        editor.putFloat(KEY_NO_LOAD, noLoad)
        taskDone()
    }
    fun getNoloadValue(): Float?{
        return pref.getFloat(KEY_NO_LOAD,0F)
    }

    fun save5gLoad(load5g: Float) {
        editor.putFloat(KEY_5G_LOAD, load5g)
        taskDone()
    }
    fun get5gloadValue(): Float{
        return pref.getFloat(KEY_5G_LOAD,0F)
    }
    fun save10gLoad(load10g: Float) {
        editor.putFloat(KEY_10G_LOAD, load10g)
        taskDone()
    }
    fun get10gloadValue(): Float{
        return pref.getFloat(KEY_10G_LOAD,0F)
    }
    fun save20gLoad(load20g: Float) {
        editor.putFloat(KEY_20G_LOAD, load20g)
        taskDone()
    }
    fun get20gloadValue(): Float{
        return pref.getFloat(KEY_20G_LOAD,0F)
    }

    fun save50gLoad(load50g: Float) {
        editor.putFloat(KEY_50G_LOAD, load50g)
        taskDone()
    }
    fun get50gloadValue(): Float{
        return pref.getFloat(KEY_50G_LOAD,0F)
    }

    fun save100gLoad(load100g: Float) {
        editor.putFloat(KEY_100G_LOAD, load100g)
        taskDone()
    }
    fun get100gloadValue(): Float{
        return pref.getFloat(KEY_100G_LOAD,0F)
    }

    fun save200gLoad(load200g: Float) {
        editor.putFloat(KEY_200G_LOAD, load200g)
        taskDone()
    }
    fun get200gloadValue(): Float{
        return pref.getFloat(KEY_200G_LOAD,0F)
    }

    fun save400gLoad(load400g: Float) {
        editor.putFloat(KEY_400G_LOAD, load400g)
        taskDone()
    }
    fun get400gloadValue(): Float{
        return pref.getFloat(KEY_400G_LOAD,0F)
    }

    fun save500gLoad(load500g: Float) {
        editor.putFloat(KEY_500G_LOAD, load500g)
        taskDone()
    }
    fun get500gloadValue(): Float{
        return pref.getFloat(KEY_500G_LOAD,0F)
    }
    fun save1000gLoad(load1000g: Float) {
        editor.putFloat(KEY_1000G_LOAD, load1000g)
        taskDone()
    }
    fun get1000gloadValue(): Float{
        return pref.getFloat(KEY_1000G_LOAD,0F)
    }
    fun save1500gLoad(load1500g: Float) {
        editor.putFloat(KEY_1500G_LOAD, load1500g)
        taskDone()
    }
    fun get1500gloadValue(): Float{
        return pref.getFloat(KEY_1500G_LOAD,0F)
    }
    fun saveCalibrationValue(calibrationValue: Float) {
        editor.putFloat(KEY_CALIBRATION_VALUE, calibrationValue)
        taskDone()
    }
    fun getCalibrationValue(): Float{
        return pref.getFloat(KEY_CALIBRATION_VALUE,0F)
    }

    fun saveCalibrationDate(calibrationDate: Long) {
        editor.putLong(KEY_CALIBRATION_DATE, calibrationDate)
        taskDone()
    }
    fun getCalibrationDate(): Long{
        return pref.getLong(KEY_CALIBRATION_DATE,0L)
    }
    fun clearCalibirationDetails(){
        editor.remove(KEY_5G_LOAD)
        editor.remove(KEY_10G_LOAD);
        editor.remove(KEY_20G_LOAD);
        editor.remove(KEY_50G_LOAD);
        editor.remove(KEY_100G_LOAD);
        editor.remove(KEY_200G_LOAD);
        editor.remove(KEY_400G_LOAD);
        editor.remove(KEY_500G_LOAD);
        editor.remove(KEY_1000G_LOAD);
        editor.remove(KEY_1500G_LOAD);
        editor.apply()
    }

    private fun taskDone(){
        editor.apply()
        editor.commit()
    }
}