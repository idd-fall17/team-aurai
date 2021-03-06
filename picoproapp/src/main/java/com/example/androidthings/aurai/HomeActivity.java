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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.android.bluetoothlegatt.BluetoothLeService;
import com.example.android.bluetoothlegatt.DeviceScanActivity;
import com.example.android.bluetoothlegatt.SampleGattAttributes;

import java.nio.ByteBuffer;
import java.util.List;
import com.google.gson.Gson;

import org.json.*;

import static com.example.androidthings.aurai.Constants.setPointTemp;

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

    //seek bar for window position
    private SeekBar seekBar;
    private TextView seekBarPercent;
    //private int seekBarSetPoint = 0;

    //weather type image view
    private ImageView weatherTypeImage;
    Handler handler = new Handler();
    int delay = 15000; //milliseconds

    //handler runnable
    Runnable r = new Runnable() {
        public void run(){
            getSensorData();
            getWeatherData();
            windowControl();
            handler.postDelayed(this, delay);
        }
    };


    //text view that appears when the window is moving
    private TextView windowMovingTV;
    Animation fadeIn;
    Animation fadeOut;
    int fadeCount = 0;


    //setup broadcast receiver to tell when bluetooth connection is gained and lost
    //this will then be used to change the bluetooth icon color
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(Constants.BT_EXTRA);
            //log message for debugging
            Log.d("receiver", "Got message: " + message);

            //verify state and update bluetooth icon based on the connection state
            if(message == Constants.BT_CONNECTED) {
                //way to adjust the weather image type after data has come in
                Drawable BTImage = ResourcesCompat.getDrawable(getResources(), R.drawable.blue_bt_icon, null);
                ImageButton BTbutton = (ImageButton) findViewById(R.id.BLESetup);
                BTbutton.setImageDrawable(BTImage);


            } else if (message == Constants.BT_DISCONNECTED) {
                //way to adjust the weather image type after data has come in
                Drawable BTImage = ResourcesCompat.getDrawable(getResources(), R.drawable.white_bt_icon, null);
                ImageButton BTbutton = (ImageButton) findViewById(R.id.BLESetup);
                BTbutton.setImageDrawable(BTImage);
            }
        }
    };


    //LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Screen initialization */
        setContentView(R.layout.homelayout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);

//        Log.d(TAG, "Display height in pixels: "+ dm.heightPixels);
//        Log.d(TAG, "Display width in pixels: "+ dm.widthPixels);
//        Log.d(TAG, "Display density in dpi: "+ dm.densityDpi);


        //register to receive messages
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Constants.BT_CONNECTION));

//        /* Set up bluetooth */
//        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
//        // We can't continue without proper Bluetooth support
//        if (!checkBluetoothSupport(bluetoothAdapter)) {
//            finish();
//        }
//
//        // IDD: SET A CUSTOM DEVICE NAME - is iMX7 by default
//        // @see https://stackoverflow.com/questions/8377558/change-the-android-bluetooth-device-name
//        // No more than 8 characters or advertising will fail (" LE Advertise Failed: 1")
//        bluetoothAdapter.setName("Aurai");
//
//        // Register for system Bluetooth events
//        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(mBluetoothReceiver, filter);
//        if (!bluetoothAdapter.isEnabled()) {
//            Log.d(TAG, "Bluetooth is currently disabled...enabling");
//            bluetoothAdapter.enable();
//        } else {
//            Log.d(TAG, "Bluetooth enabled...starting services");
//            startAdvertising();
//            startServer();
//        }

        //setup buttons onclick listeners
        setupButtons();
        setupSwitch();

        //hide setpoint adjustment on launch
        upSetButton.setVisibility(View.INVISIBLE);
        downSetButton.setVisibility(View.INVISIBLE);
        settingTemp = false;

        //get the window moving text view and hide it
        windowMovingTV = findViewById(R.id.WindowMoving);
        windowMovingTV.setAlpha(0.0f);
        //windowMovingTV.setVisibility(View.INVISIBLE);

        //set up the seek bar
        setupSeekBar();
        setupAnimations();

        //get previous set point temp if stored
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        //get integer - will return -1 if not in storage
        setPointTemp = preferences.getInt("setPointTemp",-1);

        //if not previously stored - set to 20 C
        if (setPointTemp == -1) {
            setPointTemp = 20;
        }

        //set setpoint value to value
        setTempButton.setText(Integer.toString(setPointTemp));


        //way to adjust the weather image type after data has come in
        Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.sunny, null);
        weatherTypeImage = (ImageView) findViewById(R.id.weatherTypeImageHome);
        weatherTypeImage.setImageDrawable(weatherImage);




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

        // Unregister since the activity is about to be closed.
        // This is somewhat like [[NSNotificationCenter defaultCenter] removeObserver:name:object:]
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }




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

