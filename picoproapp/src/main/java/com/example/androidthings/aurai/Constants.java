package com.example.androidthings.aurai;

import android.bluetooth.BluetoothGatt;

import com.example.android.bluetoothlegatt.BluetoothLeService;

import java.util.UUID;

/**
 * Created by MichaelOudenhoven on 11/7/17.
 */

public class Constants {

    /* Bluetooth GATT needed in order to write characteristics */
    static private BluetoothGatt mBluetoothGatt = null;

    static private BluetoothLeService mBluetoothLeService = null;

    /* UUID's for each action */
    final public static UUID CUSTOM_SERVICE = UUID.fromString("20ff77b5-75c2-45b2-851d-b42b041d35c3");
    final public static UUID POSITION = UUID.fromString("199b4278-8c07-4e71-bd0d-7f4e1fd576d2");
    final public static UUID ACTUAL_POSITION = UUID.fromString("571204bc-b396-4297-a513-311c4571c023");

//    public static UUID HRM = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");



    /*Positions for window proto 2 */
    public static int open = 1;
    public static int closed = 0;


    //percentage the window is open
    public static int seekBarSetPoint = 0;

    //setpoint temperature to be used between views
    public static int setPointTemp = 0;

    //current outdoor temp that is displayed
    public static int outdoorTemp = 0;

    //outdoor weather types - //TODO: make names the png images to be used on the screen
    final public static String RAIN = "rain_cloud.png";
    final public static String CLOUDY = "cloudy";
    final public static String PARTLY_CLOUDY = "partly_cloudy";
    final public static String SUNNY = "sunny";

    //current outdoor weather type
    public static String outdoorWeatherType = "sunny.png";









    public static BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public static void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
        Constants.mBluetoothGatt = mBluetoothGatt;
    }

    public static BluetoothLeService getmBluetoothLeService() {
        return mBluetoothLeService;
    }

    public static void setmBluetoothLeService(BluetoothLeService mBluetoothLeService2) {
        mBluetoothLeService = mBluetoothLeService2;
    }
}
