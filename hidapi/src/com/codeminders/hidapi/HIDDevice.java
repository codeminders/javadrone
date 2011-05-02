
package com.codeminders.hidapi;

import java.io.IOException;

/**
 * Instance of this class represents an open HID device.
 * 
 * @see HIDDeviceInfo.open()
 * @author lord
 * 
 */
public class HIDDevice
{
    protected long peer;

    protected HIDDevice(long peer)
    {
        this.peer = peer;
    }
    
    protected void finalize()
    {
        // It is important to call close() if user forgot to do so,
        // since it frees pointer to internal data structure.
        try
        {
            close();
        } catch(IOException e)
        {
            // Ignoring close exception in finalizer
        }
    }

    /**
     * Close open device. Multiple calls allowed - id device was already closed
     * no exception will be thrown.
     * 
     * @throws IOException
     */
    public native void close() throws IOException;

    public native int write(byte[] data) throws IOException;

    public native int read(byte[] buf) throws IOException;

    public native void enableBlocking() throws IOException;

    public native void disableBlocking() throws IOException;

    public native int sendFeatureReport(byte[] data) throws IOException;

    public native int getFeatureReport(byte[] buf) throws IOException;

    public native String getManufacturerString() throws IOException;

    public native String getProductString() throws IOException;

    public native String getSerialNumberString() throws IOException;

    public native String getIndexedString(int string_index) throws IOException;
}
