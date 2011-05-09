
package com.codeminders.ardroneui.controllers;

import java.io.IOException;

import com.codeminders.hidapi.*;

/**
 * "Afterglow" controller for PS3
 * 
 * @author lord
 * 
 */
public class AfterGlowController
{
    static final int VENDOR_ID  = 3695;
    static final int PRODUCT_ID = 25346;

    public static HIDDevice open() throws HIDDeviceNotFoundException, IOException
    {
        return HIDManager.openById(VENDOR_ID, PRODUCT_ID, null);
    }
}
