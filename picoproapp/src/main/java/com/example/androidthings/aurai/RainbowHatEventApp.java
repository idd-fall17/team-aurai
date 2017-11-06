package com.example.androidthings.aurai;

import com.google.android.things.pio.Gpio;

/**
 * Created by bjoern on 8/29/17.
 * Uses the Rainbow HAT - blinks the red LED above pads A,B,C when corresponding pad is pressed
 */

public class RainbowHatEventApp extends SimplePicoPro {
    /* Mapping of buttons and LEDs to GPIO pins */
    Gpio buttonA = GPIO_174;
    Gpio buttonB = GPIO_175;
    Gpio buttonC = GPIO_39;

    Gpio ledA = GPIO_34;
    Gpio ledB = GPIO_32;
    Gpio ledC = GPIO_37;

    @Override
    public void setup() {
        pinMode(buttonA,Gpio.DIRECTION_IN);
        pinMode(buttonB,Gpio.DIRECTION_IN);
        pinMode(buttonC,Gpio.DIRECTION_IN);
        pinMode(ledA,Gpio.DIRECTION_OUT_INITIALLY_LOW);
        pinMode(ledB,Gpio.DIRECTION_OUT_INITIALLY_LOW);
        pinMode(ledC,Gpio.DIRECTION_OUT_INITIALLY_LOW);
        setEdgeTrigger(buttonA,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonB,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonC,Gpio.EDGE_BOTH);
    }

    @Override
    public void loop() {} //do nothing here

    @Override
    void digitalEdgeEvent(Gpio pin, boolean value) {
        if(pin == buttonA) {
            digitalWrite(ledA,!value);
        } else if (pin == buttonB) {
            digitalWrite(ledB,!value);
        } else if (pin == buttonC) {
            digitalWrite(ledC,!value);
        }
    }
}
