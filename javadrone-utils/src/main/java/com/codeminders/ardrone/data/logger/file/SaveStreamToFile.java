package com.codeminders.ardrone.data.logger.file;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveStreamToFile extends Thread {

    private Logger log = Logger.getLogger(getClass().getName());
    
    FileOutputStream fout;
    DataOutputStream dout;
    LinkedBlockingQueue<Integer> queue;
    boolean done = false;

    public SaveStreamToFile(File file, int queueCapacity) throws FileNotFoundException {
        super();
        fout = new FileOutputStream(file);
        dout = new DataOutputStream(fout);
        queue = new LinkedBlockingQueue<Integer>(queueCapacity);
        setName("File " + file.getName() + " writer");
    }

    @Override
    public void run() {
        Integer toSave;
        while (!done) {
            toSave = null;
            try {
                toSave = queue.take();
            } catch (InterruptedException e) {
                break;
            }
            
            if (null != toSave) {
                try {
                    dout.writeInt(toSave);
                } catch (IOException e) {
                    done = true;
                    log.log(Level.SEVERE, "Failed to store data", e);
                }
            }
        }
        try {
            fout.flush();
            fout.close();
        } catch (IOException e) {}

    }  
    
    public void toFile(int data) {
        try {
            if (!done) {
                queue.put(data);
            } /**else {
                // throw 
            } **/
        } catch (InterruptedException e) {
           // external interrupt
        }
    }
    
    public synchronized void finish()
    {   
        done = true;
        try {
            queue.put(null);
        } catch (InterruptedException e) {}
    }
}
