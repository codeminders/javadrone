package com.codeminders.ardrone;

public abstract class DroneCommand implements Comparable
{
    protected static final int MIN_PRIORITY = 100;
    protected static final int HIGH_PRIORITY = 10;
    protected static final int MAX_PRIORITY = 0;

    @Override
    public int compareTo(Object arg0)
    {
        DroneCommand o=(DroneCommand)arg0;
        return o.getPriority()-this.getPriority();
    }

    public abstract int getPriority();

}
