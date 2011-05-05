package com.codeminders.ardrone;

public class NavData
{
    protected boolean is_trim_received;


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


    /**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b)
    {
        return byteArrayToInt(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b, int offset)
    {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

}
