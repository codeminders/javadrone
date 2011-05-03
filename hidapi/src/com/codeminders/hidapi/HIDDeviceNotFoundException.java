package com.codeminders.hidapi;

import java.io.IOException;

public class HIDDeviceNotFoundException extends IOException
{
    public HIDDeviceNotFoundException()
    {
    }

    public HIDDeviceNotFoundException(String message)
    {
        super(message);
    }
}