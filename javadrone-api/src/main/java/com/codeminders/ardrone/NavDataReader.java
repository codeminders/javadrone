
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class NavDataReader extends DataReader {
    
    private static final int BUFSIZE = 4096;
    
    NavDataProcessor nsProcessor;
    
    public NavDataReader(ARDrone drone, InetAddress drone_addr, int navdata_port, int reconnect_timeout) throws IOException
    {
        super(drone, drone_addr, navdata_port, BUFSIZE, reconnect_timeout);
        nsProcessor = new NavDataProcessor(drone, BUFSIZE);
        nsProcessor.setName("NavData decoding thread");
        nsProcessor.start();
    }

    @Override
    void handleData(ByteBuffer inbuf, int len)
    {
       nsProcessor.addDataToProcess(inbuf, len);
    }
    
    public void finish()
    {
      nsProcessor.finish();
      super.finish();
    }

}
