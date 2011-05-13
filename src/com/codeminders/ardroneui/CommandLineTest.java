package com.codeminders.ardroneui;

import com.codeminders.ardrone.ARDrone;

public class CommandLineTest
{

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
