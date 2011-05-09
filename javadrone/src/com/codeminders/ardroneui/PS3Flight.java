package com.codeminders.ardroneui;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.hidapi.*;

import java.io.IOException;


/**
 * Created by IntelliJ IDEA.
 * User: bird
 * Date: 5/6/11
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PS3Flight
{
    private static final long READ_UPDATE_DELAY_MS = 50L;

    static
    {
        System.loadLibrary("hidapi-jni");
    }

    private static final int BUFSIZE    = 2048;


    /**
     * @param args
     */
    public static void main(String[] args)
    {
        readDevice();
    }

    private static void readDevice()
    {
        ARDrone drone;
        HIDDevice dev;
        try
        {
            drone = new ARDrone();
            drone.connect();

            dev = HIDManager.openById(AfterGlowController.VENDOR_ID, AfterGlowController.PRODUCT_ID, null);
            System.err.print("Manufacturer: " + dev.getManufacturerString() + "\n");
            System.err.print("Product: " + dev.getProductString() + "\n");
            System.err.print("Serial Number: " + dev.getSerialNumberString() + "\n");
            try
            {
                byte[] buf = new byte[BUFSIZE];
                byte[] prev_buf = new byte[BUFSIZE];
                int prev_n = 0;
                dev.enableBlocking();
                while(true)
                {
                    int n = dev.read(buf);
                    printDelta(prev_buf, prev_n, buf, n);
                    prev_n = n;
                    System.arraycopy(buf, 0, prev_buf, 0, n);

                    if(n == 0)
                        continue;

                    if(buf[1] == 2)
                        drone.takeOff();
                    else
                    if(buf[1] == 1)
                        drone.land();


                    //System.err.print("" + n + " bytes read:\n\t");
//                    for(int i = 0; i < n; i++)
//                    {
//                        int v = buf[i];
//                        if(v < 0) v = n + 256;
//                        String hs = Integer.toHexString(v);
//                        if(v < 16)
//                            System.err.print("0");
//                        System.err.print(hs + " ");
//                    }
//                    System.err.println("");

                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    }
                    catch(InterruptedException e)
                    {
                        //Ignore
                    }
                }
            } 
            finally
            {
                dev.close();
            }
        } catch(HIDDeviceNotFoundException hex)
        {
            hex.printStackTrace();
            listDevices();
        }
        catch(Throwable e)
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
            for(int i = 0; i < devs.length; i++)
            {
                System.err.println("" + i + ".\t" + devs[i]);
                System.err.println("---------------------------------------------\n");
            }
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printDelta(byte[] prev, int prev_size, byte[] cur, int cur_size)
    {
        if(prev_size != cur_size)
        {
            System.err.println("Packet size is different. Prev: " + prev_size + " New: " + cur_size);
            return;
        }

        for(int i = 0; i < prev_size; i++)
        {
            if(prev[i] != cur[i])
            {
                System.err.println("Index: " + i +
                                       " Prev value: " + Integer.toHexString((int) prev[i]) +
                                       " New value: " + Integer.toHexString((int) cur[i])
                );
            }
        }
    }
}
