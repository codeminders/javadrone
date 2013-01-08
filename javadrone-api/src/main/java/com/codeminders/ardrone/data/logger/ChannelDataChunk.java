package com.codeminders.ardrone.data.logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChannelDataChunk {
    
    byte[] data;
    long ioDelay;
    
    public ChannelDataChunk(byte[] data, long ioDelay) {
        super();
        this.data = data;
        this.ioDelay = ioDelay;
    }
    
    public byte[] getData() {
        return data;
    }
    public long getIoDelay() {
        return ioDelay;
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeLong(ioDelay);
        out.writeInt(data.length);
        out.write(data, 0, data.length);
    }
    
    public static ChannelDataChunk readFromStream(DataInputStream in)  throws IOException {
        long delay = in.readLong();
        byte[] dt = new byte[in.readInt()];
        in.readFully(dt);

        return new ChannelDataChunk(dt, delay);
    }
   
}
