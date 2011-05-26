
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
    private static final int BUFSIZE = 4096;

    private DatagramChannel  channel;
    private ARDrone          drone;
    private Selector         selector;
    private boolean          done;

    public NavDataReader(ARDrone drone, InetAddress drone_addr, int navdata_port) throws IOException
    {
        this.drone = drone;
        
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(navdata_port));
        channel.connect(new InetSocketAddress(drone_addr, navdata_port));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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

    @Override
    public void run()
    {
        try
        {
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
                    if(key.isWritable())
                    {
                        byte[] trigger_bytes = {0x01, 0x00, 0x00, 0x00};
                        ByteBuffer trigger_buf = ByteBuffer.allocate(trigger_bytes.length);
                        trigger_buf.put(trigger_bytes);
                        trigger_buf.flip();
                        channel.write(trigger_buf);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if(key.isReadable())
                    {
                        inbuf.clear();
                        int len = channel.read(inbuf);
                        byte[] packet = new byte[len];
                        inbuf.flip();
                        inbuf.get(packet, 0, len);

                        NavData nd = NavData.createFromData(packet);

                        drone.navDataReceived(nd);
                    }
                }
            }
        } catch(Exception e)
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
