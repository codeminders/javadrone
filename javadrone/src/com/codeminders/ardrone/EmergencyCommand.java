package com.codeminders.ardrone;

public class EmergencyCommand extends RefCommand
{
    public EmergencyCommand()
    {
        value |= (1<<8);
    }
}
