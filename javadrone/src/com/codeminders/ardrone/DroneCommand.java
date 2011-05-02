package com.codeminders.ardrone;

public abstract class DroneCommand implements Comparable
{
    public abstract int getPriority();

    @Override
    public int compareTo(Object arg0)
    {
        DroneCommand o=(DroneCommand)arg0;
        return o.getPriority()-this.getPriority();
    }

}
