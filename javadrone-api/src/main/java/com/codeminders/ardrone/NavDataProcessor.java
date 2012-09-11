package com.codeminders.ardrone;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NavDataProcessor  extends DataProcessor {

    private Logger log = Logger.getLogger(this.getClass().getName());
            
    public NavDataProcessor(ARDrone drone, int buffer_size) {
        super(drone, buffer_size);
    }

    @Override
    void processData(ByteBuffer inbuf, int len) {
        try {
            drone.navDataReceived(NavData.createFromData(inbuf, len));
        } catch (NavDataFormatException ex) {
            log.log(Level.FINE ,"Failed to decode received navdata. Reason unsupported format", ex);
        }
    }

}
