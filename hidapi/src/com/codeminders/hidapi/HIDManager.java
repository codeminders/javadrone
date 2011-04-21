package com.codeminders.hidapi;

import java.util.List;

public class HIDManager
{
    static native List<HIDDeviceInfo> listDevices();
}
