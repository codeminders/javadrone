
package com.codeminders.ardrone;

import java.util.PriorityQueue;

public class CommandQueue
{
    private final PriorityQueue<DroneCommand> q;

    public CommandQueue(int cmdQueueSize)
    {
        q = new PriorityQueue<DroneCommand>(cmdQueueSize);
    }

    public DroneCommand take() throws InterruptedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(DroneCommand cmd)
    {
        // TODO Auto-generated method stub
    }

    public int size()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
