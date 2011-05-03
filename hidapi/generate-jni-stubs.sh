#!/bin/sh

javah -d jni-stubs -classpath bin com.codeminders.hidapi.HIDManager
javah -d jni-stubs -classpath bin com.codeminders.hidapi.HIDDevice
javah -d jni-stubs -classpath bin com.codeminders.hidapi.HIDDeviceInfo
