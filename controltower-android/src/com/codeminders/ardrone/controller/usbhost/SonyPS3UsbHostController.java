package com.codeminders.ardrone.controller.usbhost;

import java.io.IOException;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.codeminders.ardrone.controllers.decoders.ControllerStateDecoder;
import com.codeminders.ardrone.controllers.decoders.SonyPS3ControllerStateDecoder;

public class SonyPS3UsbHostController extends UsbHostController {
    
    private static final int VENDOR_ID          = 1356;
    private static final int PRODUCT_ID         = 616;
    
    static ControllerStateDecoder decoder = new SonyPS3ControllerStateDecoder();
    
    public SonyPS3UsbHostController(UsbDevice dev, UsbManager manager) throws IOException {
        super(dev, manager, decoder);
    }

    @Override
    public String getName() {
        return "SonyPS3Controller over usb host";
    }
  
    public static boolean isA(UsbDevice dev)
    {
        return(dev.getVendorId() == VENDOR_ID && dev.getProductId() == PRODUCT_ID);
    }
}
