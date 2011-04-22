
package com.codeminders.hidapi;

import java.io.IOException;

/**
 * HID device info
 * 
 * @author lord
 *
 */
public class HIDDeviceInfo
{
    private String path;
    private int    vendor_id;
    private int    product_id;
    private String serial_number;
    private int    release_number;
    private String manufacturer_string;
    private String product_string;
    private int    usage_page;
    private int    usage;
    private int    interface_number;

    /**
     * Protected constructor, used from JNI
     */
    HIDDeviceInfo()
    {
    }

    public HIDDeviceInfo(String path, int vendor_id, int product_id, String serial_number, int release_number,
            String manufacturer_string, String product_string, int usage_page, int usage, int interface_number)
    {
        this.path = path;
        this.vendor_id = vendor_id;
        this.product_id = product_id;
        this.serial_number = serial_number;
        this.release_number = release_number;
        this.manufacturer_string = manufacturer_string;
        this.product_string = product_string;
        this.usage_page = usage_page;
        this.usage = usage;
        this.interface_number = interface_number;
    }    
    
    public String getPath()
    {
        return path;
    }

    public int getVendor_id()
    {
        return vendor_id;
    }

    public int getProduct_id()
    {
        return product_id;
    }

    public String getSerial_number()
    {
        return serial_number;
    }

    public int getRelease_number()
    {
        return release_number;
    }

    public String getManufacturer_string()
    {
        return manufacturer_string;
    }

    public String getProduct_string()
    {
        return product_string;
    }

    public int getUsage_page()
    {
        return usage_page;
    }

    public int getUsage()
    {
        return usage;
    }

    public int getInterface_number()
    {
        return interface_number;
    }
   
    public native HIDDevice open() throws IOException;
    
}
