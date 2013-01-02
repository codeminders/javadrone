
package com.codeminders.ardrone.data.reader;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.data.decoder.DataDecoder;

public class VideoReader extends UDPDataReader {
    /**
     * Image data buffer. It should be big enough to hold single full frame
     * (encoded).
     */
    private static final int BUFSIZE = 100 * 1024;
        
    DataDecoder dataDecoder;
    
    public VideoReader(ARDrone drone, InetAddress drone_addr, int video_port, int reconnect_timeout, DataDecoder dataDecoder) throws IOException
    {
        super(drone, drone_addr, video_port, BUFSIZE, reconnect_timeout);
        this.dataDecoder = dataDecoder;
    }

    @Override
    public void handleData(ByteBuffer inbuf, int len)
            throws Exception {
        if (len > 0) {        
            dataDecoder.decodeData(inbuf, len);
        }
    }
    
    public void finish()
    {
        super.finish();
        dataDecoder.finish();
    }

}
