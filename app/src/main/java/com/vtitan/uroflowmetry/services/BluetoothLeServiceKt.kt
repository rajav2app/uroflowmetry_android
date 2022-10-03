package com.vtitan.uroflowmetry.services

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.repository.UroInfoRepository
import com.vtitan.uroflowmetry.util.Constants
import com.vtitan.uroflowmetry.util.Constants.*
import com.vtitan.uroflowmetry.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileWriter
import java.lang.String.format
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class BluetoothLeServiceKt : Service() {
    private var counter = ADC_COUNT_120
    private var previousAdcValue = 0
    private var previousCounter = 0
    private var previousCout = 0
    private var volumeData = 0.0
    private lateinit var mDCRep: UroInfoRepository
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED
    private var isFirst = false
    private val isPrevious = true
    private var waitTime = 0
    private var testMode=0
    private var patId=""
    private var isRecord = false
    private var testId=0L
    private var density=1.0
    private var mLConversionFactor = 0.8
    private lateinit var fWriter: FileWriter
    private var loadValue=0
    private var noloadVal=0.0
    private var loadcount=0
    private var load5gcount=0
    private var load10gcount=0
    private var load20gcount=0
    private var load50gcount=0
    private var load100gcount=0
    private var load200gcount=0
    private var load400gcount=0
    private var load500gcount=0
    private var load1000gcount=0
    private var load1500gcount=0
    private lateinit var sessionManager : SessionManager
    private lateinit var calibrationReceiver: BroadcastReceiver
    private var calibrationstatus=0

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                checkConnectionStatusUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:${mBluetoothGatt?.discoverServices()}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                checkConnectionStatusUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                checkConnectionStatusUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getService(UUID.fromString(Constants.UUID)).getCharacteristic(UUID.fromString(Constants.UUID)));
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Log.d(TAG, "Heart rate format UINT16."+characteristic.getService());
                getValues(characteristic)
                Log.d(TAG, "Uro Volume_GATT." + characteristic.getValue())
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d(TAG, "Uro Volume." + characteristic.getValue())
            getValues(characteristic)
        }
    }


    private fun checkConnectionStatusUpdate(action: String) {
        if (ACTION_GATT_CONNECTED == action) {
            deviceConnectionBroadCast(Constants.CONNECTED)
            Log.i("CONNECTION", "connected")
        } else if (ACTION_GATT_DISCONNECTED == action) {
            Log.i("CONNECTION", "disconnected")
            deviceConnectionBroadCast(Constants.NOTCONNECTED)
        } else if (ACTION_GATT_SERVICES_DISCOVERED == action) {
            // Show all the supported services and characteristics on the user interface.
            displayGattServices(supportedGattServices as List<BluetoothGattService>?)
        } else if (ACTION_DATA_AVAILABLE == action) {
            //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
        }
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            Log.i(TAG, "AAA Service :: " + gattService.getUuid().toString())
            val gattCharacteristics: List<BluetoothGattCharacteristic> =
                gattService.getCharacteristics()
            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                Log.i(TAG, "AAACharacterstics :: " + gattCharacteristic.getUuid().toString())
                if (gattCharacteristic.getUuid().toString() == Constants.CHARACTER_UUID) {
                    //readCharacteristic(gattCharacteristic);
                    val charaProp: Int = gattCharacteristic.getProperties()
                    if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        setCharacteristicNotification(
                            gattCharacteristic, true
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getValues(characteristic: BluetoothGattCharacteristic) {
        val data1: ByteArray = characteristic.getValue()
        Log.i("BYTEARRAY", byteArrayToHexvalue(data1))
        val adcbyte: ByteArray
        val aCounter: ByteArray
        val delimiter: ByteArray
        val battery_byte_array : ByteArray
        val calibration_byte_array : ByteArray
        if (data1 != null && data1.size > 0) {
            adcbyte = Arrays.copyOfRange(data1, 0, 5)
            println("****DATA_VALUE***" + byteArrayToHexvalue(adcbyte))
            aCounter = Arrays.copyOfRange(data1, 5, 7)
            println("****SEQUENCE_NO***" + byteArrayToHexvalue(aCounter))
           // delimiter = Arrays.copyOfRange(data1, 5, 6)
           // println("****DELIMETER***" + byteArrayToHexvalue(delimiter))
             battery_byte_array=Arrays.copyOfRange(data1,7,12)
            println("****BATTERY_HEX***" + byteArrayToHexvalue(battery_byte_array))
            calibration_byte_array=Arrays.copyOfRange(data1,12,20)
            println("****CALIBRATION_HEX***" + byteArrayToHexvalue(calibration_byte_array))
            val adcValue = String(adcbyte).toInt()
            val countervalue = String(aCounter).toInt()
            val batteryvalue: Int =String(battery_byte_array).toInt()
           // byteArrayToHexvalue(battery_byte_array).toInt(16)
            println("######BATTERY_VALUE##### " + batteryvalue)
           // val calibration: Int = byteArrayToHexvalue(calibration_byte_array).toInt(16)
            //println("######CALIBRATION_VALUE##### " + calibration)
            val calibrationWithDate: Long = byteArrayToHexvalue(calibration_byte_array).toLong(16)

            Log.i("CALIBIRATIONDATE",""+calibrationWithDate)
            Log.i("ADC_Value_new", "ADC : $adcValue COUNTER : $countervalue")
            /******************** To Check the Calibration status *******************/
            if(calibrationWithDate > 0) {
                deviceConnectionBroadCast(CALIBIRATED)
               /******************** To Check the battery status and show the alert *******************/
                if(batteryvalue<LOWBATTERYEND && batteryvalue > BATTERYALARMSTOP){
                deviceConnectionBroadCast(LOWBATTERY)
               }else if(batteryvalue < BATTERYALARMSTOP){
                   deviceConnectionBroadCast(BATTERYALARM)
               }else{
                   println("No Battery alarm" + batteryvalue)
               }
                /******************** To seperate the calibiration value and calibration date *******************/
                val calibrationwithdateList = calibrationWithDate.toString().toList()
                val date=calibrationwithdateList.subList(0,8).joinToString("").toInt()
                val calibrationval=calibrationwithdateList.subList(8,11).joinToString("").toInt()
                val calibrationdate=getCalibrationDate(date)

                val cal:Double =(calibrationval.toDouble()/10000.0)
                mLConversionFactor=cal
                Log.i("TAGGING","${calibrationdate} -> ${cal}")
                /******************** To start record the data *******************/
                if (isRecord) {
                   // println("ML_VALUE"+mLConversionFactor)
                    /* try {

                    String printStr= " "+adcValue+" "+countervalue+" "+cycleNo ;
                    fWriter.write(printStr + "\n");
                   // fWriter.write("ADC : " + adcValue + " " + "COUNTER : " + countervalue+ "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                    //appendLog("ADC : " + adcValue + " " + "COUNTER : " + countervalue);
                    if (isFirst) {
                        previousAdcValue = adcValue
                        isFirst = false
                        counter = 0
                        volumeData = 0.0
                    } else if (counter < getADC_COUNT(waitTime)) {
                        if (previousCounter != countervalue) {
                            counter++
                            var currentrate = 0.0
                            var currentVolume = 0.0
                            var time = 0
                            var adcValueDiff = 0.0
                            Log.i(
                                "ADC_Value",
                                "ADC : $adcValue COUNTER : $countervalue TIME COUNTER$counter"
                            )
                            //Log.i(TAG, "CurrentADC " + "ADC" + adcValue + " " + "Previous ADC" + previousAdcValue);
                            if (adcValue >= previousAdcValue) {

                               /* if(sessionManager.getCalibrationValue() > 0) {
                                    mLConversionFactor = sessionManager.getCalibrationValue().toDouble()
                                }else{
                                    mLConversionFactor =0.8
                                }*/
                                if(density > 0){
                                }else{
                                    density=1.0
                                }
                                //mLConversionFactor=density.toDouble()

                                println("CAL "+mLConversionFactor)
                                println("Den"+density)
                                adcValueDiff = (adcValue - previousAdcValue).toDouble()
                                currentVolume = ((adcValueDiff * mLConversionFactor)/density)
                                val rate = currentVolume - volumeData /*volumeData + currentVolume*/
                                time = counter - previousCout
                                currentrate = currentVolume / time
                                val uroinfo = UroInfoModel(
                                    0,
                                    counter,
                                    countervalue,
                                    currentVolume,
                                    rate,
                                    adcValue,
                                    rate,
                                    patId,
                                    testId)

                                previousCout = counter
                                if (counter % 5 == 0) {
                                    volumeData = currentVolume
                                }
                                GlobalScope.launch(Dispatchers.IO) {
                                    mDCRep.insertUroInfo(uroinfo)
                                    Log.i(TAG, "Database ValuesTime:$counter Volume:$currentVolume Rate:$currentrate Previous:$previousAdcValue")
                                }
                                teststatusBroadCast(true)
                                Log.i(TAG, "Correct ValuesTime:$counter Volume:$currentVolume Rate:$currentrate Previous:$previousAdcValue")
                            } else {
                                /* if the adc value is + or - 5 with base value consider as a
                                 device displaced
                                */
                                if (adcValue > (previousAdcValue + 5) || adcValue < (previousAdcValue - 5)) {
                                    deviceConnectionBroadCast(DEVICEDISPLACED)
                                }
                                Log.i(TAG, "Ignored Value : $adcValue")
                            }
                        } else {
                            Log.i("IGNORE COUNTER", "ADC : Time:$counter$adcValue COUNTER : $countervalue")
                        }
                    } else {
                        if (isRecord) {
                            teststatusBroadCast(false)
                        }
                        isRecord = false
                        // teststatusBroadCast(false)
                        Log.i("TEST_COMPLETED", "ADC : Time:$counter$adcValue COUNTER : $countervalue"
                        )
                    }
                    previousCounter = countervalue

                } else {
                    // teststatusBroadCast(false)
                    /*try {
                    fWriter!!.flush()
                    fWriter!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }*/
                }
           }
            else{

                if(loadValue == CALIBRATION_NO_LOAD){
                    val noLoad :MutableList<Int> = mutableListOf()
                    if(loadcount < 5){
                        noLoad.add(adcValue)
                        loadcount++
                    }
                    if(loadcount == 5){
                        noloadVal=getLoadValue(noLoad)
                        loadcount++
                        println("NoLoad"+noloadVal)
                    }
                }else if(loadValue == CALIBRATION_5G_LOAD){
                    val load5g :MutableList<Int> = mutableListOf()
                    if(load5gcount < 5){
                        load5g.add(adcValue)
                        load5gcount++
                    }
                    if(load5gcount == 5){
                        val load5gVal=(CALIBRATION_5G_LOAD/(getLoadValue(load5g) - noloadVal))
                        sessionManager.save5gLoad(load5gVal.toFloat())
                        load5gcount++
                        println("5gLoad"+load5gVal)
                    }
                }else if(loadValue == CALIBRATION_10G_LOAD){
                    val load10g :MutableList<Int> = mutableListOf()
                    if(load10gcount < 5){
                        load10g.add(adcValue)
                        load10gcount++
                    }
                    if(load10gcount == 5){
                        val load10gVal=(CALIBRATION_10G_LOAD/(getLoadValue(load10g) - noloadVal))
                        load10gcount++
                        sessionManager.save10gLoad(load10gVal.toFloat())
                        println("10gLoad"+load10gVal)
                    }
                }else if(loadValue == CALIBRATION_20G_LOAD){
                    val load20g :MutableList<Int> = mutableListOf()
                    if(load20gcount < 5){
                        load20g.add(adcValue)
                        load20gcount++

                    }
                    if(load20gcount == 5){
                        val load20gVal=(CALIBRATION_20G_LOAD/(getLoadValue(load20g) - noloadVal))
                        load20gcount++
                        sessionManager.save20gLoad(load20gVal.toFloat())
                        println("20gLoad"+load20gVal)
                    }
                }else if(loadValue == CALIBRATION_50G_LOAD){
                    val load50g :MutableList<Int> = mutableListOf()
                    if(load50gcount < 5){
                        load50g.add(adcValue)
                        load50gcount++
                    }
                    if(load50gcount == 5){
                        val load50gVal=(CALIBRATION_50G_LOAD/(getLoadValue(load50g) - noloadVal))
                        load50gcount++
                        sessionManager.save50gLoad(load50gVal.toFloat())
                        println("50gLoad"+load50gVal)
                    }
                }else if(loadValue == CALIBRATION_100G_LOAD){
                    val load100g :MutableList<Int> = mutableListOf()
                    if(load100gcount < 5){
                        load100g.add(adcValue)
                        load100gcount++
                    }
                    if(load100gcount == 5){
                        val load100gVal=(CALIBRATION_100G_LOAD/(getLoadValue(load100g) - noloadVal))
                        load100gcount++
                        sessionManager.save100gLoad(load100gVal.toFloat())
                        println("100gLoad"+load100gVal)
                    }
                }else if(loadValue == CALIBRATION_200G_LOAD){
                    val load200g :MutableList<Int> = mutableListOf()
                    if(load200gcount < 5){
                        load200g.add(adcValue)
                        load200gcount++
                    }
                    if(load200gcount == 5){
                        val load200gVal=(CALIBRATION_200G_LOAD/(getLoadValue(load200g) - noloadVal))
                        load200gcount++
                        sessionManager.save200gLoad(load200gVal.toFloat())
                        println("200gLoad"+load200gVal)
                    }
                }else if(loadValue == CALIBRATION_400G_LOAD){
                    val load400g :MutableList<Int> = mutableListOf()
                    if(load400gcount < 5){
                        load400g.add(adcValue)
                        load400gcount++
                    }
                    if(load400gcount == 5){
                        val load400gVal=(CALIBRATION_400G_LOAD/(getLoadValue(load400g) - noloadVal))
                        load400gcount++
                        sessionManager.save400gLoad(load400gVal.toFloat())
                        println("400gLoad"+load400gVal)
                    }
                } else if(loadValue == CALIBRATION_500G_LOAD){
                    val load500g :MutableList<Int> = mutableListOf()
                    if(load500gcount < 5){
                        load500g.add(adcValue)
                        load500gcount++
                    }
                    if(load500gcount == 5){
                        val load500gVal=(CALIBRATION_500G_LOAD/(getLoadValue(load500g) - noloadVal))
                        load500gcount++
                        sessionManager.save500gLoad(load500gVal.toFloat())
                        println("500gLoad"+load500gVal)
                    }
                }else if(loadValue == CALIBRATION_1000G_LOAD){

                    val load1000g :MutableList<Int> = mutableListOf()
                    if(load1000gcount < 5){
                        load1000g.add(adcValue)
                        load1000gcount++
                    }
                    if(load1000gcount == 5){
                        val load1000gVal=(CALIBRATION_1000G_LOAD/(getLoadValue(load1000g) - noloadVal))
                        load1000gcount++
                        sessionManager.save1000gLoad(load1000gVal.toFloat())
                        println("1000gLoad"+load1000gVal)
                    }
                }else if(loadValue == CALIBRATION_1500G_LOAD){
                    val load1500g :MutableList<Int> = mutableListOf()
                    if(load1500gcount < 5){
                        load1500g.add(adcValue)
                        load1500gcount++
                    }
                    if(load1500gcount == 5){
                        val load1500gVal=(CALIBRATION_1500G_LOAD/(getLoadValue(load1500g) - noloadVal))
                        load1500gcount++
                        sessionManager.save1500gLoad(load1500gVal.toFloat())
                        println("1500gLoad"+load1500gVal)
                    }
                }
                if(sessionManager.get5gloadValue() <= 0 || sessionManager.get10gloadValue() <= 0
                    ||sessionManager.get20gloadValue() <= 0 || sessionManager.get50gloadValue() <= 0
                    ||sessionManager.get100gloadValue() <= 0 || sessionManager.get200gloadValue() <= 0
                    ||sessionManager.get400gloadValue() <= 0 || sessionManager.get500gloadValue() <= 0
                    ||sessionManager.get1000gloadValue() <= 0 || sessionManager.get1500gloadValue() <= 0){
                    //sessionManager.clearCalibirationDetails()
                    println("Device not Calibirated")

                }else{
                    if(calibrationstatus == CALIBRATION_CONFIRM) {
                        for (characteristics in characteristic.service.characteristics) {
                            if (characteristics.uuid.toString().equals(CHAR_CALI_UUID)) {
                                val charaProp: Int = characteristics.getProperties()
                                if (charaProp or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                                    setCharacteristicNotification(characteristics, false)

                                    val df = DecimalFormat("#.####")
                                    df.roundingMode = RoundingMode.CEILING
                                    val cal_double = df.format(sessionManager.getCalibrationValue()).toDouble()

                                    val calibirationvalue = (cal_double * 10000).toInt()
                                    val calibrationDate=convertCalibirationDateFormate()
                                    val calibrationDateValue="${calibrationDate}"+"${calibirationvalue}"
                                    val calibrationByteArray = intToByteArray(calibrationDateValue.toLong())
                                    println("Test_CALI"+calibirationvalue.toInt()+" "+calibrationDateValue)

                                    //for (i in 0..3) command[i] = 0x00
                                    characteristics.writeType = 2
                                    characteristics.setValue(calibrationByteArray)
                                    mBluetoothGatt?.writeCharacteristic(characteristics);
                                    //setRecieverNotify(mGattService);
                                    setCharacteristicNotification(characteristics, true);
                                    sessionManager.clearCalibirationDetails()
                                }
                            }
                        }
                    }
                }
                deviceConnectionBroadCast(NOTCALIBIRATED)
                println("NCDevice")
            }
        } else {
            Log.i("DNF", "ADC : Time:$counter")
        }

    }

    fun intToByteArray(value: Long): ByteArray {
        val bytes: ByteArray = ByteBuffer.allocate(8).putLong(value).array()
        return bytes
    }
    fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.i("STATUS","------------- onCharacteristicWrite status: $status")

        // do somethings here.
    }

    override fun onCreate() {
        super.onCreate()
        val mContext = applicationContext
        mDCRep = UroInfoRepository(mContext)
        registercalibirationLoadReceiver()
        sessionManager= SessionManager(mContext)
        // registerReceiver(broadcastReceiver, new IntentFilter(START_TEST));
    }

    inner class LocalBinder : Binder() {
        val serviceKt: BluetoothLeServiceKt
            get() = this@BluetoothLeServiceKt
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "onBind: ")
        if (intent != null) {
            val mAddress: String = intent.getStringExtra("dev_mac")!!
            connect(mAddress)
        }
        // mMessenger = new Messenger(new IncomingHandler(this));
        //this.isFirst=intent.getBooleanExtra("IS_FIRST");
        //mMessenger.getBinder();
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    private val mBinder: IBinder = LocalBinder()

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }
        mBluetoothAdapter = mBluetoothManager!!.getAdapter()
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            return if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                true
            } else {
                false
            }
        }
        val device: BluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    @SuppressLint("MissingPermission")
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        // This is specific to Uro flow Measurement.
        if (characteristic.getUuid().toString() == Constants.CHARACTER_UUID) {
            val descriptor: BluetoothGattDescriptor = characteristic.getDescriptor(
                UUID.fromString(
                    Constants.CHAR_CONFI_UUID
                )
            )
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    val supportedGattServices: List<Any>?
        get() = mBluetoothGatt?.getServices()

    private fun byteArrayToHexvalue(a: ByteArray?): String {
        val sb = StringBuilder()
        for (b in a!!) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")
        if (intent != null) {
            isRecord = intent.getBooleanExtra(getString(R.string.key_is_record), false)
           if(isRecord) {
               isFirst = intent.getBooleanExtra(getString(R.string.key_is_first), false)
               testMode = intent.getIntExtra(getString(R.string.key_mode), 0)
               waitTime = intent.getIntExtra(getString(R.string.key_waiting_time), 0)
               patId = intent.getStringExtra(getString(R.string.key_pat_id))!!
               testId = intent.getLongExtra(getString(R.string.key_test_id), 0L)
               density=intent.getDoubleExtra(getString(R.string.key_density),0.0)
               if (waitTime == 2) {
                   counter = ADC_COUNT_180
               } else {
                   counter = ADC_COUNT_120
               }
           }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun deviceConnectionBroadCast(connected : Int) {
        Intent().also { intent ->
            intent.setAction(Constants.ACTION_DEVICE_CONNECTION)
            intent.putExtra(Constants.DEVICE_CONNECTED, connected)
            sendBroadcast(intent)
        }
    }

    fun teststatusBroadCast(testStatus : Boolean) {
        Intent().also { intent ->
            intent.setAction(START_TEST)
            intent.putExtra(TEST_STATUS, testStatus)
            intent.putExtra(TEST_ID,testId)
            sendBroadcast(intent)
        }
    }


    companion object {
        private val TAG = BluetoothLeServiceKt::class.java.simpleName
        //const val MSG_SESSION_ESTABLISHED = 222
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    }

    private fun registercalibirationLoadReceiver(){
        calibrationReceiver= object : BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == ACTION_CALIBRATION) {
                     loadValue = intent.getIntExtra(CALIBIRATION_LOAD, 0)
                    mLConversionFactor=intent.getDoubleExtra(CALIBIRATION_FACTOR,0.0)
                    calibrationstatus=intent.getIntExtra(CALIBIRATION_STATUS, 0)
                    sessionManager.saveCalibrationValue(mLConversionFactor.toFloat())
                    println("MLCONVERSION_VALUE"+mLConversionFactor +"CALSTATUS"+calibrationstatus)
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_CALIBRATION)
        registerReceiver(calibrationReceiver, intentFilter)
    }

    fun getLoadValue(loadValue : List<Int>) : Double{
        val avgload = loadValue.average()
     return avgload;
    }

    fun getCalibrationDate(date:Int) :String{
       val datenumberList=date.toString().toList()
        val day =datenumberList.subList(0,2).joinToString("").toInt()
        val month=datenumberList.subList(2,4).joinToString("").toInt()
        val year=datenumberList.subList(4,8).joinToString("").toInt()
        val datestring="${day}"+"-"+"${month}"+"-"+"${year}"
        return datestring
    }

    fun convertCalibirationDateFormate() : String{
        val datetime=System.currentTimeMillis()
        val simple: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        val result = Date(datetime)
        val datetimeresult=simple.format(result).split("/").joinToString("")
        val con=datetimeresult
      return datetimeresult
    }
    override fun onDestroy() {
        super.onDestroy()
        if(calibrationReceiver!=null){
            unregisterReceiver(calibrationReceiver)
        }
    }

}