//        for(int i = 0; i< list.size(); i++){
//            Log.d(TAG, list.get(i).getUuid().toString());
//
//            if (list.get(i).getUuid().toString() == Constants.CUSTOM_SERVICE.toString()) {
//                Log.d(TAG, "window service detected");
//                customService = list.get(i);
//                break;
//            }
//        }

        customService = mBluetoothGatt.getService(Constants.CUSTOM_SERVICE);

        if (customService == null) {
            Log.e(TAG, "service not found!");
            return false;
        }

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

                boolean success = writeWindowSetpoint(Constants.closed);

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

                boolean success = writeWindowSetpoint(Constants.open);

                if (!success) {
                    Log.d(TAG, "characteristic did not write to open the window");
                }

            }
        });

        Button sensorReadingButton = (Button) findViewById(R.id.sensor_data);
        sensorReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
                handler.postDelayed(r, delay);
                Switch s = findViewById(R.id.handlerSwitch);
                //turn switch on programatically
                s.setChecked(true);

//                RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
//                String url ="http://aurai-web.herokuapp.com/api/sensorreading/0x0229/?format=json";
//
//                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                        new Response.Listener<String>() {
////                            TextView tv = findViewById(R.id.sensor_data_text);
//                            TextView sensorTemp = findViewById(R.id.roomTempHome);
//                            String roomTemperature = sensorTemp.getText().toString();
//                            float roomTempf = Float.parseFloat(roomTemperature);
//                            int roomTemp = Math.round(roomTempf);
//
//
//
//                            @Override
//                            public void onResponse(String response) {
//                                // Display the first 500 characters of the response string.
////                                tv.setText("Response is: "+ response.substring(0,500));
//                                try {
//                                    JSONArray reader = new JSONArray(response);
//                                    JSONObject sensor = reader.getJSONObject(0);
//                                    roomTemperature = sensor.getString("air_temp");
//                                    roomTempf = Float.parseFloat(roomTemperature);
//                                    roomTemp = Math.round(roomTempf);
//                                    roomTemperature = Integer.toString(roomTemp);
//
//                                }
//                                catch(org.json.JSONException e){
//                                    Log.e(TAG, "Couldn't get weather data.");
//                                }
//
//                                Constants.roomTemp = roomTemp;
//                                sensorTemp.setText(roomTemperature);
//                            }
//                        }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e( TAG ,error.toString());
//                        TextView tv = findViewById(R.id.sensor_data_text);
//                        tv.setText("That didn't work!");
//                    }
//
//                });
//
//                // Add the request to the RequestQueue.
//                queue.add(stringRequest);
//
//                String weather_url = "http://api.openweathermap.org/data/2.5/weather?q=Berkeley&appid=a5c2d2ffd12b150a969a22377d62efa3";
//                StringRequest weather_stringRequest = new StringRequest(Request.Method.GET, weather_url,
//                        new Response.Listener<String>() {
//                            TextView tv = findViewById(R.id.outdoorTempHome);
//                            @Override
//                            public void onResponse(String response) {
//                                Log.d(TAG, response);
//                                String outDoorTemperature = tv.getText().toString();
//                                float outDoorTempf = Float.parseFloat(outDoorTemperature);
//                                int outDoorTemp = Math.round(outDoorTempf);
//
//                                try {
//                                    JSONObject reader = new JSONObject(response);
//                                    JSONObject main = reader.getJSONObject("main");
//                                    outDoorTemperature = main.getString("temp");
//                                    outDoorTempf = Float.parseFloat(outDoorTemperature);
//                                    outDoorTemp = Math.round(outDoorTempf) - 273;
//                                    outDoorTemperature = Integer.toString(outDoorTemp);
//
//                                }
//                                catch(org.json.JSONException e){
//                                    Log.e(TAG, "Couldn't get weather data.");
//                                }
//
//                                Constants.outdoorTemp = outDoorTemp;
//                                tv.setText(outDoorTemperature);
//                            }
//                        }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e( TAG ,error.toString());
//                    }
//
//                });
//
//                queue.add(weather_stringRequest);

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
        setTempButton = (Button) findViewById(R.id.setpointTempButtonHome);
        setTempButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "setpoint clicked");

                //if the user is setting the temp and clicks the temperature turn the arrows off
                //b/c this is the final set temperature they want
                if (settingTemp) {
                    upSetButton.setVisibility(View.INVISIBLE);
                    downSetButton.setVisibility(View.INVISIBLE);
                    settingTemp = false;

                    //save new temp to memory
                    SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("setPointTemp", setPointTemp);
                    editor.commit();

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
                Intent myIntent = new Intent(HomeActivity.this, RoomActivity.class);
                myIntent.putExtra("key", "myString"); //Optional parameters
                HomeActivity.this.startActivity(myIntent);

            }

        });

        //left arrow button
        ImageButton leftArrowButton = (ImageButton) findViewById(R.id.leftActivityButton);
        leftArrowButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "left arrow clicked");

                //TODO: create intent to move to graph view
                Intent myIntent = new Intent(HomeActivity.this, GraphActivity.class);
                myIntent.putExtra("key", "myString"); //Optional parameters
                HomeActivity.this.startActivity(myIntent);

            }

        });

        //window button
        ImageButton windowAdjustButton = (ImageButton) findViewById(R.id.windowButton);
        windowAdjustButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "window adjust clicked");
                Constants.seekBarSetPoint = Constants.window_position;

                //TODO: make slider with animation appear

                //if seekbar is currently not shown - show it
                if (seekBar.getVisibility() == View.INVISIBLE) {
                    //set to previous percentage - may have changed from bluetooth call from feather
                    seekBarPercent.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");

                    //get seekbar location and set it to the correct place on the slider
                    int seekLocation = Constants.seekBarSetPoint/10;
                    seekBar.setProgress(seekLocation);

                    //make appear on click
                    seekBar.setVisibility(View.VISIBLE);
                    seekBarPercent.setVisibility(View.VISIBLE);

                }
                //seek bar is currently up turn it off
                else {
                    seekBar.setVisibility(View.INVISIBLE);
                    seekBarPercent.setVisibility(View.INVISIBLE);
                }

            }

        });



        /*
        BUTTONS FOR FORCED OUTDOOR TEMPERATURE
         */
        Button outdoorHighButton = (Button) findViewById(R.id.outdoorHigh);
        outdoorHighButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //force outdoor temp higher than indoor temp
                Constants.outdoorTemp = Constants.roomTemp + 5;

                //update the outdoor temp on the UI
                TextView outdoorTV = (TextView) findViewById(R.id.outdoorTempHome);
                outdoorTV.setText(Integer.toString(Constants.outdoorTemp));

