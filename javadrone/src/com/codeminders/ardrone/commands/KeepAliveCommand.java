package com.codeminders.ardrone.commands;

public class KeepAliveCommand extends ATCommand
{
    @Override
    protected String getID()
    {
        return "COMWDG";
    }

    @Override
    protected Object[] getParameters()
    {
        return new Object[] {};
    }
}
