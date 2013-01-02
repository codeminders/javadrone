package com.codeminders.ardrone.data.decoder;

import java.nio.ByteBuffer;

import com.codeminders.ardrone.ARDrone;

/**
 * Data processing is taking some time. We constantly receive income data. 
 * If system is under decoding of current data packet and we receive two more
 * current realization of data processing mechanism will process only last data packet that was received.    
 *
 * This class makes an assumption - that we receive a complete amount of data in one chunk. 
 */
public abstract class OnlyActualDataDecoder extends Thread implements DataDecoder {
    
    ARDrone   drone;
    ByteBuffer worckInbuf;
    int worckInDataBufferLength;
    ByteBuffer nextBuffer;
    int nextDataBufferLength;
    boolean nextEmpty = true;
    
    boolean done = false;
    
    Object lock = new Object();
    
    public OnlyActualDataDecoder(ARDrone drone, int buffer_size) {
        super();
        this.drone = drone;
        worckInbuf = ByteBuffer.allocate(buffer_size);
        nextBuffer = ByteBuffer.allocate(buffer_size);
    }


    @Override
    public void run() {
        
        while(!done) {
            
            if (nextEmpty) { 
                synchronized (lock) {
                    try {
                        if (nextEmpty) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        done = true;
                    }   
                }
            }
            
            if (done) {
                break;
            }
            
            synchronized (nextBuffer) {
                if (!nextEmpty) {
                    worckInbuf.clear();
                    worckInbuf.put(nextBuffer);
                    worckInbuf.flip();
                    worckInDataBufferLength = nextDataBufferLength;
                    nextEmpty = true;
                } 
            }
            
            decodeActualData(worckInbuf, worckInDataBufferLength);
            
        }

    }
    
    abstract void decodeActualData(ByteBuffer infBuffer, int len);

    public void decodeData(ByteBuffer infBuffer, int len) {
        
        synchronized (nextBuffer) {
            nextBuffer.clear();
            nextBuffer.put(infBuffer);
            nextBuffer.flip();
            nextDataBufferLength = len;
            nextEmpty = false;
        }
        
        synchronized (lock) {
            lock.notify(); 
        }
    }
    
    public void finish()
    {
        done = true;
        synchronized (lock) {
            lock.notify(); 
        }
    }
    
}