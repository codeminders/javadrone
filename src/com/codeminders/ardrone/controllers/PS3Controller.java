
package com.codeminders.ardrone.controllers;

import java.io.IOException;

import com.codeminders.hidapi.HIDDevice;

/**
 * Base abstract class for supported PS3-compatible USB controllers
 * 
 * @author lord
 * 
 */
public abstract class PS3Controller
{
    protected HIDDevice dev;

    protected static void printDelta(byte[] prev, int prev_size, byte[] cur, int cur_size)
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

    public void close() throws IOException
    {
        if(dev != null)
            dev.close();
    }

    public abstract PS3ControllerState read() throws IOException;

}