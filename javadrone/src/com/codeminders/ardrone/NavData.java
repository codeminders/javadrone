package com.codeminders.ardrone;

public abstract class NavData
{
    public static NavData createFromData(byte[] buf)
    {
        System.err.println("NavData packet received");
        int i = 0;
        for(byte b : buf)
        {
            System.err.print(Integer.toHexString((int)b) + " ");
            if(i % 10 == 0)
                System.err.println();
        }
        System.err.println();
        System.err.println();

        return null;
    }
}
