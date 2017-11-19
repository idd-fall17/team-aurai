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
    public static UUID CUSTOM_SERVICE = UUID.fromString("20ff77b5-75c2-45b2-851d-b42b041d35c3");
    public static UUID POSITION = UUID.fromString("199b4278-8c07-4e71-bd0d-7f4e1fd576d2");
    public static UUID ACTUAL_POSITION = UUID.fromString("571204bc-b396-4297-a513-311c4571c023");



    public static UUID HRM = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");


    /*Positions for window proto 2 */
    public static int open = 1;
    public static int closed = 0;

    public static int seekBarSetPoint = 0;




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
