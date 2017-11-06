package com.example.androidthings.aurai;

/**
 * Created by Ashis on 10/7/2017.
 */

import android.util.Log;

import java.io.IOException;

import com.google.android.things.contrib.driver.mcp9808.MCP9808;

public class MCP9808TempApp extends SimplePicoPro {

    MCP9808 tempSensor;
    float temp = 0.f;

    public void setup() {

        try {
            tempSensor = new MCP9808("I2C1");
            tempSensor.begin();
        } catch (IOException e) {
            Log.e("MCP9808App","setup",e);
        }


    }

    @Override
    public void loop() {
        try {
            float c = tempSensor.readTempC();
            Log.i("MCP9808", Float.toString(c) + " C");
        } catch (IOException e) {
            Log.e("MCP9808App","loop",e);
        }
        delay(100);
    }

}
