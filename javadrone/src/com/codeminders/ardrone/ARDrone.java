
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;

public class ARDrone
{
    private static final int CMD_PORT         = 5556;
    private static final int NAVDATA_PORT     = 5554;
    private static final int VIDEO_PORT       = 5555;
    private static final int CONTROL_PORT     = 5559;

    private static byte[]    DEFAULT_DRONE_IP = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };

    private InetAddress      drone_addr;
    private DatagramSocket   navdata_socket;
    private DatagramSocket   video_socket;
    private DatagramSocket   cmd_socket;
    private Socket           control_socket;

    public ARDrone() throws UnknownHostException
    {
        this(InetAddress.getByAddress(DEFAULT_DRONE_IP));
    }

    public ARDrone(InetAddress drone_addr)
    {
        this.drone_addr = drone_addr;
    }

    public void connect() throws IOException
    {
        navdata_socket = new DatagramSocket(NAVDATA_PORT);
        video_socket = new DatagramSocket(VIDEO_PORT);
        cmd_socket = new DatagramSocket();
        control_socket = new Socket(drone_addr, CONTROL_PORT);
    }

    public void disconnect() throws IOException
    {
        cmd_socket.close();
        video_socket.close();
        navdata_socket.close();
        control_socket.close();
    }

    public void trim() throws IOException
    {
    }

    public void takeOff() throws IOException
    {
    }

    public void land() throws IOException
    {
    }

    public void sendEmergencySignal() throws IOException
    {
    }

    public void clearEmergencySignal() throws IOException
    {
    }

    public void hover() throws IOException
    {
    }

    public void setCombinedYawMode(boolean v)
    {
    }

    public boolean isCombinedYawMode()
    {
        return false;
    }

    public void set(float phi, float theta, float gaz, float yaw) throws IOException
    {
    }

    public void sendAllNavigationData() throws IOException
    {
    }

    public void sendADemoNavigationData() throws IOException
    {
    }

    public void setConfigOption(String name, String value) throws IOException
    {
    }

    public void playLED(int animation_no, float freq, int duration) throws IOException
    {
    }

    public void playAnimation(int animation_no, int duration) throws IOException
    {
    }

}
