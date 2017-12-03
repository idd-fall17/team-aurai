package com.example.androidthings.aurai;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by MichaelOudenhoven on 11/16/17.
 */

public class RoomActivity extends Activity {
    private static final String TAG = HomeActivity.class.getSimpleName();



    //seek bar for window position
    private SeekBar seekBar;
    private Button seekBarPercentButton;
    //private TextView seekBarPercent;
    //private int seekBarSetPoint = 0;

    //weather type image view
    private ImageView weatherTypeImage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /* Screen initialization */
        setContentView(R.layout.roomlayout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        Log.d(TAG, "Display height in pixels: "+ dm.heightPixels);
//        Log.d(TAG, "Display width in pixels: "+ dm.widthPixels);
//        Log.d(TAG, "Display density in dpi: "+ dm.densityDpi);


        //setup buttons and seekbar
        setupButtons();
        setupSeekBar();

        //set setpoint temp to correct value
        TextView setpointTemp = (TextView) findViewById(R.id.setpointTempRoom);
        setpointTemp.setText(Integer.toString(Constants.setPointTemp));

        TextView outDoorTempTV = (TextView) findViewById(R.id.outdoorTempRoom);
        String temperature = Integer.toString(Constants.outdoorTemp);
        outDoorTempTV.setText(temperature);

        TextView roomTempTV = (TextView) findViewById(R.id.roomTempRoom);
        String roomTemperature = Integer.toString(Constants.roomTemp);
        roomTempTV.setText(roomTemperature);

        //get weather type image view and set it to what outside is
        weatherTypeImage = findViewById(R.id.weatherTypeImageRoom);
        setWeatherType(Constants.weatherTypeString);

    }


    private void setupButtons() {
        //back arrow button
        ImageButton backButton = (ImageButton) findViewById(R.id.backButtonRoom);
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "back clicked");

                //go back to previous activity that called it (home)
                finish();
            }

        });


        ImageButton openButton = (ImageButton) findViewById(R.id.openButton);
        openButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "open windows clicked");

                //set to open point
                Constants.seekBarSetPoint = 100;

                //send bluetooth characteristic
                //writeCharacteristic(Constants.seekBarSetPoint);
                writeWindowSetpoint(Constants.seekBarSetPoint);


                //set to previous percentage - may have changed from bluetooth call from feather
                seekBarPercentButton.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");

                //get seekbar location and set it to the correct place on the slider
                int seekLocation = Constants.seekBarSetPoint/10;
                seekBar.setProgress(seekLocation);
                seekBar.setVisibility(View.INVISIBLE);


                //TODO: show progress on screen of windows opening


            }

        });

        ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "close windows clicked");
                //set to close point
                Constants.seekBarSetPoint = 0;

                //send bluetooth characteristic
                //writeCharacteristic(Constants.seekBarSetPoint);
                writeWindowSetpoint(Constants.seekBarSetPoint);

                //set to previous percentage - may have changed from bluetooth call from feather
                seekBarPercentButton.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");

                //get seekbar location and set it to the correct place on the slider
                int seekLocation = Constants.seekBarSetPoint/10;
                seekBar.setProgress(seekLocation);
                seekBar.setVisibility(View.INVISIBLE);


                //TODO: show progress on screen of windows opening

            }

        });


        //percent button to open up seek bar
        seekBarPercentButton = (Button) findViewById(R.id.windowAdjustButton);
        seekBarPercentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if seekbar is currently not shown - show it
                if (seekBar.getVisibility() == View.INVISIBLE) {
                    //set to previous percentage - may have changed from bluetooth call from feather
                    seekBarPercentButton.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");

                    //get seekbar location and set it to the correct place on the slider
                    int seekLocation = Constants.seekBarSetPoint/10;
                    seekBar.setProgress(seekLocation);

                    //make appear on click
                    seekBar.setVisibility(View.VISIBLE);


                }
                //seek bar is currently up turn it off
                else {
                    seekBar.setVisibility(View.INVISIBLE);

                }
            }
        });

    }



    /**
     * Sets up the seek bar to move in 10% increments to open the windows. Includes listener methods
     * for when the seek bar is moved.
     */
    private void setupSeekBar() {
        //set to startup value
        seekBarPercentButton.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");

        seekBar = findViewById(R.id.windowSeekRoom);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Log.d(TAG, Integer.toString(i));
                Constants.seekBarSetPoint = i*10;
                seekBarPercentButton.setText(Integer.toString(Constants.seekBarSetPoint)+ "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //don't need to do anything here as of now
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                //TODO: pop up view saying the window is being moved


               //write to bluetooth
                writeWindowSetpoint(Constants.seekBarSetPoint);


                //hide once done changing
                seekBar.setVisibility(View.INVISIBLE);


            }
        });


        //hide on startup
        seekBar.setVisibility(View.INVISIBLE);
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

    /**
     * Takes string of weather type from weather API and changes the image of the outside weather
     * type on the pico
     * @param type weather type as a string
     */
    public void setWeatherType(String type) {
        Log.d(TAG, "setting weather type");
        Log.d(TAG, "weather type " + type);

        switch (type) {
            case "Clear": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.sunny, null);

                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }


            case "Rain": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.rain_cloud, null);

                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }

            case "Clouds": {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.cloudy, null);

                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }


            default: {
                //way to adjust the weather image type after data has come in
                Drawable weatherImage = ResourcesCompat.getDrawable(getResources(), R.drawable.partly_cloudy, null);

                weatherTypeImage.setImageDrawable(weatherImage);

                break;
            }

        }
    }

    public static boolean writeWindowSetpoint(int window_setpoint){
        BluetoothGatt mBluetoothGatt = Constants.getmBluetoothLeService().getmBluetoothGatt();

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

}
