package com.codeminders.hidapi;

import java.io.IOException;

public class HIDAPITest
{
    static
    {
        System.loadLibrary("hidapi-jni");
    }

    static final int VENDOR_ID = 1452; //Apple
    static final int PRODUCT_ID = 566; // Keyboard/trackpad
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        listDevices();
        readDevice();
    }

    private static void readDevice()
    {
        HIDDevice dev;
        try
        {
            dev = HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
            dev.close();
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void listDevices()
    {
        String property = System.getProperty("java.library.path");
        System.err.println(property);
        
        HIDDeviceInfo[] devs = HIDManager.listDevices();
        System.out.println("Devices:\n\n");
        for(int i=0;i<devs.length;i++)
        {
            System.out.println(""+i+".\t"+devs[i]);
            System.out.println("---------------------------------------------\n");
        }
    }

}
