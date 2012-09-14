
package com.codeminders.ardrone.controller;

import java.io.IOException;

import android.hardware.usb.UsbDevice;


/**
 * Base abstract class for supported PS3-compatible USB controllers
 * 
 * @author lord
 * 
 */
public abstract class PS3Controller
{
    UsbDevice dev;

 
    public abstract void close() throws IOException;


    public abstract PS3ControllerState read() throws IOException;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PS3Controller [");
        builder.append("DeviceName=");
        builder.append(dev.getDeviceName());
        builder.append(", product=");
        builder.append(dev.getProductId());
        builder.append("]");
        return builder.toString();
    }

}