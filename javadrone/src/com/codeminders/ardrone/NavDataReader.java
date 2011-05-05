
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NavDataReader implements Runnable
{
    private static final int       BUFSIZE = 4096;

    private DatagramChannel        channel;
    private ARDrone                drone;
    private Selector               selector;
    private boolean                done;

    public NavDataReader(ARDrone drone, InetAddress drone_addr, int navdata_port)
            throws IOException
    {
        this.drone = drone;

        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(drone_addr, navdata_port));

        selector = Selector.open();
    }

    @Override
    public void run()
    {
        try
        {
            channel.register(selector, SelectionKey.OP_READ);

            ByteBuffer inbuf = ByteBuffer.allocate(BUFSIZE);
            done = false;
            while(!done)
            {
                selector.select();
                if(done)
                {
                    disconnect();
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
                        drone.navDataReceived(nd);
                    }
                }
            }
        } catch(IOException e)
        {
            drone.changeToErrorState(e);
        }

    }

    private void disconnect()
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
    }

    public void stop()
    {
        done = true;
        selector.wakeup();
    }

}
