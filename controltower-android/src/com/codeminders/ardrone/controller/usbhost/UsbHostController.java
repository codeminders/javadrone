package com.codeminders.ardrone.controller.usbhost;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import com.codeminders.ardrone.controllers.Controller;
import com.codeminders.ardrone.controllers.ControllerData;
import com.codeminders.ardrone.controllers.GameControllerState;
import com.codeminders.ardrone.controllers.decoders.ControllerStateDecoder;

public abstract class UsbHostController extends Controller {
    
    int bufferSize = 0;
    ByteBuffer buffer;

    UsbDeviceConnection readDataConnection = null;
    UsbEndpoint usbEndpointRead = null;
    UsbRequest request;
    UsbRequest requestQueued;
    UsbDevice dev;
    
    ControllerStateDecoder decodder;
    
    @Override
    public GameControllerState read() throws IOException
    {

        GameControllerState res = null;
        buffer.clear();
        request.queue(buffer, bufferSize); 
        requestQueued = readDataConnection.requestWait();  
        if (request.equals(requestQueued))
        {
            byte[] buf = new byte[bufferSize + 1];
            buffer.get(buf, 0, bufferSize);
            
            return decodder.decodeState(new ControllerData(buf, bufferSize + 1));
        }
        
        return res;
    }
    
    public UsbHostController(UsbDevice dev, UsbManager manager, ControllerStateDecoder decodder) throws IOException
    {
        this.decodder = decodder;
        
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
    
    
    @Override
    public void close() throws IOException {
       if (null != readDataConnection) {
           readDataConnection.close();
       }
        
    }
    
    @Override
    public String getManufacturerString() {
        return "read api is not supported";
    }

    @Override
    public String getProductString() {
        return "read api is not supported";
    }
    

}