//                windowControl();
            }
        });

        Button outdoorLowButton = (Button) findViewById(R.id.outdoorLow);
        outdoorLowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //force outdoor temp lower than indoor temp
                Constants.outdoorTemp = Constants.roomTemp - 5;

                //update the outdoor temp on the UI
                TextView outdoorTV = (TextView) findViewById(R.id.outdoorTempHome);
                outdoorTV.setText(Integer.toString(Constants.outdoorTemp));

//                windowControl();


            }
        });

    }

    public void getSensorData(){
        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
        String url ="http://aurai-web.herokuapp.com/api/sensorreading/0x0229/?format=json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    //                            TextView tv = findViewById(R.id.sensor_data_text);
                    TextView sensorTemp = findViewById(R.id.roomTempHome);
                    String roomTemperature = sensorTemp.getText().toString();
                    float roomTempf = Float.parseFloat(roomTemperature);
                    int roomTemp = Math.round(roomTempf);



                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                                tv.setText("Response is: "+ response.substring(0,500));
                        try {
                            JSONArray reader = new JSONArray(response);
                            JSONObject sensor = reader.getJSONObject(0);
                            roomTemperature = sensor.getString("air_temp");
                            roomTempf = Float.parseFloat(roomTemperature);
                            roomTemp = Math.round(roomTempf);
                            roomTemperature = Integer.toString(roomTemp);


                        }
                        catch(org.json.JSONException e){
                            Log.e(TAG, "Couldn't get weather data.");
                        }

                        Constants.roomTemp = roomTemp;
                        sensorTemp.setText(roomTemperature);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( TAG ,error.toString());
                //TextView tv = findViewById(R.id.sensor_data_text);
                //tv.setText("That didn't work!");
            }

        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getWeatherData(){
        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
        String weather_url = "http://api.openweathermap.org/data/2.5/weather?q=Berkeley&appid=a5c2d2ffd12b150a969a22377d62efa3";
        StringRequest weather_stringRequest = new StringRequest(Request.Method.GET, weather_url,
                new Response.Listener<String>() {
                    TextView tv = findViewById(R.id.outdoorTempHome);
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        String outDoorTemperature = tv.getText().toString();
                        float outDoorTempf = Float.parseFloat(outDoorTemperature);
                        int outDoorTemp = Math.round(outDoorTempf);

                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject main = reader.getJSONObject("main");
                            outDoorTemperature = main.getString("temp");
                            outDoorTempf = Float.parseFloat(outDoorTemperature);
                            outDoorTemp = Math.round(outDoorTempf) - 273;
                            outDoorTemperature = Integer.toString(outDoorTemp);

                            JSONArray weather = reader.getJSONArray("weather");
                            JSONObject main2 = weather.getJSONObject(0);

                            String weatherType = main2.getString("main");

                            //set in constants to be used for other activities on load
                            Constants.weatherTypeString = weatherType;
                            setWeatherType(weatherType);



                        }
                        catch(org.json.JSONException e){
                            Log.e(TAG, "Couldn't get weather data.");
                        }

                        Constants.outdoorTemp = outDoorTemp;
                        tv.setText(outDoorTemperature);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( TAG ,error.toString());
            }

        });

        queue.add(weather_stringRequest);
    }
    public void windowControl(){
        int roomTemp = Constants.roomTemp;
        int outdoorTemp = Constants.outdoorTemp;
        int setpointTemp = Constants.setPointTemp;
        int windowScale = 5;
        int window_setpoint = Constants.window_setpoint;

//        getWindowPosition();

        try{
            Constants.getmBluetoothLeService().addToBleQueue(-1);
        }
        catch (Exception e){
            Log.e(TAG, "windowControl: ", e);
        }
//        boolean success2 = getWindowPosition();
//
//        if (!success2) {
//            Log.d(TAG, "characteristic could not be read to get window position");
//        }

        //        When it's too warm inside, but cool outside.
        if (roomTemp > setpointTemp) {
            if (roomTemp > outdoorTemp) {
                window_setpoint = (roomTemp - setpointTemp) * 100;
                window_setpoint /= windowScale;
            } else {
                window_setpoint = 0;
            }
        }
        //        When it's too cold inside, but warm outside
        else{
            if (roomTemp < outdoorTemp){
                window_setpoint = (setpointTemp - roomTemp) * 100;
                window_setpoint /= windowScale;
            }
            else{
                window_setpoint = 0;
            }
        }

        if (window_setpoint > 100)
            window_setpoint = 100;

//        Log.d(TAG, "Window setpoint: " + window_setpoint);
        Constants.window_setpoint = window_setpoint;
        try {
            Constants.getmBluetoothLeService().addToBleQueue(window_setpoint);
        }
        catch (Exception e){
            Log.e(TAG, "windowControl: ", e);
        }
//        boolean success = writeWindowSetpoint(Constants.window_setpoint);
//        if (!success) {
//            Log.d(TAG, "characteristic did not write to change window setpoint");
//        }

    }

    public static boolean writeWindowSetpoint(int window_setpoint){
        BluetoothGatt mBluetoothGatt;

        try {
            mBluetoothGatt = Constants.getmBluetoothLeService().getmBluetoothGatt();
        } catch (Exception e) {
            return false;
        }


        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection - bluetooth GATT is null in writeWindowSetpoint()");
            return false;
        }

        List<BluetoothGattService> list = Constants.getmBluetoothLeService().getSupportedGattServices();
        Log.d(TAG, list.toString());

        BluetoothGattService customService = null;
        customService = mBluetoothGatt.getService(Constants.CUSTOM_SERVICE);

        if (customService == null) {
            Log.e(TAG, "service not found!");
            return false;
        }

        BluetoothGattCharacteristic charac = customService.getCharacteristic(Constants.POSITION);
        if (charac == null) {
            Log.e(TAG, "position setpoint characteristic not found!");
            return false;
        }

        mBluetoothGatt.setCharacteristicNotification(charac, true);

        byte[] value =  Integer.toHexString(window_setpoint).getBytes();
        charac.setValue(window_setpoint, BluetoothGattCharacteristic.FORMAT_UINT32, 0);

        boolean status = mBluetoothGatt.writeCharacteristic(charac);
