
package com.codeminders.ardroneui.controllers;

import java.io.IOException;
import java.util.BitSet;

import com.codeminders.hidapi.*;

/**
 * "Afterglow" controller for PS3
 * 
 * @author lord
 * 
 */
public class AfterGlowController
{
    private static final int VENDOR_ID        = 3695;
    private static final int PRODUCT_ID       = 25346;
    private HIDDevice        dev;

    private static final int BUFSIZE          = 32;
    private static final int EXPECTED_BUFSIZE = 27;
    private byte[]           buf              = new byte[BUFSIZE];

    public AfterGlowController() throws HIDDeviceNotFoundException, IOException
    {
        dev = HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
        dev.enableBlocking();
    }

    public synchronized PS3ControllerState read() throws IOException
    {
        int n = dev.read(buf);
        if(n != EXPECTED_BUFSIZE)
        {
            throw new IOException("Received packed with unexpected size " + n);
        }

        BitSet bs = new BitSet(13);
        for(int i = 0; i < 8; i++)
        {
            if((1 & (buf[0] >> i)) == 1)
                bs.set(i);
        }
        for(int i = 0; i < 5; i++)
        {
            if((1 & (buf[1] >> i)) == 1)
                bs.set(8 + i);
        }

        int i = 0;
        boolean square = bs.get(i++);
        boolean cross = bs.get(i++);
        boolean circle = bs.get(i++);
        boolean triangle = bs.get(i++);
        boolean L1 = bs.get(i++);
        boolean R1 = bs.get(i++);
        boolean L2 = bs.get(i++);
        boolean R2 = bs.get(i++);
        boolean select = bs.get(i++);
        boolean start = bs.get(i++);
        boolean leftJoystickPress = bs.get(i++);
        boolean rightJoystickPress = bs.get(i++);
        boolean PS = bs.get(i++);

        int leftJoystickX = joystickCoordConv(buf[3]);
        int leftJoystickY = joystickCoordConv(buf[4]);
        int rightJoystickX = joystickCoordConv(buf[5]);
        int rightJoystickY = joystickCoordConv(buf[6]);

        // TODO: decode HAT switch
        int hatSwitchLeftRight = 0;
        int hatSwitchUpDown = 0;

        PS3ControllerState res = new PS3ControllerState(square, cross, circle, triangle, L1, R1, L2, R2, select, start,
                leftJoystickPress, rightJoystickPress, PS, hatSwitchLeftRight, hatSwitchUpDown, leftJoystickX,
                leftJoystickY, rightJoystickX, rightJoystickY);

        // System.err.println(res.toString());
        return res;
    }

    private int joystickCoordConv(byte b)
    {
        int v = b < 0 ? b + 256 : b;
        return v - 128;
    }

    public void close() throws IOException
    {
        dev.close();
    }

    @SuppressWarnings("unused")
    private static void printDelta(byte[] prev, int prev_size, byte[] cur, int cur_size)
    {
        if(prev_size != cur_size)
        {
            System.err.println("Packet size is different. Prev: " + prev_size + " New: " + cur_size);
            return;
        }

        for(int i = 0; i < prev_size; i++)
        {
            if(prev[i] != cur[i])
            {
                System.err.println("Index: " + i + " Prev value: " + Integer.toHexString((int) prev[i])
                        + " New value: " + Integer.toHexString((int) cur[i]));
            }
        }
    }
}
