
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class ARDrone implements Runnable
{
    private static final int                    CMD_PORT         = 5556;
    private static final int                    NAVDATA_PORT     = 5554;
    private static final int                    VIDEO_PORT       = 5555;
    private static final int                    CONTROL_PORT     = 5559;

    private static byte[]                       DEFAULT_DRONE_IP = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };

    private InetAddress                         drone_addr;
    private DatagramSocket                      navdata_socket;
    private DatagramSocket                      video_socket;
    private DatagramSocket                      cmd_socket;
    private Socket                              control_socket;

    private PriorityBlockingQueue<DroneCommand> cmd_queue        = new PriorityBlockingQueue<DroneCommand>();
    private Thread                              cmd_sending_thread;

    public ARDrone() throws UnknownHostException
    {
        this(InetAddress.getByAddress(DEFAULT_DRONE_IP));
    }

    public ARDrone(InetAddress drone_addr)
    {
        this.drone_addr = drone_addr;
        cmd_sending_thread = new Thread(this);
    }

    public void connect() throws IOException
    {
        navdata_socket = new DatagramSocket(NAVDATA_PORT);
        video_socket = new DatagramSocket(VIDEO_PORT);
        cmd_socket = new DatagramSocket();
        control_socket = new Socket(drone_addr, CONTROL_PORT);
        cmd_sending_thread.start();
    }

    public void disconnect() throws IOException
    {
        cmd_queue.add(new QuitCommand());
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

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                DroneCommand c = cmd_queue.take();
                if(c instanceof QuitCommand)
                {
                    // Terminating
                    break;
                }

                if(c instanceof ATCommand)
                {
                    ATCommand cmd = (ATCommand) c;
                    byte[] pdata = cmd.getPacket(0);
                    DatagramPacket p = new DatagramPacket(pdata, pdata.length, drone_addr, CMD_PORT);
                    cmd_socket.send(p);
                }
            } catch(InterruptedException e)
            {
                // ignoring
            } catch(IOException e)
            {
                // TODO Auto-generated catch block
                // TODO: handle error
                e.printStackTrace();
            }
        }
    }

}
