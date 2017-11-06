package com.example.androidthings.aurai;

/**
 * AnalogRead example for the Pico Pro.
 * Assumes the Interactive Device Design HAT with
 * Adafruit ADS1015 ADC is installed.
 * Created by bjoern on 9/1/17.
 */

public class IddHatAnalogReadApp extends SimplePicoPro {
    float f0,f1,f2,f3;

    @Override
    public void setup() {
        analogInit(); //need to call this first before calling analogRead()
    }

    @Override
    public void loop() {
        f0=analogRead(A0);
        println("A0: "+Float.toString(f0));

        f1=analogRead(A1);
        println("A1: "+Float.toString(f1));

        f2=analogRead(A2);
        println("A2: "+Float.toString(f2));

        f3=analogRead(A3);
        println("A3: "+Float.toString(f3));
        println("-----------");

        delay(100);
    }
}
