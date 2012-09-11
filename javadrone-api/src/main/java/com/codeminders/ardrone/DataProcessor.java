package com.codeminders.ardrone;

import java.nio.ByteBuffer;

public abstract class DataProcessor extends Thread {
    
    ARDrone   drone;
    ByteBuffer worckInbuf;
    int worckInDataBufferLength;
    ByteBuffer nextBuffer;
    int nextDataBufferLength;
    boolean nextEmpty = true;
    
    boolean done = false;
    
    Object lock = new Object();
    
    public DataProcessor(ARDrone drone, int buffer_size) {
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
                        lock.wait();
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
            
            processData(worckInbuf, worckInDataBufferLength);
            
        }

    }


    abstract void processData(ByteBuffer inbuf, int len);
    
    public void addDataToProcess(ByteBuffer infBuffer, int len) {
        
        synchronized (nextBuffer) {
            nextBuffer.clear();
            nextBuffer.put(infBuffer);
            nextBuffer.flip();
            nextEmpty = false; 
            nextDataBufferLength = len;
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