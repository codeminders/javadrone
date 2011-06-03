
package com.codeminders.ardrone.examples;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.codeminders.ardrone.*;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.NavData.FlyingState;

public class RotationSnapshotTaker implements DroneVideoListener, NavDataListener
{
    private static final int  TAKEOFF_TIMEOUT = 5000;
    private static final long CONNECT_TIMEOUT = 3000;

    private static Logger     log             = Logger.getLogger(RotationSnapshotTaker.class.getName());

    private ARDrone           drone;
    private long              mstart;
    boolean                   taking          = false;
    float                     initial_angle   = Float.NaN;

    public RotationSnapshotTaker() throws UnknownHostException
    {
        drone = new ARDrone();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            RotationSnapshotTaker self = new RotationSnapshotTaker();
            self.run();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public void run() throws IOException, InterruptedException
    {
        try
        {
            drone.connect();
            drone.clearEmergencySignal();
            drone.waitForReady(CONNECT_TIMEOUT);

            drone.trim();
            drone.selectVideoChannel(VideoChannel.HORIZONTAL_ONLY);

            log.info("Taking off");
            drone.takeOff();
            Thread.sleep(TAKEOFF_TIMEOUT);

            log.info("Rotating and recording");
            mstart = System.currentTimeMillis();
            drone.addImageListener(this);
            drone.addNavDataListener(this);

            taking = true;
            Thread.sleep(5000);
            taking = false;

            log.info("Landing");
            drone.land();
        } finally
        {
            log.info("Disconnecting");
            drone.disconnect();
        }
        log.info("done");
    }

    @Override
    public void frameReceived(BufferedImage image)
    {
    }

    @Override
    public void navDataReceived(NavData nd)
    {
        // Wait for full take off
        if(nd.getFlyingState() != FlyingState.FLYING)
            return;

        // Initial angle
        if(Float.isNaN(initial_angle))
            initial_angle = nd.getYaw();

        try
        {
            drone.move(0f, 0f, 0f, 0.1f);
        } catch(IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println("Yaw = " + nd.getYaw());
    }

}
