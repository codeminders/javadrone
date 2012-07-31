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

public abstract class DataReader implements Runnable {
	
    private static final int RECONNECT_TIMEOUT = 1000;
    private static final int MAX_TMEOUT = 5;

    protected DatagramChannel  channel;
    ARDrone                    drone;
    protected Selector         selector;
    private boolean            done;
    private InetAddress        drone_addr;
	private int                data_port;
    
	private long               timeOfLastMessage = 0;
	private int                buffer_size;
	
	
    public DataReader(ARDrone drone, InetAddress drone_addr, int data_port, int buffer_size) throws ClosedChannelException, IOException {
        super();
        this.drone = drone;
        this.drone_addr = drone_addr;
        this.data_port = data_port;
        this.buffer_size = buffer_size;
        
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
                    if (timeOfLastMessage > 0 && System.currentTimeMillis() - timeOfLastMessage > RECONNECT_TIMEOUT ) {
                        disconnect();
                        try {
                            connect();
                        } catch (Exception e) {
                            // ignore
                        }
                        timeOfLastMessage = System.currentTimeMillis();
                    }
                }
                while(iterator.hasNext())
                {
                    timeOfLastMessage = System.currentTimeMillis();
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    handleReceivedMessageKey(key, inbuf);
                }
            }
        } catch(Exception e)
        {
            drone.changeToErrorState(e);
        }

    }

    abstract void handleReceivedMessageKey(SelectionKey key, ByteBuffer inbuf) throws Exception;

    public void stop()
    {
        done = true;
        selector.wakeup();
    }
	
}