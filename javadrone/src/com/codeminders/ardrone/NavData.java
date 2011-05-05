package com.codeminders.ardrone;

public class NavData
{
    protected boolean is_trim_received;


    public static NavData createFromData(byte[] buf)
    {
        System.err.println("NavData packet received. len: " + buf.length);
        int offset = 0;

        int header = byteArrayToInt(buf, offset);
        offset += 4;
        System.err.print("Header: 0x" + Integer.toHexString(header) + " ");

        int status = byteArrayToInt(buf, offset);
        offset += 4;
        System.err.print("Status: 0x" + Integer.toHexString(status) + " ");

        int sequence = byteArrayToInt(buf, offset);
        offset += 4;
        System.err.print("Sequence: 0x" + Integer.toHexString(sequence) + " ");

        int vision = byteArrayToInt(buf, offset);
        offset += 4;
        System.err.print("Vision: 0x" + Integer.toHexString(vision) + " ");

        System.err.println();


        int i = 0;
        for(byte b : buf)
        {
            System.err.print("0x" + Integer.toHexString((int)b) + " ");
            if(++i % 8 == 0)
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
        for (int i = 3; i >= 0; i--) {
            int shift = i * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The short
     */
    public static int byteArrayToShort(byte[] b, int offset)
    {
        return b[offset]*256 + b[offset+1];
    }

}
