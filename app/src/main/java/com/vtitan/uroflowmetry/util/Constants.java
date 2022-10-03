package com.vtitan.uroflowmetry.util;

import android.os.ParcelUuid;

public class Constants {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final String MACID="48:23:35:00:56:DE";
    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final String SERVICE_UUID="18424398-7cbc-11e9-8f9e-2a86e4085a59";
    public static final String CHARACTER_UUID="15005991-b131-3396-014c-664c9867b917";
    public static final String CHAR_CONFI_UUID="00002902-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_CALI_UUID="772ae377-b3d2-4f8e-4042-5481d1e0098c";
    public static final int ADC_COUNT_120=600;
    public static final int ADC_COUNT_180=900;

    public static final String START_TEST="START_TEST";
    public static final String TEST_STATUS="TEST_STATUS";
    public static final String TEST_ID="TEST_ID";
    public static final String START_BUTTON_ENABLE="START_BUTTON_ENABLE";
    public static final String ERROR_VALUE="ERROR_VALUE";
    public static final String CORRECT_VALUE="CORRECT_VALUE";

    public static final String DEVICE_CONNECTED="DEVICE_CONNECTED";
    public static final String ACTION_DEVICE_CONNECTION="ACTION_DEVICE_CONNECTION";

    public static final String ACTION_CALIBRATION="ACTION_CALIBRATION";
    public static final String CALIBIRATION_LOAD="CALIBIRATION_LOAD";
    public static final String CALIBIRATION_STATUS="CALIBIRATION_STATUS";
    public static final String CALIBIRATION_FACTOR="CALIBIRATION_FACTOR";

    public static final int TEST_INIT = 0;
    public static final int TEST_START = 1;
    public static final int TEST_STOP = 2;
    public static final int NOTCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int DEVICEDISPLACED = 2;
    public static final int LOWBATTERY = 3;
    public static final int BATTERYALARM = 4;
    public static final int NOTCALIBIRATED = 5;
    public static final int CALIBIRATED = 6;
    public static final int CALIBRATION_NO_LOAD = 1;
    public static final int CALIBRATION_5G_LOAD = 5;
    public static final int CALIBRATION_10G_LOAD = 10;
    public static final int CALIBRATION_20G_LOAD = 20;
    public static final int CALIBRATION_50G_LOAD = 50;
    public static final int CALIBRATION_100G_LOAD = 100;
    public static final int CALIBRATION_200G_LOAD = 200;
    public static final int CALIBRATION_400G_LOAD = 400;
    public static final int CALIBRATION_500G_LOAD = 500;
    public static final int CALIBRATION_1000G_LOAD = 1000;
    public static final int CALIBRATION_1500G_LOAD = 1500;
    public static final int CALIBRATION_CONFIRM = 2000;


    public static final int LOWBATTERYSTART= 29663;
    public static final int LOWBATTERYEND = 29704;
    public static final int BATTERYALARMSTART = 28533;
    public static final int BATTERYALARMSTOP = 28573;
    public static Integer getADC_COUNT(int waittime){
        if(waittime==2){
            return ADC_COUNT_180;
        }else{
            return ADC_COUNT_120;
        }
    }
    public static final String REQUEST_KEY_FRC = "RKF";
    public static final String BUNDLE_KEY="HospitalInformation";
}
