
package com.codeminders.ardrone;

import java.util.LinkedList;

public class CommandQueue
{
    private LinkedList<DroneCommand> data;
    private int                      maxSize;

    public CommandQueue(int maxSize)
    {
        data = new LinkedList<DroneCommand>();
        this.maxSize = maxSize;
    }

    public synchronized DroneCommand take() throws InterruptedException
    {
        while(true)
        {
            if(data.isEmpty())
                wait();
            else
                return data.pop();
        }
    }

    public synchronized void add(DroneCommand cmd)
    {
        // TODO Auto-generated method stub
    }

    public synchronized int size()
    {
        return data.size();
    }

}
