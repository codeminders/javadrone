
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
public class AfterGlowController extends PS3Controller
{
    private static final int VENDOR_ID        = 3695;
    private static final int PRODUCT_ID       = 25346;

    private static final int BUFSIZE          = 32;
    static final int         EXPECTED_BUFSIZE = 27;
    byte[]                   buf              = new byte[BUFSIZE];

    public AfterGlowController() throws HIDDeviceNotFoundException, IOException
    {
        dev = HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
        dev.enableBlocking();
    }

    public AfterGlowController(HIDDeviceInfo hidDeviceInfo) throws IOException
    {
        dev = hidDeviceInfo.open();
        dev.enableBlocking();
    }

    @Override
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

    public static boolean isA(HIDDeviceInfo hidDeviceInfo)
    {
        return(hidDeviceInfo.getVendor_id() == VENDOR_ID && hidDeviceInfo.getProduct_id() == PRODUCT_ID);
    }
}
