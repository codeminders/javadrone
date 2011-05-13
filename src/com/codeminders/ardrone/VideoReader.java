
package com.codeminders.ardrone;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

import com.codeminders.ardrone.video.ImageDecoder;

public class VideoReader implements Runnable
{
    /**
     * Image data buffer. It should be big enough to hold single full frame
     * (encoded).
     */
    private static final int BUFSIZE = 512 * 1024 * 1024;

    private enum State
    {
        SEEKING, READING
    };

    State                   state;
    private DatagramChannel channel;
    private ARDrone         drone;
    private Selector        selector;
    private boolean         done;

    public VideoReader(ARDrone drone, InetAddress drone_addr, int video_port) throws IOException
    {
        this.drone = drone;

        state = State.SEEKING;
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(video_port));
        channel.connect(new InetSocketAddress(drone_addr, video_port));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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
                        byte[] trigger_bytes = { 0x01, 0x00, 0x00, 0x00 };
                        ByteBuffer trigger_buf = ByteBuffer.allocate(trigger_bytes.length);
                        trigger_buf.put(trigger_bytes);
                        trigger_buf.flip();
                        channel.write(trigger_buf);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if(key.isReadable())
                    {

                        int pos = inbuf.position();
                        int len = channel.read(inbuf);

                        if(state == State.SEEKING)
                        {
                            int e = findPSC(inbuf, pos, len);
                            if(e != -1)
                            {
                                // Found!
                                inbuf.position(e);
                                inbuf.compact();
                                state = State.READING;
                            }
                        } else if(state == State.READING)
                        {
                            int s = findEOS(inbuf, pos, len);
                            if(s != -1)
                            {
                                // Found!
                                inbuf.limit(s);
                                inbuf.position(0);
                                byte[] packet = new byte[Math.min(inbuf.limit(), s)];
                                inbuf.get(packet, 0, len);
                                BufferedImage image = ImageDecoder.readUINT_RGBImage(packet);
                                drone.videoFrameReceived(image);
                                inbuf.compact();
                                state = State.SEEKING;
                            }
                        }
                    }
                }
            }
        } catch(Exception e)
        {
            drone.changeToErrorState(e);
        }

    }

    /**
     * Finds EOS marker in the buffer starting from given position.
     * EOS marker is 22 binary bits: 0000 0000 0000 0000 1 11111
     * 
     * @param inbuf
     * @param pos
     * @param len
     * @return position of the marker on -1 if not found
     */
    private int findEOS(ByteBuffer inbuf, int pos, int len)
    {
        if(len <= 0)
            return -1;

        int f = Math.max(0, pos - 2);
        int t = pos + len;
        if((f - t) < 3)
            return -1;
        
        for(int i = f; i < (pos + len - 2); i++)
        {
            if(inbuf.get(i) == 0 && inbuf.get(i + 1) == 0 && (inbuf.get(i) & 3) == -4)
                return i;
        }
        return -1;
    }

    /**
     * Finds PSC marker in the buffer starting from given position.
     * PSC marker is 22 binary bits: 0000 0000 0000 0000 1 00000.
     * 
     * @param inbuf
     * @param pos
     * @param len
     * @return position of the marker on -1 if not found
     */
    private int findPSC(ByteBuffer inbuf, int pos, int len)
    {
        if(len <= 0)
            return -1;

        int f = Math.max(0, pos - 2);
        int t = pos + len;
        if((f - t) < 3)
            return -1;
        
        for(int i = f; i < (pos + len - 2); i++)
        {
            if(inbuf.get(i) == 0 && inbuf.get(i + 1) == 0 && (inbuf.get(i) & 3) == -128)
                return i;
        }
        return -1;
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
