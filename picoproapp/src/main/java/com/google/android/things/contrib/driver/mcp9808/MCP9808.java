package com.google.android.things.contrib.driver.mcp9808;

/**
 * Created by Ashis on 10/7/2017.
 */

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;


public class MCP9808 implements AutoCloseable {
    private static final String TAG = MCP9808.class.getSimpleName();

    public static final int MCP9808_I2CADDR_DEFAULT = 0x18;
    static final int MCP9808_REG_CONFIG =            0x01;

    static final int MCP9808_REG_CONFIG_SHUTDOWN =   0x0100;
    static final int MCP9808_REG_CONFIG_CRITLOCKED = 0x0080;
    static final int MCP9808_REG_CONFIG_WINLOCKED =  0x0040;
    static final int MCP9808_REG_CONFIG_INTCLR =     0x0020;
    static final int MCP9808_REG_CONFIG_ALERTSTAT =  0x0010;
    static final int MCP9808_REG_CONFIG_ALERTCTRL =  0x0008;
    static final int MCP9808_REG_CONFIG_ALERTSEL =   0x0004;
    static final int MCP9808_REG_CONFIG_ALERTPOL =   0x0002;
    static final int MCP9808_REG_CONFIG_ALERTMODE =   0x0001;

    static final int MCP9808_REG_UPPER_TEMP =        0x02;
    static final int MCP9808_REG_LOWER_TEMP =        0x03;
    static final int MCP9808_REG_CRIT_TEMP  =        0x04;
    static final int MCP9808_REG_AMBIENT_TEMP =      0x05;
    static final int MCP9808_REG_MANUF_ID =          0x06;
    static final int MCP9808_REG_DEVICE_ID =        0x07;

    private I2cDevice mDevice;


    public MCP9808(String bus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(bus, MCP9808_I2CADDR_DEFAULT);
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

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;
        //setSamplingRate(RATE_120HZ);
    }


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

    public boolean begin() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        mDevice.writeRegByte(MCP9808_REG_CONFIG, (byte) 0x0);
        return true;
    }

    public float readTempC() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        byte[] sample = new byte[2];
        mDevice.readRegBuffer(MCP9808_REG_AMBIENT_TEMP,sample,2);

        int t = (short)(((sample[0] & 0xFF) << 8) | (sample[1] & 0xFF));
        float temp = t & 0x0FFF;
        temp /= 16.0;

        if ((t & 0x1000)!=0) temp -= 256;

        float c = temp;
        return c;
    }

}