//        mBluetoothGatt.setCharacteristicNotification(charac, false);
        return status;
    }

    public static boolean getWindowPosition(){
        BluetoothGatt mBluetoothGatt = Constants.getmBluetoothLeService().getmBluetoothGatt();
        int window_position;

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection - bluetooth GATT is null in getWindowPosition()");
            return false;
        }

        List<BluetoothGattService> list = Constants.getmBluetoothLeService().getSupportedGattServices();
        Log.d(TAG, list.toString());

        BluetoothGattService customService = null;
        customService = mBluetoothGatt.getService(Constants.CUSTOM_SERVICE);

        if (customService == null) {
            Log.e(TAG, "service not found!");
            return false;
        }

        BluetoothGattCharacteristic charac = customService.getCharacteristic(Constants.ACTUAL_POSITION);
        if (charac == null) {
            Log.e(TAG, "position characteristic not found!");
            return false;
        }

        mBluetoothGatt.setCharacteristicNotification(charac, true);
        Log.d(TAG, "Attempting to read " + charac.getUuid());

//        Constants.getmBluetoothLeService().readCharacteristic(charac);
        byte[] value = charac.getValue();

        Log.d(TAG, "Window position (byte address): " + value);

        try {
            Log.d(TAG, "Window position: " + charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));

            window_position = charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);

            Constants.window_position = window_position;
        }
        catch (Exception e){
            Log.e(TAG, "getWindowPosition: ",e );
        }
        Log.d(TAG, "Window position: " + Constants.window_position);


        boolean status = mBluetoothGatt.readCharacteristic(charac);
