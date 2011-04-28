
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

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("HIDDeviceInfo [path=");
        builder.append(path);
        builder.append(", vendor_id=");
        builder.append(vendor_id);
        builder.append(", product_id=");
        builder.append(product_id);
        builder.append(", serial_number=");
        builder.append(serial_number);
        builder.append(", release_number=");
        builder.append(release_number);
        builder.append(", manufacturer_string=");
        builder.append(manufacturer_string);
        builder.append(", product_string=");
        builder.append(product_string);
        builder.append(", usage_page=");
        builder.append(usage_page);
        builder.append(", usage=");
        builder.append(usage);
        builder.append(", interface_number=");
        builder.append(interface_number);
        builder.append("]");
        return builder.toString();
    }
    
    
}
