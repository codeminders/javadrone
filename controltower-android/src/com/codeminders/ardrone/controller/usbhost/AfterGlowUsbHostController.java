package com.codeminders.ardrone.controller.usbhost;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.codeminders.ardrone.controllers.decoders.AfterGlowControllerDecoder;
import com.codeminders.ardrone.controllers.decoders.ControllerStateDecoder;

@SuppressLint("NewApi")
public class AfterGlowUsbHostController extends UsbHostController {
    
    private static final int VENDOR_ID        = 3695;
    private static final int PRODUCT_ID       = 25346;
    
    static ControllerStateDecoder decoder = new AfterGlowControllerDecoder();
    
    public AfterGlowUsbHostController(UsbDevice dev, UsbManager manager) throws IOException {
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
