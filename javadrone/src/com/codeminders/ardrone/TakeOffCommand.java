package com.codeminders.ardrone;

public class TakeOffCommand extends RefCommand
{
    public TakeOffCommand()
    {
        value |= (1<<9);
    }
}
