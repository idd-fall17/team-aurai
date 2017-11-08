package com.example.androidthings.aurai;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by MichaelOudenhoven on 11/3/17.
 */

public class MotorControllerBLEProfile {
    private static final String TAG = MotorControllerBLEProfile.class.getSimpleName();

    //window position in percentage. Need feedback on startup to get position
    public static int windowPos = 0;

    /* UUID's for each action */
    public static UUID CUSTOM_SERVICE = Constants.CUSTOM_SERVICE;
    public static UUID POSITION = Constants.POSITION;


    /**
     * Return a configured {@link BluetoothGattService} instance for the
     * a custom Service.
     */
    public static BluetoothGattService createCustomService() {
        BluetoothGattService service = new BluetoothGattService(CUSTOM_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
//
        // Output - send the percentage or pot reading that you want the window to go to
        BluetoothGattCharacteristic posSend = new BluetoothGattCharacteristic(POSITION,
                //write characteristic,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(posSend);

        return service;
    }


    /**
     * Sets the position of the window in percentage -- feather converts percent to POT position
     * @param value int from 0-100 to be calculated by feather
     */
    public static void setWindowPos(byte[] value) {
        windowPos = value[0];
    }

}
