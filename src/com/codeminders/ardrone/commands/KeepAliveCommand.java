package com.codeminders.ardrone.commands;

public class KeepAliveCommand extends ATCommand
{
    
    @Override
    protected String getID()
    {
        return "COMWDG";
    }

    @Override
    public int getPriority()
    {
        return HIGH_PRIORITY;
    }
    
    @Override
    protected Object[] getParameters()
    {
        return new Object[] {};
    }
}
