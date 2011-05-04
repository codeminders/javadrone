package com.codeminders.ardrone.commands;

import java.io.UnsupportedEncodingException;

import com.codeminders.ardrone.DroneCommand;

public abstract class ATCommand extends DroneCommand
{
    @Override
    public int getPriority()
    {
        return 10; // All AT commands have same priority.
    }

    public byte[] getPacket(int seq)
    {
        try
        {
            return getCommandString(seq).getBytes("ASCII");
        }
        catch(UnsupportedEncodingException e)
        {
            // never happens
            return null;
        }
    }

    public String getCommandString(int seq)
    {
        return "AT*" + getID() + "=" + seq + getParametersString() + "\n";
    }

    private String getParametersString()
    {
        StringBuffer sb = new StringBuffer();
        for(Object p : getParameters())
        {
            sb.append(',').append(encodeParameter(p));
        }

        return sb.toString();
    }

    private String encodeParameter(Object p)
    {
        if(p instanceof Integer)
            return p.toString();

        if(p instanceof Float)
            return Integer.toString(Float.floatToIntBits((Float)p));

        if(p instanceof String)
            return "\"" + p + "\"";

        throw new IllegalArgumentException("Unsupported parameter type: " + p.getClass().getName() + " " + p);
    }


    protected abstract String getID();

    protected abstract Object[] getParameters();
}
