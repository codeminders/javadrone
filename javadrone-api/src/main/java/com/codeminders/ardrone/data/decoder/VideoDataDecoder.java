package com.codeminders.ardrone.data.decoder;

import java.nio.ByteBuffer;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.video.BufferedVideoImage;

public class VideoDataDecoder extends OnlyActualDataDecoder {
    
    final BufferedVideoImage vi = new BufferedVideoImage();
    
    public VideoDataDecoder(ARDrone drone, int buffer_size) {
        super(drone, buffer_size);
        setName("Video decoding thread");
    }

    public void decodeActualData(ByteBuffer infbuf, int len) {      
        vi.addImageStream(infbuf);
        drone.videoFrameReceived(0, 0, vi.getWidth(), vi.getHeight(), vi.getJavaPixelData(), 0, vi.getWidth());
    }
}
