package com.codeminders.ardrone.controllers.hid;

import java.io.IOException;

import com.codeminders.ardrone.controllers.ControllerData;
import com.codeminders.ardrone.controllers.decoders.AfterGlowControllerDecoder;
import com.codeminders.ardrone.controllers.decoders.ControllerStateDecoder;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;

public class HIDAPIAfterGlowController extends HIDAPIController {

    private static final int VENDOR_ID        = 3695;
    private static final int PRODUCT_ID       = 25346;

    private static final int BUFSIZE          = 32;
    private static final int EXPECTED_BUFSIZE = 27;
    
    private static ControllerStateDecoder decoder = new AfterGlowControllerDecoder();

    
    public HIDAPIAfterGlowController() throws HIDDeviceNotFoundException, IOException
    {
        super(VENDOR_ID, PRODUCT_ID, decoder, BUFSIZE);
    }

    public HIDAPIAfterGlowController(HIDDeviceInfo hidDeviceInfo) throws IOException
    {
        super(decoder, BUFSIZE, hidDeviceInfo);
    }

    @Override
    public String getName() {
        return "HID API connected AfterGlowController";
    }

    @Override
    public boolean isValid(ControllerData data) throws IOException {
        if(data.getActualBufferDataLength() != EXPECTED_BUFSIZE)
        {
            throw new IOException("Received packed with unexpected size " + data.getActualBufferDataLength());
        }
        return true;
    }

    public static boolean isA(HIDDeviceInfo hidDeviceInfo) {
        return VENDOR_ID == hidDeviceInfo.getVendor_id() &&  PRODUCT_ID == hidDeviceInfo.getProduct_id();
    }

}
