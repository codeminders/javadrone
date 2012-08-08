
package com.codeminders.ardrone.controllers;

import java.io.IOException;

import com.codeminders.hidapi.*;

import java.util.BitSet;

import org.apache.log4j.Logger;

public class SonyPS3Controller extends PS3Controller
{
    private static final int VENDOR_ID          = 1356;
    private static final int PRODUCT_ID         = 616;

    private static final int BUFSIZE            = 64;
    private static final int EXPECTED_BUFSIZE   = 32;
    private static final int EXPECTED_BUFSIZE_2 = 49;

    private byte[]           buf                = new byte[BUFSIZE];


    public static boolean isA(HIDDeviceInfo hidDeviceInfo)
    {
        return(hidDeviceInfo.getVendor_id() == VENDOR_ID && hidDeviceInfo.getProduct_id() == PRODUCT_ID);
    }

    public SonyPS3Controller() throws HIDDeviceNotFoundException, IOException
    {
        dev = HIDManager.getInstance().openById(VENDOR_ID, PRODUCT_ID, null);
        if (null != dev) {
        	dev.enableBlocking(); 
        } else {
        	throw new HIDDeviceNotFoundException("Device not found");
        }
    }

    public SonyPS3Controller(HIDDeviceInfo hidDeviceInfo) throws IOException
    {
        dev = hidDeviceInfo.open();
         if (null != dev) {
        	dev.enableBlocking();
	    } else {
	    	throw new HIDDeviceNotFoundException("Device not found");
	    }
//        dev.close();
    }

    private int joystickCoordConv(byte b)
    {
        int v = b < 0 ? b + 256 : b;
        return(v - 128);
    }

    @Override
    public PS3ControllerState read() throws IOException
    {
        int n = dev.read(buf);
        if(n != EXPECTED_BUFSIZE && n != EXPECTED_BUFSIZE_2)
        {
            throw new IOException("Received packed with unexpected size " + n);
        }

        BitSet bs = new BitSet(24);
        for(int i = 0; i < 8; i++)
        {
            if((1 & (buf[2] >> i)) == 1)
                bs.set(i);
        }

        for(int i = 0; i < 8; i++)
        {
            if((1 & (buf[3] >> i)) == 1)
                bs.set(8 + i);
        }
        for(int i = 0; i < 8; i++)
        {
            if((1 & (buf[4] >> i)) == 1)
                bs.set(16 + i);
        }

        int i = 0;
        boolean select = bs.get(i++);
        boolean leftJoystickPress = bs.get(i++);
        boolean rightJoystickPress = bs.get(i++);
        boolean start = bs.get(i++);
        bs.get(i++);
        bs.get(i++);
        bs.get(i++);
        bs.get(i++);
        boolean L2 = bs.get(i++);
        boolean R2 = bs.get(i++);
        boolean R1 = bs.get(i++);
        boolean L1 = bs.get(i++);
        boolean triangle = bs.get(i++);
        boolean circle = bs.get(i++);
        boolean cross = bs.get(i++);
        boolean square = bs.get(i++);
        boolean PS = bs.get(i++);

        int leftJoystickX = joystickCoordConv(buf[6]);
        int leftJoystickY = joystickCoordConv(buf[7]);
        int rightJoystickX = joystickCoordConv(buf[8]);
        int rightJoystickY = joystickCoordConv(buf[9]);

        // TODO: decode HAT switch
        int hatSwitchLeftRight = 0;
        int hatSwitchUpDown = 0;

        PS3ControllerState res = new PS3ControllerState(square, cross, circle, triangle, L1, R1, L2, R2, select, start,
                leftJoystickPress, rightJoystickPress, PS, hatSwitchLeftRight, hatSwitchUpDown, leftJoystickX,
                leftJoystickY, rightJoystickX, rightJoystickY);

        return res;
    }

}
