
package com.codeminders.ardrone.examples;

import com.codeminders.ardrone.ARDrone;

public class TakeOffAndLand
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
            // Create ARDrone object,
            // connect to drone and initialize it.
            drone = new ARDrone();
            drone.connect();
            drone.clearEmergencySignal();

            // Wait until drone is ready
            drone.waitForReady(CONNECT_TIMEOUT);

            // do TRIM operation
            drone.trim();

            // Take off
            System.err.println("Taking off");
            drone.takeOff();

            // Fly a little :)
            Thread.sleep(3000);

            // Land
            System.err.println("Landing");
            drone.land();
            
            // Disconnect from the done
            drone.disconnect();

        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
}
