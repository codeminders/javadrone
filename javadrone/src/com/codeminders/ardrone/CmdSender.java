
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.PriorityBlockingQueue;

public class CmdSender implements Runnable
{
    private static final int                    CMD_PORT = 5556;

    private PriorityBlockingQueue<DroneCommand> cmd_queue;
    private ARDrone                             drone;
    private InetAddress                         drone_addr;
    private DatagramSocket                      cmd_socket;

    public CmdSender(PriorityBlockingQueue<DroneCommand> cmd_queue, ARDrone drone, InetAddress drone_addr,
            DatagramSocket cmd_socket)
    {
        super();
        this.cmd_queue = cmd_queue;
        this.drone = drone;
        this.drone_addr = drone_addr;
        this.cmd_socket = cmd_socket;
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
                    byte[] pdata = cmd.getPacket();
                    DatagramPacket p = new DatagramPacket(pdata, pdata.length, drone_addr, CMD_PORT);
                    cmd_socket.send(p);
                }
            } catch(InterruptedException e)
            {
                // ignoring
            } catch(IOException e)
            {
                drone.changeToErrorState(e);
                break;
            }
        }
    }

}
