
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
    public native void close() throws IOException;

    public native int write(byte[] data) throws IOException;

    public native int read(byte[] buf) throws IOException;

    public native void enableBlocking();

    public native void disableBlocking();

    public native int sendFeatureReport(byte[] data) throws IOException;

    public native int getFeatureReport(byte[] buf) throws IOException;

    public native String getManufacturerString() throws IOException;

    public native String getProductString() throws IOException;

    public native String getSerialNumberString() throws IOException;

    public native String getIndexedString(int string_index) throws IOException;

    public native String getLastError() throws IOException;

}
