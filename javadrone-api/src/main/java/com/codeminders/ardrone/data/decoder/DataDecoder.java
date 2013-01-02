package com.codeminders.ardrone.data.decoder;

import java.nio.ByteBuffer;

public interface DataDecoder {
    
    void decodeData(ByteBuffer inbuf, int len);
    
    void finish();
}
