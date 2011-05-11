
package com.codeminders.ardrone;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.ardrone.commands.*;

public class ARDrone
{
    public enum State
    {
        DISCONNECTED, CONNECTING, BOOTSTRAP, DEMO, ERROR
    }

    private Logger                              log              = Logger.getLogger("ARDrone");

    private State                               state            = State.DISCONNECTED;
    private Object                              state_mutex      = new Object();

    private static final int                    NAVDATA_PORT     = 5554;
    private static final int                    VIDEO_PORT       = 5555;
    // private static final int CONTROL_PORT = 5559;

    private static byte[]                       DEFAULT_DRONE_IP = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };

    private InetAddress                         drone_addr;
    private DatagramSocket                      video_socket;
    private DatagramSocket                      cmd_socket;
    // private Socket control_socket;

    private PriorityBlockingQueue<DroneCommand> cmd_queue        = new PriorityBlockingQueue<DroneCommand>();
    private BlockingQueue<NavData>              navdata_queue    = new LinkedBlockingQueue<NavData>();

    private NavDataReader                       nav_data_reader;
    private CmdSender                           cmd_sender;

    private Thread                              nav_data_reader_thread;
    private Thread                              cmd_sending_thread;

    private boolean                             combinedYawMode  = true;

    private boolean                             emergencyMode    = true;
    private Object                              emergency_mutex  = new Object();

    private List<DroneStatusChangeListener>     status_listeners = new LinkedList<DroneStatusChangeListener>();

    public ARDrone() throws UnknownHostException
    {
        this(InetAddress.getByAddress(DEFAULT_DRONE_IP));
    }

    public ARDrone(InetAddress drone_addr)
    {
        this.drone_addr = drone_addr;
    }

    private void changeState(State newstate) throws IOException
    {
        if(newstate == State.ERROR)
            changeToErrorState(null);

        synchronized(state_mutex)
        {
            if(state != newstate)
            {
                log.fine("State changed from " + state + " to " + newstate);
                state = newstate;

                // We automatically switch to DEMO from bootstrap
                if(state == State.BOOTSTRAP)
                    sendDemoNavigationData();
            }
        }

        if(newstate == State.DEMO)
        {
            synchronized(status_listeners)
            {
                for(DroneStatusChangeListener l : status_listeners)
                    l.ready();
            }
        }
    }

    public void changeToErrorState(Exception ex)
    {
        synchronized(state_mutex)
        {
            try
            {
                if(state != State.DISCONNECTED)
                    doDisconnect();
            } catch(IOException e)
            {
                // Ignoring exceptions on disconnection
            }
            log.fine("State changed from " + state + " to " + State.ERROR + " with exception " + ex);
            state = State.ERROR;
        }
    }

    /**
     * Initiate drone connection procedure.
     * 
     * @throws IOException
     */
    public void connect() throws IOException
    {
        try
        {
            video_socket = new DatagramSocket(VIDEO_PORT);
            cmd_socket = new DatagramSocket();
            // control_socket = new Socket(drone_addr, CONTROL_PORT);

            cmd_sender = new CmdSender(cmd_queue, this, drone_addr, cmd_socket);
            cmd_sending_thread = new Thread(cmd_sender);
            cmd_sending_thread.start();

            nav_data_reader = new NavDataReader(this, drone_addr, NAVDATA_PORT);
            nav_data_reader_thread = new Thread(nav_data_reader);
            nav_data_reader_thread.start();

            changeState(State.CONNECTING);

        } catch(IOException ex)
        {
            changeToErrorState(ex);
            throw ex;
        }
    }

    /**
     * Synchronous connect. Try to connect to drone and get it into DEMO mode. 
     * Throw exception if this not succeeded within given timeout.
     * 
     * @param how_long
     * @throws IOException
     */
    public void syncConnect(long how_long) throws IOException
    {
        log.fine("connecting to drone");
        try
        {
            video_socket = new DatagramSocket(VIDEO_PORT);
            cmd_socket = new DatagramSocket();
            // control_socket = new Socket(drone_addr, CONTROL_PORT);

            cmd_sender = new CmdSender(cmd_queue, this, drone_addr, cmd_socket);
            cmd_sending_thread = new Thread(cmd_sender);
            cmd_sending_thread.start();

            nav_data_reader = new NavDataReader(this, drone_addr, NAVDATA_PORT);
            nav_data_reader_thread = new Thread(nav_data_reader);
            nav_data_reader_thread.start();
        } catch(IOException ex)
        {
            changeToErrorState(ex);
            throw ex;
        }

        log.fine("Waiting for status change");

        long since = System.currentTimeMillis();
        synchronized(state_mutex)
        {
            changeState(State.CONNECTING);
            while(true)
            {
                long p = Math.min(System.currentTimeMillis() - since, how_long);
                try
                {
                    state_mutex.wait(p);
                } catch(InterruptedException e)
                {
                    // Ignore
                }
                if((System.currentTimeMillis() - since) >= how_long)
                {
                    // Timeout, too late
                    throw new IOException("Timeout connecting to ARDrone");
                } else if(state == State.DEMO)
                {
                    return; // OK! We are now connected
                } else if(state == State.ERROR || state == State.DISCONNECTED)
                {
                    throw new IOException("Connection Error");
                }
            }
        }
    }

    public void disconnect() throws IOException
    {
        try
        {
            doDisconnect();
        } finally
        {
            changeState(State.DISCONNECTED);
        }
    }

    private void doDisconnect() throws IOException
    {
        cmd_queue.add(new QuitCommand());
        nav_data_reader.stop();
        cmd_socket.close();
        video_socket.close();

        // Only the following method can throw an exception.
        // We call it last, to ensure it won't prevent other
        // cleanup operations from being completed
        // control_socket.close();
    }

    public void trim() throws IOException
    {
        cmd_queue.add(new FlatTrimCommand());
    }

    public void takeOff() throws IOException
    {
        cmd_queue.add(new TakeOffCommand());
    }

    public void land() throws IOException
    {
        cmd_queue.add(new LandCommand());
    }

    public void sendEmergencySignal() throws IOException
    {
        synchronized(emergency_mutex)
        {
            if(!isEmergencyMode())
                cmd_queue.add(new EmergencyCommand());
        }
    }

    public void clearEmergencySignal() throws IOException
    {
        synchronized(emergency_mutex)
        {
            if(isEmergencyMode())
                cmd_queue.add(new EmergencyCommand());
        }
    }

    public void hover() throws IOException
    {
        cmd_queue.add(new HoverCommand());
    }

    public void setCombinedYawMode(boolean combinedYawMode)
    {
        this.combinedYawMode = combinedYawMode;
    }

    public boolean isCombinedYawMode()
    {
        return combinedYawMode;
    }

    public boolean isEmergencyMode()
    {
        return emergencyMode;
    }

    /**
     * Move the drone
     * 
     * @param left_right_tilt The left-right tilt (aka. "drone roll" or phi
     *            angle) argument is a percentage of the maximum inclination as
     *            configured here. A negative value makes the drone tilt to its
     *            left, thus flying leftward. A positive value makes the drone
     *            tilt to its right, thus flying rightward.
     * @param front_back_tilt The front-back tilt (aka. "drone pitch" or theta
     *            angle) argument is a percentage of the maximum inclination as
     *            configured here. A negative value makes the drone lower its
     *            nose, thus flying frontward. A positive value makes the drone
     *            raise its nose, thus flying backward. The drone translation
     *            speed in the horizontal plane depends on the environment and
     *            cannot be determined. With roll or pitch values set to 0, the
     *            drone will stay horizontal but continue sliding in the air
     *            because of its inertia. Only the air resistance will then make
     *            it stop.
     * @param vertical_speed The vertical speed (aka. "gaz") argument is a
     *            percentage of the maximum vertical speed as defined here. A
     *            positive value makes the drone rise in the air. A negative
     *            value makes it go down.
     * @param angular_speed The angular speed argument is a percentage of the
     *            maximum angular speed as defined here. A positive value makes
     *            the drone spin right; a negative value makes it spin left.
     * @throws IOException
     */
    public void move(float left_right_tilt, float front_back_tilt, float vertical_speed, float angular_speed)
            throws IOException
    {
        cmd_queue
                .add(new MoveCommand(combinedYawMode, left_right_tilt, front_back_tilt, vertical_speed, angular_speed));
    }

    public void sendAllNavigationData() throws IOException
    {
        setConfigOption("general:navdata_demo", "FALSE");
        cmd_queue.add(new ControlCommand(5, 0));
    }

    public void sendDemoNavigationData() throws IOException
    {
        setConfigOption("general:navdata_demo", "TRUE");
        cmd_queue.add(new ControlCommand(5, 0));
    }

    public void setConfigOption(String name, String value) throws IOException
    {
        cmd_queue.add(new ConfigureCommand(name, value));
    }

    public void playLED(int animation_no, float freq, int duration) throws IOException
    {
        cmd_queue.add(new PlayLEDCommand(animation_no, freq, duration));
    }

    public void playAnimation(int animation_no, int duration) throws IOException
    {
        cmd_queue.add(new PlayAnimationCommand(animation_no, duration));
    }

    // Callback used by receiver
    public void navDataReceived(NavData nd)
    {
        synchronized(emergency_mutex)
        {
            emergencyMode = nd.isEmergency();
        }

        try
        {
            synchronized(state_mutex)
            {
                if(state != State.BOOTSTRAP && nd.getMode() == NavData.Mode.BOOTSTRAP)
                {
                    changeState(State.BOOTSTRAP);
                } else if(state != State.DEMO && nd.getMode() == NavData.Mode.DEMO)
                {
                    changeState(State.DEMO);
                }

                if(nd.isCommunicationProblemOccurred())
                {
                    // 50ms communications watchdog has been triggered
                    cmd_queue.add(new KeepAliveCommand());
                }
            }
        } catch(IOException e)
        {
            log.log(Level.SEVERE, "Error changing the state", e);
        }

        if(state == State.DEMO)
        {
            navdata_queue.add(nd);
        }
    }

    public void addStatusChangeListener(DroneStatusChangeListener l)
    {
        synchronized(status_listeners)
        {
            status_listeners.add(l);
        }
    }

    public List<DroneStatusChangeListener> getStatusChangeListeners()
    {
        return status_listeners;
    }

}
