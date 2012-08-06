
package com.codeminders.ardrone.tools;

import java.io.IOException;

import com.codeminders.ardrone.controllers.*;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public class ControllerTest
{
    private static final long READ_UPDATE_DELAY_MS = 30L;

    static
    {
        System.loadLibrary("hidapi-jni-64");
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            PS3Controller c = findController();
            if(c == null)
            {
                System.err.println("Controller not found");
                System.exit(1);
            } else
            {
                System.err.println("Controller found "+c);
            }
            try
            {
                while(true)
                {

                    PS3ControllerState x = c.read();
                    System.err.println(x);
                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    } catch(InterruptedException e)
                    {
                        // Ignore
                    }
                }
            } finally
            {
                c.close();
            }
        } catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    private static PS3Controller findController() throws IOException
    {
        HIDDeviceInfo[] devs = HIDManager.listDevices();
        for(int i = 0; i < devs.length; i++)
        {
        	if (MotioninJoyVirtualController.isA(devs[i]))
        	    return new MotioninJoyVirtualController(devs[i]);
            if(AfterGlowController.isA(devs[i]))
                return new AfterGlowController(devs[i]);
            if(SonyPS3Controller.isA(devs[i]))
                return new SonyPS3Controller(devs[i]);
        }
        return null;
    }

}
