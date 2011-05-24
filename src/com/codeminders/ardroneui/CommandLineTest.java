package com.codeminders.ardroneui;

import com.codeminders.ardrone.ARDrone;

public class CommandLineTest
{

    private static final long CONNECT_TIMEOUT = 1000;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ARDrone drone;
        try
        {
            drone = new ARDrone();
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            drone.trim();
            Thread.sleep(1000);
            drone.takeOff();
            Thread.sleep(5000);
            drone.land();
            Thread.sleep(2000);
            drone.disconnect();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

}
