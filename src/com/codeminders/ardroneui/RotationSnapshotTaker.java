
package com.codeminders.ardroneui;

import java.util.logging.*;

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
        setupLog();

        ARDrone drone;
        try
        {
            drone = new ARDrone();
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            drone.clearEmergencySignal();
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
            log.info("Disconnecting");
            drone.disconnect();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
        log.info("done");
    }

    private static void setupLog()
    {
        Logger topLogger = java.util.logging.Logger.getLogger("");
        Handler consoleHandler = null;
        for(Handler handler : topLogger.getHandlers())
            if(handler instanceof ConsoleHandler)
            {
                consoleHandler = handler;
                break;
            }

        if(consoleHandler == null)
        {
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        topLogger.setLevel(Level.ALL);
        consoleHandler.setLevel(java.util.logging.Level.ALL);
    }

}
