package com.codeminders.ardrone.controllers.hid;

import java.io.IOException;

import com.codeminders.ardrone.controllers.Controller;
import com.codeminders.ardrone.controllers.ControllerData;
import com.codeminders.ardrone.controllers.GameControllerState;
import com.codeminders.ardrone.controllers.decoders.ControllerStateDecoder;
import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;
import com.codeminders.hidapi.HIDManager;

public abstract class HIDAPIController extends Controller {

    private byte[]           buf;
    HIDDevice                dev;
    
    ControllerStateDecoder decoder;
    
    public HIDAPIController(int vid, int pid, ControllerStateDecoder decoder, int readBufferSize) throws HIDDeviceNotFoundException, IOException {
        
        this.decoder = decoder;
        buf = new byte[readBufferSize];
        
        dev = HIDManager.getInstance().openById(vid, vid, null);
        if (null != dev) {
            dev.enableBlocking(); 
        } else {
            throw new HIDDeviceNotFoundException("Device not found");
        }
    }
    
    public abstract boolean isValid(ControllerData data) throws IOException;
    
    public HIDAPIController(ControllerStateDecoder decodder, int readBufferSize, HIDDeviceInfo hidDeviceInfo) throws IOException {
       
        this.decoder = decodder;
        buf = new byte[readBufferSize];
        
        dev = hidDeviceInfo.open();
        if (null != dev) {
           dev.enableBlocking();
       } else {
           throw new HIDDeviceNotFoundException("Device not found");
       }
    }
    
    public GameControllerState read() throws IOException {
        ControllerData data = readDataFromDevice();
        if (isValid(data)) {
            return decoder.decodeState(data);
        } else {
            return null;
        }
    }
    
    @Override
    public synchronized void close() throws IOException {
       if (dev != null) {
           dev.close();
       }
    }
    
    @Override
    public String getManufacturerString() {
        try {
            return (dev != null)? dev.getManufacturerString() : "device not avalible";
        } catch (IOException e) {
          return "device not avalible";
        }
    }

    @Override
    public String getProductString() {
        try {
            return (dev != null)? dev.getProductString() : "device not avalible";
        } catch (IOException e) {
          return "device not avalible";
        }
    }

    private synchronized ControllerData readDataFromDevice() throws IOException {
        int n = dev.read(buf);
        return new ControllerData(buf, n);
    }

}
