
package com.codeminders.ardroneui;

import java.util.logging.*;

import com.codeminders.ardrone.ARDrone;

public class RotationSnapshotTaker
{
    private static final int TAKEOFF_TIMEOUT = 3000;

    private static final long CONNECT_TIMEOUT = 1000;

    private static Logger log = Logger.getLogger(RotationSnapshotTaker.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        setupLog();

        log.entering("RotationSnapshotTaker", "main");
        ARDrone drone;
        try
        {
            drone = new ARDrone();
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            drone.clearEmergencySignal();
            drone.trim();
            drone.takeOff();
            Thread.sleep(TAKEOFF_TIMEOUT);
            long mstart = System.currentTimeMillis();

            //while((mstart + 3000) > System.currentTimeMillis())
            //    drone.move(0f, 0f, 0f, 0f);
            // Thread.sleep(5000);

            drone.land();
            drone.disconnect();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
        log.exiting("RotationSnapshotTaker", "main");
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
