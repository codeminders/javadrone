package com.codeminders.ardrone;

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

public abstract class DataReader implements Runnable {
	
    private int reconnect_timeout;
    private static final int MAX_TMEOUT = 500;
    private Logger log = Logger.getLogger(this.getClass().getName());
    
    protected DatagramChannel  channel;
    ARDrone                    drone;
    protected Selector         selector;
    private boolean            done;
    private InetAddress        drone_addr;
	private int                data_port;
    
	private long               timeOfLastMessage = 0;
	private int                buffer_size;
	
	
	static final byte[] TRIGGER_BYTES = { 0x01, 0x00, 0x00, 0x00 };
	
	ByteBuffer trigger_buffer = ByteBuffer.allocate(TRIGGER_BYTES.length);
    
    public DataReader(ARDrone drone, InetAddress drone_addr, int data_port, int buffer_size, int reconnect_timeout) throws ClosedChannelException, IOException {
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
            // Ignore
        }

        if (!channel.socket().isClosed()) {
            channel.socket().close();
        }

        try {
            if (channel.isConnected())
                channel.disconnect();
        } catch (IOException iox) 
        {
            // Ignore
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
                selector.select(MAX_TMEOUT);
                if(done)
                {
                    disconnect();
                    break;
                }
                Set readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();
                
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

    abstract void handleData(ByteBuffer buf, int len) throws Exception;

    public void finish()
    {
        done = true;
        if (null != selector) {
            selector.wakeup();
        }
    }
	
}