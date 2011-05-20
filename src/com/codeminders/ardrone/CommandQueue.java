
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
            if(data.isEmpty())
                wait();
            else
                return data.pollLast();
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
                    data.add(pos, cmd);
                else
                    log.finest("Not adding duplicate element " + cmd);
                cmd = null; // inserted
                break;
            }
        }

        if(cmd != null)
            data.addLast(cmd);

        if(data.size() > maxSize)
        {
            // TODO: trim
        }
    }

    public synchronized int size()
    {
        return data.size();
    }

}
