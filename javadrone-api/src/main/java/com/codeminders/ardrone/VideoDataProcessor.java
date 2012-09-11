package com.codeminders.ardrone;

import java.nio.ByteBuffer;

import com.codeminders.ardrone.video.BufferedVideoImage;

public class VideoDataProcessor extends DataProcessor {
    
    final BufferedVideoImage vi = new BufferedVideoImage();
    
    public VideoDataProcessor(ARDrone drone, int buffer_size) {
        super(drone, buffer_size);
        setName("Video decoding thread");
    }

    void processData(ByteBuffer infbuf, int len) {      
        vi.addImageStream(infbuf);
        drone.videoFrameReceived(0, 0, vi.getWidth(), vi.getHeight(), vi.getJavaPixelData(), 0, vi.getWidth());
    }
}
