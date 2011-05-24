
package com.codeminders.ardrone;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.ardrone.commands.*;

public class ARDrone
{
    public enum State
    {
        DISCONNECTED, CONNECTING, BOOTSTRAP, DEMO, ERROR, TAKING_OFF, LANDING
    }

    public enum VideoChannel
    {
        HORIZONTAL_ONLY, VERTICAL_ONLY, VERTICAL_IN_HORIZONTAL, HORIZONTAL_IN_VERTICAL
    }

    public enum Animation
    {
        PHI_M30_DEG(0), PHI_30_DEG(1), THETA_M30_DEG(2), THETA_30_DEG(3), THETA_20DEG_YAW_200DEG(4), THETA_20DEG_YAW_M200DEG(
                5), TURNAROUND(6), TURNAROUND_GODOWN(7), YAW_SHAKE(8), YAW_DANCE(9), PHI_DANCE(10), THETA_DANCE(11), VZ_DANCE(
                12), WAVE(13), PHI_THETA_MIXED(14), DOUBLE_PHI_THETA_MIXED(15), ANIM_MAYDAY(16);

        private int value;

        private Animation(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    public enum LED
    {
        BLINK_GREEN_RED(0), BLINK_GREEN(1), BLINK_RED(2), BLINK_ORANGE(3), SNAKE_GREEN_RED(4), FIRE(5), STANDARD(6), RED(
                7), GREEN(8), RED_SNAKE(9), BLANK(10), RIGHT_MISSILE(11), LEFT_MISSILE(12), DOUBLE_MISSILE(13), FRONT_LEFT_GREEN_OTHERS_RED(
                14), FRONT_RIGHT_GREEN_OTHERS_RED(15), REAR_RIGHT_GREEN_OTHERS_RED(16), REAR_LEFT_GREEN_OTHERS_RED(17), LEFT_GREEN_RIGHT_RED(
                18), LEFT_RED_RIGHT_GREEN(19), BLINK_STANDARD(20);

        private int value;

        private LED(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    public enum ConfigOption
    {
        ACCS_OFFSET("control:accs_offset"), ACCS_GAINS("control:accs_gains"), GYROS_OFFSET("control:gyros_offset"), GYROS_GAINS(
                "control:gyros_gains"), GYROS110_OFFSET("control:gyros110_offset"), GYROS110_GAINS(
                "control:gyros110_gains"), GYRO_OFFSET_THR_X("control:gyro_offset_thr_x"), GYRO_OFFSET_THR_Y(
                "control:gyro_offset_thr_y"), GYRO_OFFSET_THR_Z("control:gyro_offset_thr_z"), PWM_REF_GYROS(
                "control:pwm_ref_gyros"), CONTROL_LEVEL("control:control_level"), SHIELD_ENABLE("control:shield_enable"), EULER_ANGLE_MAX(
                "control:euler_angle_max"), ALTITUDE_MAX("control:altitude_max"), ALTITUDE_MIN("control:altitude_min"), CONTROL_TRIM_Z(
                "control:control_trim_z"), CONTROL_IPHONE_TILT("control:control_iphone_tilt"), CONTROL_VZ_MAX(
                "control:control_vz_max"), CONTROL_YAW("control:control_yaw"), OUTDOOR("control:outdoor"), FLIGHT_WITHOUT_SHELL(
                "control:flight_without_shell"), BRUSHLESS("control:brushless"), AUTONOMOUS_FLIGHT(
                "control:autonomous_flight"), MANUAL_TRIM("control:manual_trim"), INDOOR_EULER_ANGLE_MAX(
                "control:indoor_euler_angle_max"), INDOOR_CONTROL_VZ_MAX("control:indoor_control_vz_max"), INDOOR_CONTROL_YAW(
                "control:indoor_control_yaw"), OUTDOOR_EULER_ANGLE_MAX("control:outdoor_euler_angle_max"), OUTDOOR_CONTROL_VZ_MAX(
                "control:outdoor_control_vz_max"), OUTDOOR_CONTROL_YAW("outdoor_control:control_yaw");

        private String value;

        private ConfigOption(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    private Logger                          log               = Logger.getLogger(getClass().getName());

    private static final int                CMD_QUEUE_SIZE    = 64;
    private State                           state             = State.DISCONNECTED;
    private Object                          state_mutex       = new Object();

    private static final int                NAVDATA_PORT      = 5554;
    private static final int                VIDEO_PORT        = 5555;
    // private static final int CONTROL_PORT = 5559;

    private static byte[]                   DEFAULT_DRONE_IP  = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };

    private InetAddress                     drone_addr;
    private DatagramSocket                  cmd_socket;
    // private Socket control_socket;

    private CommandQueue                    cmd_queue         = new CommandQueue(CMD_QUEUE_SIZE);

    private NavDataReader                   nav_data_reader;
    private VideoReader                     video_reader;
    private CommandSender                   cmd_sender;

    private Thread                          nav_data_reader_thread;
    private Thread                          cmd_sending_thread;
    private Thread                          video_reader_thread;

    private boolean                         combinedYawMode   = true;

    private boolean                         emergencyMode     = true;
    private Object                          emergency_mutex   = new Object();

    private List<DroneStatusChangeListener> status_listeners  = new LinkedList<DroneStatusChangeListener>();
    private List<DroneVideoListener>        image_listeners   = new LinkedList<DroneVideoListener>();
    private List<NavDataListener>           navdata_listeners = new LinkedList<NavDataListener>();

    public ARDrone() throws UnknownHostException
    {
        this(InetAddress.getByAddress(DEFAULT_DRONE_IP));
    }

    public ARDrone(InetAddress drone_addr)
    {
        this.drone_addr = drone_addr;
    }

    public void addImageListener(DroneVideoListener l)
    {
        synchronized(image_listeners)
        {
            image_listeners.add(l);
        }
    }

    public void addStatusChangeListener(DroneStatusChangeListener l)
    {
        synchronized(status_listeners)
        {
            status_listeners.add(l);
        }
    }

    public void addNavDataListener(NavDataListener l)
    {
        synchronized(navdata_listeners)
        {
            navdata_listeners.add(l);
        }
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

                state_mutex.notifyAll();

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
            log.log(Level.FINER, "Stack trace", ex);
            state = State.ERROR;
            state_mutex.notifyAll();
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

    /**
     * Initiate drone connection procedure.
     * 
     * @throws IOException
     */
    public void connect() throws IOException
    {
        try
        {
            cmd_socket = new DatagramSocket();
            // control_socket = new Socket(drone_addr, CONTROL_PORT);

            cmd_sender = new CommandSender(cmd_queue, this, drone_addr, cmd_socket);
            cmd_sending_thread = new Thread(cmd_sender);
            cmd_sending_thread.start();

            nav_data_reader = new NavDataReader(this, drone_addr, NAVDATA_PORT);
            nav_data_reader_thread = new Thread(nav_data_reader);
            nav_data_reader_thread.start();

            video_reader = new VideoReader(this, drone_addr, VIDEO_PORT);
            video_reader_thread = new Thread(video_reader);
            video_reader_thread.start();

            changeState(State.CONNECTING);

        } catch(IOException ex)
        {
            changeToErrorState(ex);
            throw ex;
        }
    }

    public void disableAutomaticVideoBitrate() throws IOException
    {
        cmd_queue.add(new ConfigureCommand("video:bitrate_control_mode", "0"));
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
        if(cmd_queue != null)
            cmd_queue.add(new QuitCommand());

        if(nav_data_reader != null)
            nav_data_reader.stop();

        if(video_reader != null)
            video_reader.stop();

        if(cmd_socket != null)
            cmd_socket.close();

        // Only the following method can throw an exception.
        // We call it last, to ensure it won't prevent other
        // cleanup operations from being completed
        // control_socket.close();
    }

    /**
     * Enables the automatic bitrate control of the video stream. Enabling this
     * configuration will reduce the bandwith used by the video stream under bad
     * Wi-Fi conditions, reducing the commands latency. Note : Before enabling
     * this config, make sure that your video decoder is able to handle the
     * variable bitrate mode !
     * 
     * @throws IOException
     */
    public void enableAutomaticVideoBitrate() throws IOException
    {
        cmd_queue.add(new ConfigureCommand("video:bitrate_control_mode", "1"));
    }

    public List<DroneVideoListener> getImageListeners()
    {
        return image_listeners;
    }

    public List<DroneStatusChangeListener> getStatusChangeListeners()
    {
        return status_listeners;
    }

    public List<NavDataListener> getNavDataListeners()
    {
        return navdata_listeners;
    }

    public void hover() throws IOException
    {
        cmd_queue.add(new HoverCommand());
    }

    public boolean isCombinedYawMode()
    {
        return combinedYawMode;
    }

    public boolean isEmergencyMode()
    {
        return emergencyMode;
    }

    public void land() throws IOException
    {
        // TODO: Review of possible race condition
        cmd_queue.add(new LandCommand());
        changeState(State.LANDING);
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
                if((state == State.TAKING_OFF && nd.isFlying()) || (state == State.LANDING && !nd.isFlying()))
                {
                    cmd_queue.clear(); // Maybe we should just remove
                                       // LAND/TAKEOFF comand
                                       // instead of nuking whole queue?
                }

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
            synchronized(navdata_listeners)
            {
                for(NavDataListener l : navdata_listeners)
                    l.navDataReceived(nd);
            }
        }
    }

    public void playAnimation(int animation_no, int duration) throws IOException
    {
        cmd_queue.add(new PlayAnimationCommand(animation_no, duration));
    }

    public void playAnimation(Animation animation, int duration) throws IOException
    {
        cmd_queue.add(new PlayAnimationCommand(animation.getValue(), duration));
    }

    public void playLED(int animation_no, float freq, int duration) throws IOException
    {
        cmd_queue.add(new PlayLEDCommand(animation_no, freq, duration));
    }

    public void playLED(LED animation, float freq, int duration) throws IOException
    {
        cmd_queue.add(new PlayLEDCommand(animation.getValue(), freq, duration));
    }

    public void selectVideoChannel(VideoChannel c) throws IOException
    {
        /*
         * Current implementation supports 4 different channels : -
         * ARDRONE_VIDEO_CHANNEL_HORI - ARDRONE_VIDEO_CHANNEL_VERT -
         * ARDRONE_VIDEO_CHANNEL_LARGE_HORI_SMALL_VERT -
         * ARDRONE_VIDEO_CHANNEL_LARGE_VERT_SMALL_HORI
         * 
         * AT command example : AT*CONFIG=605,"video:video_channel","2"
         */

        String s;
        switch(c)
        {
        case HORIZONTAL_ONLY: // ARDRONE_VIDEO_CHANNEL_HORI
            s = "0";
            break;

        case VERTICAL_ONLY: // ARDRONE_VIDEO_CHANNEL_VERT
            s = "1";
            break;

        case VERTICAL_IN_HORIZONTAL: // ARDRONE_VIDEO_CHANNEL_LARGE_HORI_SMALL_VERT
            s = "2";
            break;

        case HORIZONTAL_IN_VERTICAL: // ARDRONE_VIDEO_CHANNEL_LARGE_VERT_SMALL_HORI
            s = "3";
            break;
        default:
            assert (false);
            return;
        }

        cmd_queue.add(new ConfigureCommand("video:video_channel", s));
    }

    public void sendAllNavigationData() throws IOException
    {
        setConfigOption("general:navdata_demo", "FALSE");
    }

    public void sendDemoNavigationData() throws IOException
    {
        setConfigOption("general:navdata_demo", "TRUE");
    }

    public void sendEmergencySignal() throws IOException
    {
        synchronized(emergency_mutex)
        {
            if(!isEmergencyMode())
                cmd_queue.add(new EmergencyCommand());
        }
    }

    public void setCombinedYawMode(boolean combinedYawMode)
    {
        this.combinedYawMode = combinedYawMode;
    }

    public void setConfigOption(String name, String value) throws IOException
    {
        cmd_queue.add(new ConfigureCommand(name, value));
        cmd_queue.add(new ControlCommand(5, 0));
    }

    public void setConfigOption(ConfigOption option, String value) throws IOException
    {
        cmd_queue.add(new ConfigureCommand(option.getValue(), value));
        cmd_queue.add(new ControlCommand(5, 0));
    }

    public void takeOff() throws IOException
    {
        // TODO: review for possible race condition
        cmd_queue.add(new TakeOffCommand());
        changeState(State.TAKING_OFF);
    }

    public void trim() throws IOException
    {
        cmd_queue.add(new FlatTrimCommand());
    }

    // Callback used by VideoReciver
    public void videoFrameReceived(BufferedImage image)
    {
        synchronized(image_listeners)
        {
            for(DroneVideoListener l : image_listeners)
                l.frameReceived(image);
        }
    }

    /**
     * Wait for drone to switch to demo mode. Throw exception if this not
     * succeeded within given timeout. Should be called right after connect().
     * 
     * This is a convenience function. Another way to achieve the same result is
     * using status change callback.
     * 
     * @param how_long
     * @throws IOException
     */
    public void waitForReady(long how_long) throws IOException
    {
        long since = System.currentTimeMillis();
        synchronized(state_mutex)
        {
            while(true)
            {
                if((System.currentTimeMillis() - since) >= how_long)
                {
                    try
                    {
                        disconnect();
                    } catch(IOException e)
                    {
                    }
                    // Timeout, too late
                    throw new IOException("Timeout connecting to ARDrone");
                } else if(state == State.DEMO)
                {
                    return; // OK! We are now connected
                } else if(state == State.ERROR || state == State.DISCONNECTED)
                {
                    throw new IOException("Connection Error");
                }

                long p = Math.min(how_long - (System.currentTimeMillis() - since), how_long);
                if(p > 0)
                {
                    try
                    {
                        state_mutex.wait(p);
                    } catch(InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
        }
    }

}
