package com.codeminders.ardrone.decoder;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.data.decoder.DataDecoder;
import com.twilight.h264.decoder.AVFrame;
import com.twilight.h264.decoder.AVPacket;
import com.twilight.h264.decoder.H264Decoder;
import com.twilight.h264.decoder.MpegEncContext;
import com.twilight.h264.player.FrameUtils;

public class TestH264DataDecoder implements DataDecoder {

    ARDrone drone;
    int buffer_size;
    //public static final int INBUF_SIZE = 65535;
    public int INBUF_SIZE;
    
    H264Decoder codec;
    MpegEncContext c = null;
    int frame, len;
    int[] got_picture = new int[1];
    
    AVFrame picture;
    
    byte[] inbuf = new byte[INBUF_SIZE + MpegEncContext.FF_INPUT_BUFFER_PADDING_SIZE];
    int[] inbuf_int = new int[INBUF_SIZE + MpegEncContext.FF_INPUT_BUFFER_PADDING_SIZE];
    byte[] buf = new byte[1024];
    private int[] buffer = null;
    
    AVPacket avpkt;
    
    int dataPointer;
    
    
    public TestH264DataDecoder(ARDrone drone, int buffer_size) {
        super();
        this.drone = drone;
        this.buffer_size = buffer_size;
        INBUF_SIZE = buffer_size;
        
        avpkt = new AVPacket();
        avpkt.av_init_packet();
        
        Arrays.fill(inbuf, INBUF_SIZE, MpegEncContext.FF_INPUT_BUFFER_PADDING_SIZE + INBUF_SIZE, (byte)0);
        
        codec = new H264Decoder();
        if (codec == null) {
            System.out.println("codec not found\n");
            System.exit(1);
        } 

        c = MpegEncContext.avcodec_alloc_context();
        picture= AVFrame.avcodec_alloc_frame();

        if((codec.capabilities & H264Decoder.CODEC_CAP_TRUNCATED)!=0)
            c.flags |= MpegEncContext.CODEC_FLAG_TRUNCATED; /* we do not send complete frames */

        if (c.avcodec_open(codec) < 0) {
            System.out.println("could not open codec\n");
            System.exit(1);
        }
    }

    @Override
    public void decodeData(ByteBuffer fin, int len) {
        
        try {
            // avpkt must contain exactly 1 NAL Unit in order for decoder to decode correctly.
            // thus we must read until we get next NAL header before sending it to decoder.
            // Find 1st NAL
            int[] cacheRead = new int[3];
            cacheRead[0] = fin.getInt();
            cacheRead[1] = fin.getInt();
            cacheRead[2] = fin.getInt();
            
            while(!(
                    cacheRead[0] == 0x00 &&
                    cacheRead[1] == 0x00 &&
                    cacheRead[2] == 0x01 
                    )) {
                 cacheRead[0] = cacheRead[1];
                 cacheRead[1] = cacheRead[2];
                 cacheRead[2] = fin.getInt();
            } // while
            
            boolean hasMoreNAL = true;
            
            // 4 first bytes always indicate NAL header
            inbuf_int[0]=inbuf_int[1]=inbuf_int[2]=0x00;
            inbuf_int[3]=0x01;
            
            while(hasMoreNAL) { // TODO: Possible error because we use not file 
                dataPointer = 4;
                // Find next NAL
                cacheRead[0] = fin.getInt();
                if(cacheRead[0]==-1) hasMoreNAL = false;
                cacheRead[1] = fin.getInt();
                if(cacheRead[1]==-1) hasMoreNAL = false;
                cacheRead[2] = fin.getInt();
                if(cacheRead[2]==-1) hasMoreNAL = false;
                while(!(
                        cacheRead[0] == 0x00 &&
                        cacheRead[1] == 0x00 &&
                        cacheRead[2] == 0x01 
                        ) && hasMoreNAL) {
                     inbuf_int[dataPointer++] = cacheRead[0];
                     cacheRead[0] = cacheRead[1];
                     cacheRead[1] = cacheRead[2];
                     cacheRead[2] = fin.getInt();
                    if(cacheRead[2]==-1) hasMoreNAL = false;
                } // while

                avpkt.size = dataPointer;

                avpkt.data_base = inbuf_int;
                avpkt.data_offset = 0;

                try {
                    while (avpkt.size > 0) {
                        len = c.avcodec_decode_video2(picture, got_picture, avpkt);
                        if (len < 0) {
                            System.out.println("Error while decoding frame "+ frame);
                            // Discard current packet and proceed to next packet
                            break;
                        } // if
                        if (got_picture[0]!=0) {
                            picture = c.priv_data.displayPicture;
        
                            int bufferSize = picture.imageWidth * picture.imageHeight;
                            if (buffer == null || bufferSize != buffer.length) {
                                buffer = new int[bufferSize];
                            }
                            FrameUtils.YUV2RGB(picture, buffer);       
                            
                            drone.videoFrameReceived(0, 0, picture.imageWidth ,picture.imageHeight, buffer, 0,  picture.imageWidth);                     
                        }
                        avpkt.size -= len;
                        avpkt.data_offset += len;
                    }
                } catch(Exception ie) {
                    // Any exception, we should try to proceed reading next packet!
                    ie.printStackTrace();
                } // try
                
            } // while
                    
    
        } catch(Exception e) {
            e.printStackTrace();
        }  


    }

    @Override
    public void finish() {
        c.avcodec_close();
    }

}
