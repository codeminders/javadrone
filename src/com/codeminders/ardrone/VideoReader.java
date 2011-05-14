
package com.codeminders.ardrone;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

import com.codeminders.ardrone.video.*;

public class VideoReader implements Runnable
{
    /**
     * Image data buffer. It should be big enough to hold single full frame
     * (encoded).
     */
    private static final int BUFSIZE = 4096;

    private DatagramChannel  channel;
    private ARDrone          drone;
    private Selector         selector;
    private boolean          done;

    public VideoReader(ARDrone drone, InetAddress drone_addr, int video_port) throws IOException
    {
        this.drone = drone;

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
                VideoImage vi = new VideoImage();
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
                        inbuf.clear();
                        int len = channel.read(inbuf);
                        byte[] packet = new byte[len];
                        inbuf.flip();
                        inbuf.get(packet, 0, len);
                        if(vi.AddImageStream(packet))
                        {
                            BufferedImage bi = imageFromVideoImage(vi);
                            drone.videoFrameReceived(bi);
                        }

                    }
                }
            }
        } catch(Exception e)
        {
            drone.changeToErrorState(e);
        }

    }

    private BufferedImage imageFromVideoImage(VideoImage vi)
    {
        uint[] outData = vi.getPixelData();

        byte[] processedData = new byte[outData.length * 3];
        for(int i = 0; i < outData.length; i++)
        {
            int i2 = i * 3;
            uint dataI = outData[i];
            byte[] elt = dataI.getBytes();
            processedData[i2] = elt[2];
            processedData[i2 + 1] = elt[1];
            processedData[i2 + 2] = elt[0];
        }

        int[] pixelData = new int[processedData.length / 3];
        int raw, pixel = 0, j = 0;
        for(int i = 0; i < pixelData.length; i++)
        {
            pixel = 0;
            raw = processedData[j++] & 0xFF;
            pixel |= (raw << 16);
            raw = processedData[j++] & 0xFF;
            pixel |= (raw << 8);
            raw = processedData[j++] & 0xFF;
            pixel |= (raw << 0);
            pixelData[i] = pixel;
        }
        BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, 320, 240, pixelData, 0, 320);
        return image;
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
