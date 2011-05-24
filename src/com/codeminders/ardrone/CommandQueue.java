
package com.codeminders.ardrone;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

public class CommandQueue
{
    private LinkedList<DroneCommand> data;
    private int                      maxSize;
    private Logger                   log = Logger.getLogger(getClass().getName());

    public CommandQueue(int maxSize)
    {
        data = new LinkedList<DroneCommand>();
        this.maxSize = maxSize;
    }

    public synchronized DroneCommand take() throws InterruptedException
    {
        while(true)
        {
            DroneCommand res = data.pollLast();
            if(res != null)
            {
                //log.finest("[" + data.size() + "] Returning " + res);
                if(res.isSticky())
                    data.addLast(res);
                return res;
            } else
            {
                // log.finest("Waiting for data");
                wait();
            }
        }
    }

    public synchronized void add(DroneCommand cmd)
    {
        Iterator<DroneCommand> i = data.iterator();
        int p = cmd.getPriority();
        int pos = -1;

        while(i.hasNext())
        {
            DroneCommand x = i.next();
            pos++;
            int xp = x.getPriority();
            if(xp < p)
            {
                // Skipping
                continue;
            } else
            {
                // Found insertion point.
                if(!x.equals(cmd))
                {
                    // log.finest("[" + data.size() + "] Adding command " +
                    // cmd);
                    data.add(pos, cmd);
                    notify();
                }
                // else
                // log.finest("Not adding duplicate element " + cmd);
                cmd = null; // inserted
                break;
            }
        }

        if(cmd != null)
        {
            // log.finest("[" + data.size() + "] Adding command " + cmd);
            data.addLast(cmd);
            notify();
        }

        if(data.size() > maxSize)
        {
            // TODO: trim
        }
    }

    public synchronized int size()
    {
        return data.size();
    }

    public synchronized void clear()
    {
        data.clear();
        notify();
    }

}
