
package com.codeminders.ardrone.controllers.hid;

import java.io.IOException;

import com.codeminders.ardrone.controllers.ControllerData;
import com.codeminders.ardrone.controllers.decoders.SonyPS3ControllerStateDecoder;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;

public class HIDAPISonyPS3Controller extends HIDAPIController
{
    private static final int VENDOR_ID          = 1356;
    private static final int PRODUCT_ID         = 616;

    private static final int BUFSIZE            = 64;
    private static final int EXPECTED_BUFSIZE   = 32;
    private static final int EXPECTED_BUFSIZE_2 = 49;

    static final SonyPS3ControllerStateDecoder decoder = new SonyPS3ControllerStateDecoder();
    
    public HIDAPISonyPS3Controller() throws HIDDeviceNotFoundException, IOException
    {
        super(VENDOR_ID, PRODUCT_ID, decoder, BUFSIZE);
    }

    public HIDAPISonyPS3Controller(HIDDeviceInfo hidDeviceInfo) throws IOException
    {
        super(decoder, BUFSIZE, hidDeviceInfo);
    }

    @Override
    public String getName() {
        return "HID API connected SonyPS3Controller";
    }

    @Override
    public boolean isValid(ControllerData data) throws IOException {
        if(data.getActualBufferDataLength() != EXPECTED_BUFSIZE && data.getActualBufferDataLength() != EXPECTED_BUFSIZE_2)
        {
            throw new IOException("Received packed with unexpected size " + data.getActualBufferDataLength());
        }
        return true;
    }
    
    public static boolean isA(HIDDeviceInfo hidDeviceInfo) {
        return VENDOR_ID == hidDeviceInfo.getVendor_id() &&  PRODUCT_ID == hidDeviceInfo.getProduct_id();
    }
}
