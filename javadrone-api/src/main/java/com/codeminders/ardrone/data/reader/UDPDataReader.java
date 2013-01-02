package com.codeminders.ardrone.data.reader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.ardrone.ARDrone;

public abstract class UDPDataReader implements Runnable {
	
    private int reconnect_timeout;
    private static final int MAX_TMEOUT = 500;
    private Logger log = Logger.getLogger(this.getClass().getName());
    
    protected DatagramChannel  channel;
    ARDrone                    drone;
    protected Selector         selector;
    private boolean            done;
    private boolean            pauseFlag;
    private InetAddress        drone_addr;
	private int                data_port;
    
	private long               timeOfLastMessage = 0;
	private int                buffer_size;
	
	static final byte[] TRIGGER_BYTES = { 0x01, 0x00, 0x00, 0x00 };
	
	ByteBuffer trigger_buffer = ByteBuffer.allocate(TRIGGER_BYTES.length);

    
    public UDPDataReader(ARDrone drone, InetAddress drone_addr, int data_port, int buffer_size, int reconnect_timeout) throws ClosedChannelException, IOException {
        super();
        this.drone = drone;
        this.drone_addr = drone_addr;
        this.data_port = data_port;
        this.buffer_size = buffer_size;
        this.reconnect_timeout = reconnect_timeout;
        
        trigger_buffer.put(TRIGGER_BYTES);
        trigger_buffer.flip();
        
        connect();
    }

    private void connect() throws IOException, ClosedChannelException {
        
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(data_port));
        channel.connect(new InetSocketAddress(drone_addr, data_port));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void disconnect() {
        try {
            if (selector.isOpen())
                selector.close();
        } catch (IOException iox) 
        {
            // ignore
        }
        
        if (!channel.socket().isClosed()) {
            channel.socket().close();
        }

        try {
            if (channel.isConnected())
                channel.disconnect();
        } catch (IOException iox) 
        {
           // ignore
        }
        
        try {
            channel.close();
        } catch (IOException iox) {
            // ignore
        }
        
    }
    
    @Override
    public void run()
    {
        try
        {
            ByteBuffer inbuf = ByteBuffer.allocate(buffer_size);
            done = false;
            timeOfLastMessage = System.currentTimeMillis();
            while(!done)
            {
                if (pauseFlag) {
                    synchronized(this) {
                        if (pauseFlag) {
                           wait();
                           timeOfLastMessage = 1; // will automatically reconnect channel
                        }
                    }
                }
                
                selector.select(MAX_TMEOUT);
                if(done)
                {
                    disconnect();
                    break;
                }
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                
                if (!iterator.hasNext()) {
                    if (timeOfLastMessage > 0 && System.currentTimeMillis() - timeOfLastMessage > reconnect_timeout ) {
                        log.fine("Data Timeout in " + reconnect_timeout + "ms. reached. Attemting to reconnect" );
                        disconnect();
                        try {
                            connect();
                        } catch (Exception e) {                           
                            log.log(Level.FINE, "Failed to re-connect", e);
                        }
                        timeOfLastMessage = System.currentTimeMillis();
                    }
                }
                while(iterator.hasNext())
                {
                    timeOfLastMessage = System.currentTimeMillis();
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    
                    if(key.isWritable())
                    {
                        channel.write(trigger_buffer);
                        channel.register(selector, SelectionKey.OP_READ);
                        // prepare buffer for new reconnection attempt
                        trigger_buffer.clear();
                        trigger_buffer.put(TRIGGER_BYTES);
                        trigger_buffer.flip();
                    } 
                    else if(key.isReadable())
                    {
                        inbuf.clear(); 
                        int len = channel.read(inbuf);
                        inbuf.flip();
                        handleData(inbuf, len);
                    }
                }
            }
        } catch(Exception e)
        {
            if (!done) {
                drone.changeToErrorState(e);
            }
        }

    }

    public abstract void handleData(ByteBuffer buf, int len) throws Exception;

    public synchronized void finish()
    {  
        if (pauseFlag) {
           resumeReading();
        }
        done = true;
        if (null != selector) {
            selector.wakeup();
        }
    }
    
    public synchronized void pauseReading() {
        pauseFlag = true;
    }
	
    public synchronized void resumeReading() {
        pauseFlag = false;
        notify();
    }
}