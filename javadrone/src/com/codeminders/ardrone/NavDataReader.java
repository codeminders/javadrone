
package com.codeminders.ardrone;

import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

public class NavDataReader implements Runnable
{
    private DatagramSocket         navdata_socket;
    private BlockingQueue<NavData> navdata_queue;

    public NavDataReader(DatagramSocket navdata_socket, BlockingQueue<NavData> navdata_queue)
    {
        this.navdata_socket = navdata_socket;
        this.navdata_queue = navdata_queue;
    }

    @Override
    public void run()
    {
        // TODO Auto-generated method stub

    }

    public void stop()
    {
        // TODO Auto-generated method stub

    }

}
