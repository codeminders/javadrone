package com.codeminders.hidapi;

public class HIDManager
{
    static native HIDDeviceInfo[] listDevices();
}
