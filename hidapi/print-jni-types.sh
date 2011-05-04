#!/bin/sh

javap -private -s -classpath bin com.codeminders.hidapi.HIDDevice
javap -private -s -classpath bin com.codeminders.hidapi.HIDManager
javap -private -s -classpath bin com.codeminders.hidapi.HIDDeviceInfo
