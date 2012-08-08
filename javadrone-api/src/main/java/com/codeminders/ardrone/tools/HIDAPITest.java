
package com.codeminders.ardrone.tools;

import java.io.IOException;
import java.util.BitSet;

import com.codeminders.hidapi.*;

public class HIDAPITest
{
    private static final long READ_UPDATE_DELAY_MS = 500L;

    static
    {
        ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    private static final int  BUFSIZE              = 2048;

    // "Afterglow" controller for PS3
    // static final int VENDOR_ID = 3695;
    // static final int PRODUCT_ID = 25346;

    // Sony PLAYSTATION(R)3 Controller
    static final int          VENDOR_ID            = 1356;
    static final int          PRODUCT_ID           = 616;

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
            dev = HIDManager.getInstance().openById(VENDOR_ID, PRODUCT_ID, null);
            System.err.print("Manufacturer: " + dev.getManufacturerString() + "\n");
            System.err.print("Product: " + dev.getProductString() + "\n");
            System.err.print("Serial Number: " + dev.getSerialNumberString() + "\n");
            try
            {
                byte[] buf = new byte[BUFSIZE];
                BitSet old = null;

                dev.enableBlocking();
                while(true)
                {
                    int n = dev.read(buf);
                    if(n != 49)
                    {
                        System.err.println("Unexpected data packet size!");
                        return;
                    }

                    BitSet current = arrayToBitSet(buf, n);
                    if(old != null)
                    {
                        printHEX(buf, n);
                        printBitSetDiff(current, old);
                    }
                    old = current;
                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    } catch(InterruptedException e)
                    {
                        // Ignore
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

    private static void printBitSetDiff(BitSet current, BitSet old)
    {
        if(current.length() != old.length())
        {
            System.err.println("BitSet size does not match!");
            return;
        }

        BitSet diff = (BitSet) current.clone();
        diff.xor(old);
        System.err.println(diff);
    }

    private static BitSet arrayToBitSet(byte[] buf, int n)
    {
        BitSet bs = new BitSet(n * 8);
        for(int i = 0; i < n; i++)
        {
            byte b = buf[i];
            for(int j = 0; j < 8; j++)
            {
                if((b & (1 << j)) > 0)
                    bs.set(i * 8 + j);
            }
        }
        return bs;
    }

    private static void printHEX(byte[] buf, int n)
    {
        for(int i = 0; i < n; i++)
        {
            int v = buf[i];
            if(v < 0)
                v = v + 256;
            String hs = Integer.toHexString(v);
            if(v < 16)
                System.err.print("0");
            System.err.print(hs + " ");
        }
        System.err.println("");
    }

    private static void listDevices()
    {
        String property = System.getProperty("java.library.path");
        System.err.println(property);

        try
        {
            HIDDeviceInfo[] devs = HIDManager.getInstance().listDevices();
            System.err.println("Devices:\n\n");
            if (null != devs) {
                for(int i = 0; i < devs.length; i++)
                {
                    System.err.println("" + i + ".\t" + devs[i]);
                    System.err.println("---------------------------------------------\n");
                }
            }
        } catch(IOException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
