package com.codeminders.ardrone.controller.usbhost;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.codeminders.ardrone.controllers.Controller;
import com.codeminders.ardrone.controllers.ControllerData;
import com.codeminders.ardrone.controllers.GameControllerState;
import com.codeminders.ardrone.controllers.decoders.ControllerStateDecoder;

@SuppressLint("NewApi")
public abstract class UsbHostController extends Controller {
    
    int bufferSize = 0;
    byte[] buf;

    UsbDeviceConnection readDataConnection = null;
    UsbEndpoint usbEndpointRead = null;
    UsbDevice dev;
    
    ControllerStateDecoder decodder;
    
    @Override
    public GameControllerState read() throws IOException
    {
        int i =  readDataConnection.bulkTransfer(usbEndpointRead, buf, bufferSize, 5);
        if (i > 0) {
            return decodder.decodeState(new ControllerData(buf, i + 1));
        } else  {
            return null;
        }
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
        buf = new byte[bufferSize + 1];
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
    
    private static final byte[] HEX_CHAR = new byte[]
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static final String dumpBytes( byte[] buffer )
    {
        if ( buffer == null )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for ( int i = 0; i < buffer.length; i++ )
        {
            sb.append( "0x" ).append( ( char ) ( HEX_CHAR[( buffer[i] & 0x00F0 ) >> 4] ) ).append(
                ( char ) ( HEX_CHAR[buffer[i] & 0x000F] ) ).append( " " );
        }

        return sb.toString();
    }
}
