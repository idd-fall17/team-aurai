package com.example.androidthings.myproject;

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

    //direction the motor should run
    // 0 -- no movement
    // 1 -- open window
    // 2 -- close window
    public static int windowDirection = 0;

    /* UUID's for each action */
    public static UUID CUSTOM_SERVICE = UUID.fromString("20ff77b5-75c2-45b2-851d-b42b041d35c3");
    public static UUID OPEN_CLOSE = UUID.fromString("01bdc97f-12d4-4297-8195-340ba1cfc8fa");
    public static UUID SEND_POSITION = UUID.fromString("199b4278-8c07-4e71-bd0d-7f4e1fd576d2");
    public static UUID POSITION_FEEDBACK = UUID.fromString("350b1fd2-65da-4a74-b95a-0c08b9cc64bb");

    /**
     * Return a configured {@link BluetoothGattService} instance for the
     * a custom Service.
     */
    public static BluetoothGattService createCustomService() {
        BluetoothGattService service = new BluetoothGattService(CUSTOM_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Input - Position feedback from the feather back to the pico to update interface
//        BluetoothGattCharacteristic posFeedback = new BluetoothGattCharacteristic(POSITION_FEEDBACK,
//                //Read-only characteristic, supports notifications
//                BluetoothGattCharacteristic.PROPERTY_READ,
//                BluetoothGattCharacteristic.PERMISSION_READ);

        // Output - command to open and close the window
        BluetoothGattCharacteristic openClose = new BluetoothGattCharacteristic(OPEN_CLOSE,
                //write characteristic,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        // Output - send the percentage or pot reading that you want the window to go to
        BluetoothGattCharacteristic posSend = new BluetoothGattCharacteristic(SEND_POSITION,
                //write characteristic,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);


//        service.addCharacteristic(posFeedback);
        service.addCharacteristic(openClose);
        service.addCharacteristic(posSend);

        return service;
    }


    /**
     * Sets the direction of the window to open, close, or no movement
     * @param value 0 for no movement, 1 for open, 2 for close
     */
    public static void setWindowDirection(byte[] value) {
        windowDirection = value[0];
    }


    /**
     * Sets the position of the window in percentage -- feather converts percent to POT position
     * @param value int from 0-100 to be calculated by feather
     */
    public static void setWindowPos(byte[] value) {
        windowPos = value[0];
    }


//    public static byte[] getInputValue() {
//
////        byte[] field = new byte[1];
////        field[0] = (byte) mCounter;
////        mCounter = (mCounter+1) %128;
////        return field;
//
//        return null;
//    }
//
//    public static void setOutputValue (byte[] value) {
//        //handle output here
////        mCounter = value[0];
//    }




}
