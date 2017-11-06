package com.example.androidthings.aurai;

import com.google.android.things.pio.Gpio;

/**
 * Blink two LEDs in an alternating pattern using delay()
 * Uses the LEDs above pads A (red) and B (green) on the Rainbow HAT board
 * Created by bjoern on 7/26/17.
 */

public class RainbowHatBlinkApp extends SimplePicoPro {
    Gpio redLED = GPIO_34;
    Gpio greenLED = GPIO_32;


    @Override
    public void setup() {
        pinMode(redLED, Gpio.DIRECTION_OUT_INITIALLY_LOW);
        pinMode(greenLED,Gpio.DIRECTION_OUT_INITIALLY_LOW);

    }

    @Override
    public void loop() {
        digitalWrite(redLED,HIGH);
        digitalWrite(greenLED,LOW);
        delay(100);
        digitalWrite(redLED,LOW);
        digitalWrite(greenLED,HIGH);
        delay(100);
    }
}
