
package com.codeminders.ardrone.tools;

import java.io.IOException;

import com.codeminders.ardrone.controllers.*;
import com.codeminders.ardrone.controllers.hid.manager.HIDControllerFinder;
import com.codeminders.hidapi.ClassPathLibraryLoader;

public class ControllerTest
{
    private static final long READ_UPDATE_DELAY_MS = 30L;

    static
    {
        ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            Controller c = findController();
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

                    GameControllerState x = c.read();
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

    private static Controller findController() throws IOException
    {
        return HIDControllerFinder.findController();
    }

}
