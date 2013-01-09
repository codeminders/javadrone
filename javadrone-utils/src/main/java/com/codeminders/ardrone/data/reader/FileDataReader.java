package com.codeminders.ardrone.data.reader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.ardrone.data.logger.ChannelDataChunk;

public abstract class FileDataReader implements Runnable {
   
    private boolean            done;
    private Logger             log = Logger.getLogger(this.getClass().getName());
    
    File                       dataFile;
    FileInputStream            fin;
    DataInputStream            din;
    
    long                       lastFrameTime = 0;
    
    public FileDataReader(File dataFile) throws FileNotFoundException {
        super();
        this.dataFile = dataFile;
        fin = new FileInputStream(dataFile);
    }

    @Override
    public void run() {
        din = new DataInputStream(fin);
        
        ChannelDataChunk dataChunk = null;
        
        try {
            while (!done) {
                 dataChunk = ChannelDataChunk.readFromStream(din);
                 if (null == dataChunk.getData()) {
                     break;
                 }
                 
                 if (lastFrameTime > 0 && dataChunk.getIoDelay() > 0) {
                     Thread.sleep(dataChunk.getIoDelay() - lastFrameTime);
                 }
                  
                 lastFrameTime = dataChunk.getIoDelay();
                 
                 handleData(ByteBuffer.wrap(dataChunk.getData()), dataChunk.getData().length);
            }  
        } catch (IOException e) {
            log.log(Level.SEVERE, "DataReading is stoped", e);
        } catch (Exception e) {
            log.log(Level.SEVERE, "DataReading is stoped", e);
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to correctly close input stream", e);
            }
        }
    }

    public abstract void handleData(ByteBuffer buf, int len) throws Exception;
    
    public synchronized void finish()
    {   
        done = true;
    }
}
