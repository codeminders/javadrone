package com.codeminders.hidapi;

public class HIDAPITest
{
    static
    {
        System.loadLibrary("hidapi-jni");
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String property = System.getProperty("java.library.path");
        System.err.println(property);
        
        HIDDeviceInfo[] devs = HIDManager.listDevices();
        System.out.println("Devices:\n\n");
        for(int i=0;i<devs.length;i++)
            System.out.println(devs[i]);
        System.out.println("\n---------------\n");       
    }

}
