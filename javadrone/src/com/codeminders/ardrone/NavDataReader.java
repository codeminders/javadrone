
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;

public class NavDataReader implements Runnable
{
    private static final int       BUFSIZE = 4096;
    private DatagramChannel        channel;
    private BlockingQueue<NavData> navdata_queue;
    private ARDrone                drone;

    public NavDataReader(ARDrone drone, int navdata_port, BlockingQueue<NavData> navdata_queue) throws IOException
    {
        this.drone = drone;
        this.navdata_queue = navdata_queue;

        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(navdata_port));
    }

    @Override
    public void run()
    {
        ByteBuffer inbuf = ByteBuffer.allocate(BUFSIZE);
        while(true)
        {
            inbuf.clear();
            try
            {
                channel.receive(inbuf);
            } catch(IOException e)
            {
                drone.changeToErrorState(e);
                break;
            }
            NavData nd = NavData.createFromData(inbuf.array());
            navdata_queue.add(nd);
        }
    }

    public void stop()
    {
        // TODO Auto-generated method stub

    }

}
