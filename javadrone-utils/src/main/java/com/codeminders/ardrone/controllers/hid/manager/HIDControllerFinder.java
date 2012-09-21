package com.codeminders.ardrone.controllers.hid.manager;

import java.io.IOException;

import com.codeminders.ardrone.controllers.Controller;
import com.codeminders.ardrone.controllers.hid.HIDAPIAfterGlowController;
import com.codeminders.ardrone.controllers.hid.HIDAPIMotioninJoyVirtualController;
import com.codeminders.ardrone.controllers.hid.HIDAPISonyPS3Controller;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public class HIDControllerFinder {
    
    public static Controller findController() throws IOException
    {

        HIDDeviceInfo[] devs = HIDManager.getInstance().listDevices();
        if (null != devs) {
            for(int i = 0; i < devs.length; i++)
            {
                if(HIDAPIAfterGlowController.isA(devs[i]))
                    return new HIDAPIAfterGlowController(devs[i]);
                if(HIDAPISonyPS3Controller.isA(devs[i]))
                    return new HIDAPISonyPS3Controller(devs[i]);
                if(HIDAPIMotioninJoyVirtualController.isA(devs[i]))
                    return new HIDAPIMotioninJoyVirtualController(devs[i]);
            }
        }
        return null;
    }
}
