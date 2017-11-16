package com.example.androidthings.aurai;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.android.bluetoothlegatt.BluetoothLeService;
import com.example.android.bluetoothlegatt.DeviceControlActivity;
import com.example.android.bluetoothlegatt.DeviceScanActivity;
import com.example.android.bluetoothlegatt.SampleGattAttributes;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import static com.example.android.bluetoothlegatt.BluetoothLeService.EXTRA_DATA;

/**
 * Created by MichaelOudenhoven on 11/3/17.
 */

public class HomeActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    /* Bluetooth API */
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    //image buttons to hide and unhide when user trying to change the set point
    private ImageButton upSetButton;
    private ImageButton downSetButton;
    private Button setTempButton;
    //boolean to control when the setpoint arrows should be shown or not
    private boolean settingTemp = false;
    //temperature the room is set to
    private int setPointTemp = 20;


    //LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Screen initialization */
        setContentView(R.layout.homelayout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        Log.d(TAG, "Display height in pixels: "+ dm.heightPixels);
        Log.d(TAG, "Display width in pixels: "+ dm.widthPixels);
        Log.d(TAG, "Display density in dpi: "+ dm.densityDpi);



        /* Set up bluetooth */
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        // We can't continue without proper Bluetooth support
        if (!checkBluetoothSupport(bluetoothAdapter)) {
            finish();
        }

        // IDD: SET A CUSTOM DEVICE NAME - is iMX7 by default
        // @see https://stackoverflow.com/questions/8377558/change-the-android-bluetooth-device-name
        // No more than 8 characters or advertising will fail (" LE Advertise Failed: 1")
        bluetoothAdapter.setName("Aurai");

        // Register for system Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling");
            bluetoothAdapter.enable();
        } else {
            Log.d(TAG, "Bluetooth enabled...starting services");
            startAdvertising();
            startServer();
        }

        //setup buttons onclick listeners
        setupButtons();

        //hide action bar from view
        getActionBar().hide();



        //TODO: call to server to get stored setpoint temperature for the room and load it into the button text


        //hide setpoint adjustment on launch
        upSetButton.setVisibility(View.INVISIBLE);
        downSetButton.setVisibility(View.INVISIBLE);
        settingTemp = false;


        //TODO: make slider for window adjustment on home view hidden

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            stopServer();
            stopAdvertising();
        }

        unregisterReceiver(mBluetoothReceiver);
    }

    /**
     * Verify the level of Bluetooth support provided by the hardware.
     * @param bluetoothAdapter System {@link BluetoothAdapter}.
     * @return true if Bluetooth is properly supported, false otherwise.
     */
    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
                    break;
                default:
                    // Do nothing
            }

        }
    };

    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    private void startAdvertising() {
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();

        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        if (mBluetoothLeAdvertiser == null) {
            Log.w(TAG, "Failed to create advertiser");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(MotorControllerBLEProfile.CUSTOM_SERVICE))
                .build();

        mBluetoothLeAdvertiser
                .startAdvertising(settings, data, mAdvertiseCallback);
    }

    /**
     * Stop Bluetooth advertisements.
     */
    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from the Time Profile.
     */
    private void startServer() {
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            Log.w(TAG, "Unable to create GATT server");
            return;
        }


        mBluetoothGattServer.addService(MotorControllerBLEProfile.createCustomService());


    }

    /**
     * Shut down the GATT server.
     */
    private void stopServer() {
        if (mBluetoothGattServer == null) return;

        mBluetoothGattServer.close();
    }

    /**
     * Callback to receive information about the advertisement process.
     */
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
        }
    };



    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics are handled here.
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
            }
        }

        /**
         * Check for all writable characteristics
         **/
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {

            if (MotorControllerBLEProfile.POSITION.equals(characteristic.getUuid())) {
                Log.i(TAG, "Write Output Characteristic");


                MotorControllerBLEProfile.setWindowPos(value);

                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            value);
                }
            }

        }

        /**
         * Check for all readable characteristics
         **/
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {

            Log.d(TAG, "onCharacteristicReadRequest Called");
            if (characteristic.getUuid().toString() == SampleGattAttributes.HEART_RATE_MEASUREMENT) {
                Log.d(TAG, "onCharacteristicReadRequest got HRM charactertistic");

                String x = new String(characteristic.getValue());
                 Log.d(TAG, "characteristic value: " + x);

            }

            Log.d(TAG, "onCharacteristicReadRequest and didnt get HRM UUID");

//            if (MotorControllerBLEProfile.POSITION_FEEDBACK.equals(characteristic.getUuid())) {
//                Log.i(TAG, "Read Input Characteristic");
////                mBluetoothGattServer.sendResponse(device,
////                        requestId,
////                        BluetoothGatt.GATT_SUCCESS,
////                        0,
////                        MotorControllerBLEProfile.getInputValue());
//            }
//
//            else {
//                // Invalid characteristic
//                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.getUuid());
//                mBluetoothGattServer.sendResponse(device,
//                        requestId,
//                        BluetoothGatt.GATT_FAILURE,
//                        0,
//                        null);
//            }
        }
    };


    /**
     * Takes a given integer window position and writes the value to the feather using BLE GATT service.
     *
     * @param position 0-100 int value of the window position
     * @return true if successfully wrote characteristic, false if failure
     */
    private boolean writeCharacteristic(int position) {

        BluetoothGatt mBluetoothGatt = Constants.getmBluetoothLeService().getmBluetoothGatt();

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection - bluetooth GATT is null in writeCharacteristic()");
            return false;
        }


        //TODO:
        List<BluetoothGattService> list = Constants.getmBluetoothLeService().getSupportedGattServices();
        Log.d(TAG, list.toString());

        BluetoothGattService customService = null;

        for(int i = 0; i< list.size(); i++){
            Log.d(TAG, list.get(i).getUuid().toString());

            if (list.get(i).getUuid().toString() == Constants.CUSTOM_SERVICE.toString()) {
                Log.d(TAG, "heart rate service detected");
                customService = list.get(i);
                break;
            }
        }

        customService = mBluetoothGatt.getService(Constants.CUSTOM_SERVICE);

        if (customService == null) {
            Log.e(TAG, "service not found!");
            return false;
        }


