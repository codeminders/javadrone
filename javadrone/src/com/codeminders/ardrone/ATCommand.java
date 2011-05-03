package com.codeminders.ardrone;

public class ATCommand extends DroneCommand
{

    @Override
    public int getPriority()
    {
        return 10; // All AT commands have same priority.
    }

    public byte[] getPacket()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
