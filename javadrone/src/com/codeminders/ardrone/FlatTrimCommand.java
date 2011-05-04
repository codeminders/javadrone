package com.codeminders.ardrone;

public class FlatTrimCommand extends ATCommand
{
    @Override
    protected String getID()
    {
        return "FTRIM";
    }

    @Override
    protected Object[] getParameters()
    {
        return new Object[] {};
    }
}
