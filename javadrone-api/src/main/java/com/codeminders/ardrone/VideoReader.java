
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import com.codeminders.ardrone.video.*;

public class VideoReader extends DataReader {
    /**
     * Image data buffer. It should be big enough to hold single full frame
     * (encoded).
     */
    private static final int BUFSIZE = 100 * 1024;
  

    public VideoReader(ARDrone drone, InetAddress drone_addr, int video_port) throws IOException
    {
        super(drone, drone_addr, video_port, BUFSIZE);
    }

    @Override
    void handleReceivedMessageKey(SelectionKey key, ByteBuffer inbuf)
            throws Exception {
        
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
            if(len > 0)
            {
                inbuf.flip();
                final BufferedVideoImage vi = new BufferedVideoImage();
                vi.addImageStream(inbuf);
                drone.videoFrameReceived(0, 0, vi.getWidth(), vi.getHeight(), vi.getJavaPixelData(), 0, vi.getWidth());
            }
        }
        
    }

}
