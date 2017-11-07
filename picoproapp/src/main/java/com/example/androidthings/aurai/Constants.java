package com.example.androidthings.aurai;

import android.bluetooth.BluetoothGatt;

import java.util.UUID;

/**
 * Created by MichaelOudenhoven on 11/7/17.
 */

public class Constants {

    /* Bluetooth GATT needed in order to write characteristics */
    static private BluetoothGatt mBluetoothGatt;

    /* UUID's for each action */
    public static UUID CUSTOM_SERVICE = UUID.fromString("20ff77b5-75c2-45b2-851d-b42b041d35c3");
    public static UUID POSITION = UUID.fromString("199b4278-8c07-4e71-bd0d-7f4e1fd576d2");


    public static BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public static void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
        Constants.mBluetoothGatt = mBluetoothGatt;
    }
}
