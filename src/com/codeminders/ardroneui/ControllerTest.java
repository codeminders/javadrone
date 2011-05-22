
package com.codeminders.ardroneui;

import java.io.IOException;

import com.codeminders.ardroneui.controllers.*;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public class ControllerTest
{
    private static final long READ_UPDATE_DELAY_MS = 30L;

    static
    {
        System.loadLibrary("hidapi-jni");
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
            if(AfterGlowController.isA(devs[i]))
                return new AfterGlowController(devs[i]);
            if(SonyPS3Controller.isA(devs[i]))
                return new SonyPS3Controller(devs[i]);
        }
        return null;
    }

}
