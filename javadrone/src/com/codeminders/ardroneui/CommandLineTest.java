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
            drone.disconnect();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

}
