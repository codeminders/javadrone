
package com.codeminders.ardroneui.controllers;

import java.io.IOException;

import com.codeminders.hidapi.*;

public class SonyPS3Controller extends PS3Controller
{
    private static final int VENDOR_ID        = 1356;
    private static final int PRODUCT_ID       = 616;

    private static final int BUFSIZE          = 64;
    private static final int EXPECTED_BUFSIZE = 49;

    private byte[] buf = new byte[BUFSIZE];

    public static boolean isA(HIDDeviceInfo hidDeviceInfo)
    {
        return(hidDeviceInfo.getVendor_id() == VENDOR_ID && hidDeviceInfo.getProduct_id() == PRODUCT_ID);
    }

    public SonyPS3Controller() throws HIDDeviceNotFoundException, IOException
    {
        dev = HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
        dev.enableBlocking();
    }

    public SonyPS3Controller(HIDDeviceInfo hidDeviceInfo) throws IOException
    {
        dev = hidDeviceInfo.open();
        dev.enableBlocking();
    }

    @Override
    public PS3ControllerState read() throws IOException
    {
        int n = dev.read(buf);
        if(n != EXPECTED_BUFSIZE)
        {
            throw new IOException("Received packed with unexpected size " + n);
        }

        // TODO Auto-generated method stub
        return null;
    }

}
