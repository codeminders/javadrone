
package com.codeminders.ardrone.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.widget.TextView;


public class SonyPS3Controller extends PS3Controller
{
    private static final int VENDOR_ID          = 1356;
    private static final int PRODUCT_ID         = 616;

    int bufferSize                              = 0;
    ByteBuffer buffer;

    UsbDeviceConnection readDataConnection = null;
    UsbEndpoint usbEndpointRead = null;
    UsbRequest request;
    UsbRequest requestQueued;
    
    public static boolean isA(UsbDevice dev)
    {
        return(dev.getVendorId() == VENDOR_ID && dev.getProductId() == PRODUCT_ID);
    }


    public SonyPS3Controller(UsbDevice dev, UsbManager manager) throws IOException
    {
        this.dev = dev;
        readDataConnection = manager.openDevice(dev); 
        if (null == readDataConnection) {
            throw new IOException("Failed to open connection to USB device");
        }
        UsbInterface usbInterfaceRead = dev.getInterface(0);
        readDataConnection.claimInterface(usbInterfaceRead, true);
        
        for(int i = 0; i < usbInterfaceRead.getEndpointCount(); i++) {
            if (usbInterfaceRead.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                usbEndpointRead = usbInterfaceRead.getEndpoint(i);
                break;
            }
        }
       
        if (null == usbEndpointRead) {
            throw new IOException("Failed to find usb read endpoint");
        }
        
        bufferSize = usbEndpointRead.getMaxPacketSize();
        buffer = ByteBuffer.allocate(bufferSize + 1);
        request = new UsbRequest();     
        request.initialize(readDataConnection, usbEndpointRead);
    }

    private int joystickCoordConv(byte b)
    {
        int v = b < 0 ? b + 256 : b;
        return(v - 128);
    }

    @Override
    public PS3ControllerState read() throws IOException
    {

        PS3ControllerState res = null;
        buffer.clear();
        request.queue(buffer, bufferSize); 
        requestQueued = readDataConnection.requestWait();  
        if (request.equals(requestQueued))
        {
            byte[] buf = new byte[bufferSize + 1];
            buffer.get(buf, 0, bufferSize);
            
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

            int hatSwitchLeftRight = 0;
            int hatSwitchUpDown = 0;

            res = new PS3ControllerState(square, cross, circle, triangle, L1, R1, L2, R2, select, start,
                    leftJoystickPress, rightJoystickPress, PS, hatSwitchLeftRight, hatSwitchUpDown, leftJoystickX,
                    leftJoystickY, rightJoystickX, rightJoystickY);
        }
        

       
        return res;
    }


    @Override
    public void close() throws IOException {
       if (null != readDataConnection) {
           readDataConnection.close();
       }
        
    }

}
