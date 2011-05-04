package com.codeminders.hidapi;

import java.io.IOException;

public class HIDAPITest
{
    private static final long READ_UPDATE_DELAY_MS = 50L;

    static
    {
        System.loadLibrary("hidapi-jni");
    }

    // "Afterglow" controller for PS3
    static final int VENDOR_ID = 3695;
    static final int PRODUCT_ID = 25346;
    private static final int BUFSIZE = 2048; 
    
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
            System.err.print("Manufacturer: " + dev.getManufacturerString() + "\n");
            System.err.print("Product: " + dev.getProductString() + "\n");
            System.err.print("Serial Number: " + dev.getSerialNumberString() + "\n");
            try
            {
                byte[] buf = new byte[BUFSIZE];
                dev.enableBlocking();
                while(true)
                {
                    int n = dev.read(buf);
                    System.err.print(""+n+" bytes read:\n\t");
                    for(int i=0; i<n; i++)
                    {
                        int v = buf[i];
                        if (v<0) v = n+256;
                        String hs = Integer.toHexString(v);
                        if (v<16) 
                            System.err.print("0");
                        System.err.print(hs + " ");
                    }
                    System.err.println("");
                    
                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    } catch(InterruptedException e)
                    {
                        //Ignore
                    }
                }
            } finally
            {
                dev.close();
            }
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void listDevices()
    {
        String property = System.getProperty("java.library.path");
        System.err.println(property);
        
        try
        {
            HIDDeviceInfo[] devs = HIDManager.listDevices();
            System.err.println("Devices:\n\n");
            for(int i=0;i<devs.length;i++)
            {
                System.err.println(""+i+".\t"+devs[i]);
                System.err.println("---------------------------------------------\n");
            }
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
