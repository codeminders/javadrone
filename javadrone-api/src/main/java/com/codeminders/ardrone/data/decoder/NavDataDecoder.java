package com.codeminders.ardrone.data.decoder;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataFormatException;

public class NavDataDecoder extends OnlyActualDataDecoder {

    private Logger log = Logger.getLogger(this.getClass().getName());
            
    public NavDataDecoder(ARDrone drone, int buffer_size) {
        super(drone, buffer_size);
        setName("NavData decodding thread");
    }

    @Override
    public void decodeActualData(ByteBuffer inbuf, int len) {
        try {
            drone.navDataReceived(NavData.createFromData(inbuf, len));
        } catch (NavDataFormatException ex) {
            log.log(Level.FINE ,"Failed to decode received navdata. Reason unsupported format", ex);
        }
    }

}
