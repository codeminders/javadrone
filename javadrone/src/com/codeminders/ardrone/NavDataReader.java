
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class NavDataReader implements Runnable
{
    private static final int       BUFSIZE = 4096;

    private DatagramChannel        channel;
    private BlockingQueue<NavData> navdata_queue;
    private ARDrone                drone;
    private Selector               selector;
    private boolean                done;

    public NavDataReader(ARDrone drone, InetAddress drone_addr, int navdata_port, BlockingQueue<NavData> navdata_queue)
            throws IOException
    {
        this.drone = drone;
        this.navdata_queue = navdata_queue;

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(drone_addr, navdata_port));
    }

    @Override
    public void run()
    {
        try
        {
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);

            ByteBuffer inbuf = ByteBuffer.allocate(BUFSIZE);
            done = false;
            while(!done)
            {
                selector.select();
                if(done)
                {
                    try
                    {
                        selector.close();
                    } catch(IOException iox)
                    {
                        // Ignore
                    }

                    try
                    {
                        channel.disconnect();
                    } catch(IOException iox)
                    {
                        // Ignore
                    }

                    break;
                }
                Set readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();
                while(iterator.hasNext())
                {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if(key.isReadable())
                    {
                        inbuf.clear();
                        channel.read(inbuf);

                        NavData nd = NavData.createFromData(inbuf.array());
                        navdata_queue.add(nd);
                    }
                }
            }
        } catch(IOException e)
        {
            drone.changeToErrorState(e);
        }

    }

    public void stop()
    {
        done = true;
        selector.wakeup();
    }

}
