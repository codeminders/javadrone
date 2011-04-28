package com.codeminders.ardrone;

import java.io.IOException;

public interface ARDrone
{
    public void connect() throws IOException;

    public void trim() throws IOException;

    public void takeOff() throws IOException;
    public void land() throws IOException;
    
    public void sendEmergencySignal() throws IOException;
    public void clearEmergencySignal() throws IOException;

    public void hover() throws IOException;
    
    public void setCombinedYawMode(boolean v);
    public boolean isCombinedYawMode();
    
    public void set(float phi, float theta, float gaz, float yaw) throws IOException;

    public void sendAllNavigationData() throws IOException;
    public void sendADemoNavigationData() throws IOException; 
    
    public void setConfigOption(String name, String value) throws IOException;
    
    public void playLED(int animation_no, float freq, int duration) throws IOException;

    public void playAnimation(int animation_no, int duration) throws IOException;

}
