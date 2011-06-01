
package com.codeminders.ardrone.examples;

import org.apache.log4j.Logger;

import com.codeminders.ardrone.ARDrone;

public class RotationSnapshotTaker
{
    private static final int  TAKEOFF_TIMEOUT = 5000;
    private static final long CONNECT_TIMEOUT = 3000;

    private static Logger     log             = Logger.getLogger(RotationSnapshotTaker.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ARDrone drone;
        try
        {
            drone = new ARDrone();
            try
            {
                drone.connect();
                drone.clearEmergencySignal();
                drone.waitForReady(CONNECT_TIMEOUT);
                drone.trim();
                log.info("Taking off");
                drone.takeOff();
                Thread.sleep(TAKEOFF_TIMEOUT);

                log.info("Flying");
                long mstart = System.currentTimeMillis();
                while((mstart + 3000) > System.currentTimeMillis())
                    drone.move(0f, 0f, 0f, 0f);
                // Thread.sleep(5000);

                log.info("Landing");
                drone.land();
            } finally
            {
                log.info("Disconnecting");
                drone.disconnect();
            }
        } catch(Exception e1)
        {
            e1.printStackTrace();
        }
        log.info("done");
    }

}
