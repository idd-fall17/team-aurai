package com.example.androidthings.myproject;

import com.google.android.things.pio.Gpio;

/**
 * Minimal example for how to use setEdgeTrigger() and digitalEdgeEvent().
 * uses the Rainbow HAT - blinks the red LED above pad A when pad A is pressed
 *
 * Created by bjoern on 8/29/17.
 */

public class RainbowHatMinimalEventApp extends SimplePicoPro {
    Gpio buttonA = GPIO_174;
    Gpio ledA = GPIO_34;

    @Override
    public void setup() {
        pinMode(buttonA,Gpio.DIRECTION_IN);
        pinMode(ledA,Gpio.DIRECTION_OUT_INITIALLY_LOW);
        setEdgeTrigger(buttonA,Gpio.EDGE_BOTH);
    }

    @Override
    public void loop() {} //do nothing here

    @Override
    void digitalEdgeEvent(Gpio pin, boolean value) {
        if(pin == buttonA) {
            digitalWrite(ledA, value);
        }
    }
}
