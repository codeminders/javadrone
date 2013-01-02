
package com.codeminders.ardrone.data.reader;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.data.decoder.DataDecoder;

public class UDPDataReaderAndDecoder extends UDPDataReader {

    DataDecoder dataDecoder;
    
    public UDPDataReaderAndDecoder(ARDrone drone, InetAddress drone_addr, int navdata_port, int bufferSize, int reconnect_timeout, DataDecoder dataDecoder) throws IOException
    {
        super(drone, drone_addr, navdata_port, bufferSize, reconnect_timeout);
        this.dataDecoder = dataDecoder;
    }

    @Override
    public void handleData(ByteBuffer inbuf, int len)
    {
        dataDecoder.decodeData(inbuf, len);
    }
    
    public void finish()
    {   
        super.finish();
        dataDecoder.finish();
    }

}
