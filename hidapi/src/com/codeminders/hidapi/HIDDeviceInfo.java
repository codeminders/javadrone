
package com.codeminders.hidapi;

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
}