//        mBluetoothGatt.setCharacteristicNotification(charac, false);
        return status;
    }

    /**
     * Sets up the seek bar to move in 10% increments to open the windows. Includes listener methods
     * for when the seek bar is moved.
     */
    private void setupSeekBar() {
        seekBarPercent = findViewById(R.id.windowPercent);
        //set to startup value
        seekBarPercent.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");

        seekBar = findViewById(R.id.windowSeek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Log.d(TAG, Integer.toString(i));
                Constants.seekBarSetPoint = i*10;
                seekBarPercent.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //don't need to do anything here as of now
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //start fade in and the rest of the animation will trigger
                windowMovingTV.startAnimation(fadeIn);

                //send bluetooth characteristic
                //writeCharacteristic(Constants.seekBarSetPoint);
                writeWindowSetpoint(Constants.seekBarSetPoint);

                //hide once done changing
                seekBar.setVisibility(View.INVISIBLE);
                seekBarPercent.setVisibility(View.INVISIBLE);

            }
        });


        //hide on startup
        seekBar.setVisibility(View.INVISIBLE);
        seekBarPercent.setVisibility(View.INVISIBLE);
    }

    /**
     * Sets up the switch to turn the handler on and off
     */
    public void setupSwitch() {
        Switch s = findViewById(R.id.handlerSwitch);

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "state changed to: " + Boolean.toString(b));

                if (b == true) {
                    handler.postDelayed(r, delay);
                }
                else {
                    handler.removeCallbacks(r);
                }
            }
        });
    }

    /**
     * Takes string of weather type from weather API and changes the image of the outside weather
     * type on the pico
     * @param type weather type as a string
     */
    public void setWeatherType(String type){
        Log.d(TAG, "setting weather type");
        Log.d(TAG, "weather type " + type);

        switch (type) {
            case "Clear": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.sunny, null);
                weatherTypeImage = (ImageView) findViewById(R.id.weatherTypeImageHome);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }


            case "Rain": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.rain_cloud, null);
                weatherTypeImage = (ImageView) findViewById(R.id.weatherTypeImageHome);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }

            case "Clouds": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.cloudy, null);
                weatherTypeImage = (ImageView) findViewById(R.id.weatherTypeImageHome);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }


            default: {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.partly_cloudy, null);
                weatherTypeImage = (ImageView) findViewById(R.id.weatherTypeImageHome);
                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }

        }

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
     * Sets up the animations and listeners for the moving window text view to fade in and out
     */
    public void setupAnimations() {
        //make moving window appear and disappear
        //windowMovingTV.setAlpha(1.0f);


        fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                windowMovingTV.setAlpha(1.0f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                windowMovingTV.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);
        fadeOut.setStartOffset(100);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (fadeCount < 4) {
                    fadeCount++;
                    windowMovingTV.startAnimation(fadeIn);
                }
                else if (fadeCount == 4) {
                    //reset animation for next time
                    fadeCount = 0;
                    windowMovingTV.setAlpha(0.0f);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

}
