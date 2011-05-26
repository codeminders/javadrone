
package com.codeminders.ardroneui;

import java.util.logging.*;

import com.codeminders.ardrone.ARDrone;

public class CommandLineTest
{

    private static final long CONNECT_TIMEOUT = 1000;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        setupLog();

        ARDrone drone;
        try
        {
            drone = new ARDrone();
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            drone.clearEmergencySignal();
            drone.trim();
            Thread.sleep(1000);
            drone.takeOff();
            Thread.sleep(1000);
            drone.land();
            Thread.sleep(2000);
            drone.disconnect();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

    private static void setupLog()
    {
        Logger topLogger = java.util.logging.Logger.getLogger("");
        Handler consoleHandler = null;
        for(Handler handler : topLogger.getHandlers())
        {
            if(handler instanceof ConsoleHandler)
            {
                consoleHandler = handler;
                break;
            }
        }

        if(consoleHandler == null)
        {
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        topLogger.setLevel(Level.FINE);
        consoleHandler.setLevel(java.util.logging.Level.FINE);
    }

}
