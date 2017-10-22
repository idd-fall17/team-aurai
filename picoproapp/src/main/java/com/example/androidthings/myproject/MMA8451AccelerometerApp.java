package com.example.androidthings.myproject;

import android.util.Log;

import java.io.IOException;

import com.google.android.things.contrib.driver.mma8451q.Mma8451Q;

/**
 * Created by bjoern on 9/12/17.
 * Example for the Adafruit MMA8451 Accelerometer and its driver.
 *
 *
 */

public class MMA8451AccelerometerApp extends SimplePicoPro {
    Mma8451Q accelerometer;
    float[] xyz = {0.f,0.f,0.f};

    @Override
    public void setup() {

        try {
            accelerometer = new Mma8451Q("I2C1");
            accelerometer.setMode(Mma8451Q.MODE_ACTIVE);
        } catch (IOException e) {
            Log.e("MMA8451App","setup",e);
        }


    }

    @Override
    public void loop() {
        try {
            xyz = accelerometer.readSample();
            println("X: "+xyz[0]+"   Y: "+xyz[1]+"   Z: "+xyz[2]);
        } catch (IOException e) {
            Log.e("MMA8451App","loop",e);
        }
        delay(100);
    }
}
