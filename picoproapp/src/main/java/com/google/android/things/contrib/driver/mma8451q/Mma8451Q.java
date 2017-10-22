package com.google.android.things.contrib.driver.mma8451q;

//see https://developer.android.com/studio/write/annotations.html
//import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



/**
 * Created by bjoern on 9/10/17.
 * Based on Android Things MMA7660 Driver:
 * https://github.com/androidthings/contrib-drivers/blob/master/mma7660fc/src/main/java/com/google/android/things/contrib/driver/mma7660fc/Mma7660Fc.java
 *
 * and Adafruit MMA8451 driver:
 * https://github.com/adafruit/Adafruit_MMA8451_Library/blob/master/Adafruit_MMA8451.h
 *
 * This is an alpha driver that only supports the default I2C address (0x1C), default range (2G), and default sampling rate (800Hz) for now.
 */

public class Mma8451Q implements AutoCloseable {
    private static final String TAG = Mma8451Q.class.getSimpleName();

    /**
     * I2C slave address of the MMA7660FC.
     */
    public static final int I2C_ADDRESS = 0x1d; // if A is GND, its 0x1C

    static final float MAX_RANGE_G = 8f;
    static final float MAX_POWER_UA = 165.f; // at 800hz
    static final float MAX_FREQ_HZ = 800.f;
    static final float MIN_FREQ_HZ = 1.56f;

    /**
     * Sampling rate of the measurement.
     */
    public @interface SamplingRate {}

    public static final int RATE_800HZ = 0b0000;
    public static final int RATE_400HZ  = 0b0001;
    public static final int RATE_200HZ  = 0b0010;
    public static final int RATE_100HZ  = 0b0011;
    public static final int RATE_50HZ   = 0b0100;
    public static final int RATE_12_5HZ   = 0b0101;
    public static final int RATE_6_25HZ   = 0b0110;
    public static final int RATE_1_56HZ   = 0b0111;

    /** Sampling Range */
    public @interface SamplingRange {}
    public static final int MMA8451_RANGE_2G   = 0b00; //+/- 2g default
    public static final int MMA8451_RANGE_4G   = 0b01;
    public static final int MMA8451_RANGE_8G   = 0b10;
    private int mRange = MMA8451_RANGE_2G;

    private static final int REG_OUT_X_MSB = 0x01;
    private static final int REG_CTRL_REG1 = 0x2A;
    private I2cDevice mDevice;

    /**
     * Power mode.
     */
    public @interface Mode {}

    public static final int MODE_STANDBY = 0x00; // STANDBY //see table 61 in datahseet
    public static final int MODE_ACTIVE = 0x01; // ACTIVE




    /**
     * Create a new MMA7660FC driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public Mma8451Q(String bus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS);
        try {
            connect(device);
        } catch (IOException|RuntimeException e) {
            try {
                close();
            } catch (IOException|RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new MMA7660FC driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ Mma8451Q(I2cDevice device) throws IOException {
        connect(device);
    }

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;
        //setSamplingRate(RATE_120HZ);
    }


    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Set current power mode.
     * @param mode
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setMode(int mode) throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        mDevice.writeRegByte(REG_CTRL_REG1, (byte) mode);
    }

    /**
     * Read an accelerometer sample.
     * @return acceleration over xyz axis in G.
     * @throws IOException
     * @throws IllegalStateException
     */
    public float[] readSample() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        byte[] sample = new byte[6];
        mDevice.readRegBuffer(REG_OUT_X_MSB,sample,6);

        int x = sample[0];// use 8 bit resolution for now
        int y = sample[2];// use 8 bit resolution for now
        int z = sample[4];// use 8 bit resolution for now

        int divider = 4096;
        //default is 2G
        if (mRange == MMA8451_RANGE_2G) {
            divider = 4096;
        }else if (mRange == MMA8451_RANGE_4G) {
            divider = 2048;
        }else if (mRange == MMA8451_RANGE_8G) {
            divider = 1024;
        }

        int MSB_only = 64; // divide by 2^6 = 64 since we're only reading 8 MSBs for now, not 6 LSBs
        return new float[] {
                ((float)x) / (divider/MSB_only),
                ((float)y) / (divider/MSB_only),
                ((float)z) / (divider/MSB_only)
        };
    }
}

