
package com.codeminders.ardroneui.controllers;

import java.io.IOException;

import com.codeminders.hidapi.*;

/**
 * "Afterglow" controller for PS3
 * 
 * @author lord
 * 
 */
public class AfterGlowController
{
    private static final int VENDOR_ID        = 3695;
    private static final int PRODUCT_ID       = 25346;
    private HIDDevice        dev;

    private static final int BUFSIZE          = 32;
    private static final int EXPECTED_BUFSIZE = 27;
    private byte[]           buf              = new byte[BUFSIZE];

    public AfterGlowController() throws HIDDeviceNotFoundException, IOException
    {
        dev = HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
        dev.enableBlocking();

    }

    public synchronized PS3ControllerState read() throws IOException
    {
        int n = dev.read(buf);
        if(n != EXPECTED_BUFSIZE)
        {
            throw new IOException("Received packed with unexpected size " + n);
        }
        return new PS3ControllerState(buf); 
    }

    public void close() throws IOException
    {
        dev.close();
    }

    @SuppressWarnings("unused")
    private static void printDelta(byte[] prev, int prev_size, byte[] cur, int cur_size)
    {
        if(prev_size != cur_size)
        {
            System.err.println("Packet size is different. Prev: " + prev_size + " New: " + cur_size);
            return;
        }

        for(int i = 0; i < prev_size; i++)
        {
            if(prev[i] != cur[i])
            {
                System.err.println("Index: " + i + " Prev value: " + Integer.toHexString((int) prev[i])
                        + " New value: " + Integer.toHexString((int) cur[i]));
            }
        }
    }
}
