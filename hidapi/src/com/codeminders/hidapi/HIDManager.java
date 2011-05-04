
package com.codeminders.hidapi;

import java.io.IOException;

/**
 * High-level interface to enumerate, find and open HID devices
 * 
 * @author lord
 * 
 */
public class HIDManager
{
    public static native HIDDeviceInfo[] listDevices() throws IOException;

    /**
     * Convenience method to find and open device by path
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public static HIDDevice openByPath(String path) throws IOException, HIDDeviceNotFoundException
    {
        HIDDeviceInfo[] devs = listDevices();
        for(HIDDeviceInfo d : devs)
        {
            if(d.getPath().equals(path))
                return d.open();
        }
        throw new HIDDeviceNotFoundException(); 
    }

    /**
     * Convenience method to open a HID device using a Vendor ID (VID), Product
     * ID (PID) and optionally a serial number.
     * 
     * @param vendor_id
     * @param product_id
     * @param serial_number
     * @return
     * @throws IOException
     */
    public static HIDDevice openById(int vendor_id, int product_id, String serial_number) throws IOException, HIDDeviceNotFoundException
    {
        HIDDeviceInfo[] devs = listDevices();
        for(HIDDeviceInfo d : devs)
        {
            if(d.getVendor_id() == vendor_id && d.getProduct_id() == product_id
                    && (serial_number == null || d.getSerial_number().equals(serial_number)))
                return d.open();
        }
        throw new HIDDeviceNotFoundException(); 
    }

}
