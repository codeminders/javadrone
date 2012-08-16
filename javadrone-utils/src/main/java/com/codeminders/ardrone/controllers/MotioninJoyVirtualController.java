package com.codeminders.ardrone.controllers;

import java.io.IOException;
import java.util.BitSet;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;
import com.codeminders.hidapi.HIDManager;

public class MotioninJoyVirtualController  extends PS3Controller {
    

	private static final int VENDOR_ID          = 34952;
    private static final int PRODUCT_ID         = 776; 

    private static final int BUFSIZE            = 64;
    private static final int EXPECTED_BUFSIZE_3 = 11;

    private byte[]           buf                = new byte[BUFSIZE];
    
    boolean  first_passed = false;
    
    public MotioninJoyVirtualController() throws HIDDeviceNotFoundException, IOException {
        dev = HIDManager.getInstance().openById(VENDOR_ID, PRODUCT_ID, null);
        if (null != dev) {
        	dev.enableBlocking(); 
        } else {
        	throw new HIDDeviceNotFoundException("Device not found");
        }
    }
    
    public MotioninJoyVirtualController(HIDDeviceInfo hidDeviceInfo) throws IOException {
    	dev = hidDeviceInfo.open();
        if (null != dev) {
         	dev.enableBlocking();
 	    } else {
 	    	throw new HIDDeviceNotFoundException("Device not found");
 	    }
	}

	public static boolean isA(HIDDeviceInfo hidDeviceInfo)
    {
        return(hidDeviceInfo.getVendor_id() == VENDOR_ID && hidDeviceInfo.getProduct_id() == PRODUCT_ID);
    }
    
	@Override
	public PS3ControllerState read() throws IOException {
		int n = dev.read(buf);
		
        if(n != EXPECTED_BUFSIZE_3)
        {
            throw new IOException("Received packed with unexpected size " + n);
        }
       
        BitSet bs = new BitSet(16);
        
        for(int i = 0; i < 8; i++)
        {
            if((1 & (buf[0] >> i)) == 1)
                bs.set(i);
        }

        for(int i = 0; i < 8; i++)
        {
            if((1 & (buf[1] >> i)) == 1)
                bs.set(8 + i);
        }

        int i = 0;
        
        boolean triangle = bs.get(i++); 
        boolean circle = bs.get(i++); 
        boolean cross = bs.get(i++); 
        boolean square = bs.get(i++);  
        boolean L1 = bs.get(i++);  
        boolean R1 = bs.get(i++);  
        boolean L2 = bs.get(i++);  
        boolean R2 = bs.get(i++); 
        boolean select = bs.get(i++); 
        boolean leftJoystickPress = bs.get(i++);
        boolean rightJoystickPress = bs.get(i++);
        boolean start = bs.get(i++);
        boolean PS = bs.get(i++);


        int leftJoystickX = joystickCoordConv(buf[3]);
        int leftJoystickY = joystickCoordConv(buf[4]);
        int rightJoystickX = joystickCoordConv(buf[5]);
        int rightJoystickY = joystickCoordConv(buf[8]);

        // TODO: decode HAT switch
        int hatSwitchLeftRight = 0;
        int hatSwitchUpDown = 0;

        PS3ControllerState res = new PS3ControllerState(square, cross, circle, triangle, L1, R1, L2, R2, select, start,
                leftJoystickPress, rightJoystickPress, PS, hatSwitchLeftRight, hatSwitchUpDown, leftJoystickX,
                leftJoystickY, rightJoystickX, rightJoystickY);

        return res;
	}
	
	private int joystickCoordConv(byte b)
    {
        int v = b < 0 ? b + 256 : b;
        return(v - 128);
    }
}
