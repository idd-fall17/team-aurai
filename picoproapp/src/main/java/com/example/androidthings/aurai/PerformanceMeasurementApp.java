package com.example.androidthings.aurai;

import com.google.android.things.pio.Gpio;

/**
 * Small example to measure performance of the loop() construct.
 * Created by bjoern on 8/1/17.
 */

public class PerformanceMeasurementApp extends SimplePicoPro {
    Gpio button1 = GPIO_175;
    Gpio button2 = GPIO_174;
    Gpio led1 = GPIO_34;
    Gpio led2 = GPIO_32;
    boolean ledState = LOW;
    boolean button1LastState;
    boolean button2LastState;
    long loopCount;
    long lastTime;
    @Override
    public void setup() {
        pinMode(button1,Gpio.DIRECTION_IN);
        pinMode(button2,Gpio.DIRECTION_IN);
        pinMode(led1,Gpio.DIRECTION_OUT_INITIALLY_LOW);
        pinMode(led2,Gpio.DIRECTION_OUT_INITIALLY_LOW);
        button1LastState = digitalRead(button1);
        button2LastState = digitalRead(button2);

        loopCount=0;
        lastTime = millis();

    }

    @Override
    public void loop() {
        ledState = !ledState;
        digitalWrite(led1,ledState);
        digitalWrite(led2,ledState);
        //boolean button1NewState = digitalRead(button1);
        //boolean button2NewState = digitalRead(button2);
        /*if(button1NewState!=button1LastState) {
            println("Button 1: "+button1NewState);
            button1LastState=button1NewState;
        }
        if(button2NewState!=button2LastState) {
            println("Button 2: "+button2NewState);
            button2LastState=button2NewState;
        }
        */

        loopCount++;
        if(loopCount%1000==0) {
            long newTime = millis();
            long delta = newTime-lastTime;
            println("1000 loops in "+delta+" ms");
            lastTime=newTime;
        }
    }
}
