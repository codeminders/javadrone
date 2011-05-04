
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;

public class NavDataReader implements Runnable
{
    private static final int       BUFSIZE = 4096;
    private DatagramSocket         navdata_socket;
    private BlockingQueue<NavData> navdata_queue;
    private ARDrone                drone;

    public NavDataReader(ARDrone drone, DatagramSocket navdata_socket, BlockingQueue<NavData> navdata_queue)
    {
        this.drone = drone;
        this.navdata_socket = navdata_socket;
        this.navdata_queue = navdata_queue;
    }

    @Override
    public void run()
    {
        byte[] inbuf = new byte[BUFSIZE];
        DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);
        while(true)
        {
            try
            {
                navdata_socket.receive(packet);
            } catch(IOException e)
            {
                drone.changeToErrorState(e);
                break;
            }
            int numBytesReceived = packet.getLength();
            NavData nd = NavData.createFromData(inbuf, numBytesReceived);
            navdata_queue.add(nd);
        }
    }

    public void stop()
    {
        // TODO Auto-generated method stub

    }

}
