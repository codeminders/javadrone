package com.codeminders.ardrone;

public class QuitCommand extends DroneCommand
{
    @Override
    public int getPriority()
    {
        return 0;
    }
}