//        BluetoothGattService Service = mBluetoothGatt.getService(Constants.CUSTOM_SERVICE);
////        BluetoothGattService Service = mBluetoothGatt.getService(Constants.CUSTOM_SERVICE);
//        if (Service == null) {
//            Log.e(TAG, "service not found!");
//            return false;
//        }
        BluetoothGattCharacteristic charac = customService.getCharacteristic(Constants.POSITION);
        if (charac == null) {
            Log.e(TAG, "position characteristic not found!");
            return false;
        }



        mBluetoothGatt.setCharacteristicNotification(charac, true);

        byte[] value =  Integer.toHexString(position).getBytes();

        //Log.d(TAG, "Hex String: " + Integer.toHexString(position));

        charac.setValue(position, BluetoothGattCharacteristic.FORMAT_UINT32, 0);

        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        return status;

    }

    private void setupButtons() {
        /* Setup button click for BLE setup screen */
        ImageButton BLESetupButton = (ImageButton) findViewById(R.id.BLESetup);
        BLESetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(HomeActivity.this, DeviceScanActivity.class);
                myIntent.putExtra("key", "myString"); //Optional parameters
                HomeActivity.this.startActivity(myIntent);
            }
        });

        /* Setup button click for closing window */
        Button closeButton = (Button) findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean success = writeCharacteristic(Constants.closed);

                if (!success) {
                    Log.d(TAG, "characteristic did not write to close the window");
                }


            }
        });

        /* Setup button click for opening window */
        Button openButton = (Button) findViewById(R.id.open);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean success = writeCharacteristic(Constants.open);

                if (!success) {
                    Log.d(TAG, "characteristic did not write to open the window");
                }

            }
        });

        Button sensorReadingButton = (Button) findViewById(R.id.sensor_data);
        sensorReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                try {
//                    URL url = new URL("http://aurai-web.herokuapp.com/api/sensorreadings/?format=json");
//                    URLConnection uc = url.openConnection();
//                    String userpass = "admin:admin123";
//                    String basicAuth = "Basic " + new String(userpass.getBytes());
//                    uc.setRequestProperty("Authorization", basicAuth);
//                    InputStream in = uc.getInputStream();
//                    String theString = IOUtils.toString(in, "UTF-8");
//                    Log.d(TAG, "onClick: "+theString);
//                }
//                catch (IOException e){
//                    throw new RuntimeException(e);
//                }
                RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
                String url ="http://aurai-web.herokuapp.com/api/sensorreadings/?format=json";

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            TextView tv = findViewById(R.id.sensor_data_text);
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                tv.setText("Response is: "+ response.substring(0,500));
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e( TAG ,error.toString());
                        TextView tv = findViewById(R.id.sensor_data_text);
                        tv.setText("That didn't work!");
                    }

                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);

            }
        });

        //up button on temperature setpoint
        upSetButton = (ImageButton) findViewById(R.id.upSet);
        upSetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "up setpoint clicked");
                setPointTemp += 1;
                setTempButton.setText(Integer.toString(setPointTemp));
            }
        });

        //down button on temperature setpoint
        downSetButton = (ImageButton) findViewById(R.id.downSet);
        downSetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "down setpoint clicked");
                setPointTemp -= 1;
                setTempButton.setText(Integer.toString(setPointTemp));
            }
        });

        //button as the setpoint temperature to toggle the setting temperature on and off
        //TODO: add logic to cause a timeout of setpoint if user doesn't click arrows after 3 seconds
        setTempButton = (Button) findViewById(R.id.setpointTempButton);
        setTempButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "setpoint clicked");

                //if the user is setting the temp and clicks the temperature turn the arrows off
                //b/c this is the final set temperature they want
                if (settingTemp) {
                    upSetButton.setVisibility(View.INVISIBLE);
                    downSetButton.setVisibility(View.INVISIBLE);
                    settingTemp = false;
                }
                //user wants to edit the temperature turn the set point arrows on
                else {
                    upSetButton.setVisibility(View.VISIBLE);
                    downSetButton.setVisibility(View.VISIBLE);
                    settingTemp = true;
                }
            }
        });



        //right arrow button
        ImageButton rightArrowButton = (ImageButton) findViewById(R.id.rightActivityButton);
        rightArrowButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "right arrow clicked");

                //TODO: create intent to move to room view

            }

        });

        //left arrow button
        ImageButton leftArrowButton = (ImageButton) findViewById(R.id.leftActivityButton);
        leftArrowButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "left arrow clicked");

                //TODO: create intent to move to graph view

            }

        });

        //window button
        ImageButton windowAdjustButton = (ImageButton) findViewById(R.id.windowButton);
        windowAdjustButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "window adjust clicked");

                //TODO: make slider with animation appear
            }

        });


    }
}
