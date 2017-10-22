package com.google.android.things.contrib.driver.mma8451q;

/**
 * Created by bjoern on 9/10/17.
 */

/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

        import android.hardware.Sensor;
        import android.hardware.SensorManager;

        import com.google.android.things.userdriver.UserDriverManager;
        import com.google.android.things.userdriver.UserSensor;
        import com.google.android.things.userdriver.UserSensorDriver;
        import com.google.android.things.userdriver.UserSensorReading;

        import java.io.IOException;
        import java.util.UUID;

public class Mma8451QAccelerometerDriver implements AutoCloseable {
    private static final String TAG = Mma8451QAccelerometerDriver.class.getSimpleName();
    private static final String DRIVER_NAME = "MMA 8451 Accelerometer";
    private static final String DRIVER_VENDOR = "Adafruit";
    private static final float DRIVER_MAX_RANGE = Mma8451Q.MAX_RANGE_G * SensorManager.GRAVITY_EARTH;
    private static final float DRIVER_RESOLUTION = DRIVER_MAX_RANGE / 128.f; // 8bit signed
    private static final float DRIVER_POWER = Mma8451Q.MAX_POWER_UA / 1000.f;
    private static final int DRIVER_MIN_DELAY_US = Math.round(1000000.f/Mma8451Q.MAX_FREQ_HZ);
    private static final int DRIVER_MAX_DELAY_US = Math.round(1000000.f/Mma8451Q.MIN_FREQ_HZ);
    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";
    private Mma8451Q mDevice;
    private UserSensor mUserSensor;

    /**
     * Create a new framework accelerometer driver connected to the given I2C bus.
     * The driver emits {@link android.hardware.Sensor} with acceleration data when registered.
     * @param bus
     * @throws IOException
     * @see #register()
     */
    public Mma8451QAccelerometerDriver(String bus) throws IOException {
        mDevice = new Mma8451Q(bus);
    }

    /**
     * Close the driver and the underlying device.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregister();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register the driver in the framework.
     * @see #unregister()
     */
    public void register() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot registered closed driver");
        }
        if (mUserSensor == null) {
            mUserSensor = build(mDevice);
            UserDriverManager.getManager().registerSensor(mUserSensor);
        }
    }

    /**
     * Unregister the driver from the framework.
     */
    public void unregister() {
        if (mUserSensor != null) {
            UserDriverManager.getManager().unregisterSensor(mUserSensor);
            mUserSensor = null;
        }
    }

    static UserSensor build(final Mma8451Q mma8451q) {
        return new UserSensor.Builder()
                .setType(Sensor.TYPE_ACCELEROMETER)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setMaxRange(DRIVER_MAX_RANGE)
                .setResolution(DRIVER_RESOLUTION)
                .setPower(DRIVER_POWER)
                .setMinDelay(DRIVER_MIN_DELAY_US)
                .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                .setMaxDelay(DRIVER_MAX_DELAY_US)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        float[] sample = mma8451q.readSample();
                        for (int i=0; i<sample.length; i++) {
                            sample[i] = sample[i] * SensorManager.GRAVITY_EARTH;
                        }
                        return new UserSensorReading(
                                sample,
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH); // 120Hz
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {
                            mma8451q.setMode(Mma8451Q.MODE_ACTIVE);
                        } else {
                            mma8451q.setMode(Mma8451Q.MODE_STANDBY);
                        }
                    }
                })
                .build();
    }
}