
package com.codeminders.ardrone;

public abstract class DroneCommand implements Comparable
{
    protected static final int MIN_PRIORITY       = 0;
    protected static final int HIGH_PRIORITY      = 50;
    protected static final int VERY_HIGH_PRIORITY = 90;
    protected static final int MAX_PRIORITY       = 100;

    @Override
    public int compareTo(Object arg0)
    {
        DroneCommand o = (DroneCommand) arg0;
        //TODO: take into account isSticky()
        return o.getPriority() - this.getPriority();
    }

    public abstract int getPriority();

    public boolean isSticky()
    {
        return false;
    }

}
