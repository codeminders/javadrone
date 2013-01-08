
package com.codeminders.ardrone.data.reader;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.data.decoder.DataDecoder;
import com.codeminders.ardrone.data.logger.ChannelDataLogger;
import com.codeminders.ardrone.data.logger.ChannelDataChunk;

public class UDPDataReaderAndDecoder extends UDPDataReader {

    DataDecoder dataDecoder;
    private ChannelDataLogger logger;
    
    long lastDataTime = 0l;
    
    public UDPDataReaderAndDecoder(ARDrone drone, InetAddress drone_addr, int navdata_port, int bufferSize, int reconnect_timeout, DataDecoder dataDecoder, ChannelDataLogger logger) throws IOException
    {
        super(drone, drone_addr, navdata_port, bufferSize, reconnect_timeout);
        this.dataDecoder = dataDecoder;
        this.logger = logger;
    }

    @Override
    public void handleData(ByteBuffer inbuf, int len)
    {
        if (null != logger) {
            byte[] data = inbuf.array();
            logger.log(new ChannelDataChunk(data, lastDataTime));
            lastDataTime = System.currentTimeMillis();
            
            inbuf.clear();
            inbuf.put(data);
            inbuf.flip();
        }
        
        dataDecoder.decodeData(inbuf, len);
    }
    
    public void finish()
    {   
        super.finish();
        dataDecoder.finish();
    }

}
