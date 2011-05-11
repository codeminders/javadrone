
package com.codeminders.ardroneui;

import java.io.IOException;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardroneui.controllers.*;
import com.codeminders.hidapi.*;

/**
 * Created by IntelliJ IDEA. User: bird Date: 5/6/11 Time: 4:50 PM To change
 * this template use File | Settings | File Templates.
 */
public class PS3Flight
{
    private static final long READ_UPDATE_DELAY_MS = 50L;
    private static final long CONNECT_TIMEOUT      = 3000L;

    static
    {
        System.loadLibrary("hidapi-jni");
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        readDevice();
    }

    private static void readDevice()
    {
        PS3Controller dev;
        try
        {
            final ARDrone drone = new ARDrone();
            drone.setCombinedYawMode(true);
            drone.addStatusChangeListener(new DroneStatusChangeListener() {

                @Override
                public void ready()
                {
                    try
                    {
                        drone.trim();
                        drone.setConfigOption("control:altitude_max", "1000");
                        drone.setConfigOption("control:euler_angle_max", "0.2");
                        drone.setConfigOption("control:control_vz_max", "2000.0");
                        drone.setConfigOption("control:control_yaw", "2.0");
                    } catch(IOException e)
                    {
                        drone.changeToErrorState(e);
                    }
                }
            });

            System.err.println("Connecting to the drone");
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            System.err.println("Connected to the drone");
            try
            {
                dev = findController();
                if(dev == null)
                {
                    System.err.println("No suitable controller found!");
                    listDevices();
                    return;
                }

                System.err.println("Gamepad controller found");

                try
                {
                    PS3ControllerState oldpad = null;
                    while(true)
                    {
                        PS3ControllerState pad = dev.read();
                        if(pad == null)
                            continue;
                        
                        PS3ControllerStateChange pad_change = new PS3ControllerStateChange(oldpad, pad);
                        oldpad = pad;

                        if(pad_change.isStartChanged() && pad_change.isStart())
                        {
                            System.err.println("Taking off");
                            drone.takeOff();
                        } else if(pad_change.isSelectChanged() && pad_change.isSelect())
                        {
                            System.err.println("Landing");
                            drone.land();
                        } else if(pad_change.isPSChanged() && pad_change.isPS())
                        {
                            System.err.println("Reseting");

                            drone.clearEmergencySignal();
                            drone.trim();
                        } else if(pad_change.isJoysticksChanged())
                        {
                            // Detecting if we need to move the drone

                            int leftX = pad.getLeftJoystickX();
                            int leftY = pad.getLeftJoystickY();

                            int rightX = pad.getRightJoystickX();
                            int rightY = pad.getRightJoystickY();

                            float left_right_tilt = 0f;
                            float front_back_tilt = 0f;
                            float vertical_speed = 0f;
                            float angular_speed = 0f;

                            if(leftX != 0)
                            {
                                left_right_tilt = ((float) leftX) / 128f;
                                System.err.println("Left-Right tilt: " + left_right_tilt);
                            }

                            if(leftY != 0)
                            {
                                front_back_tilt = ((float) leftY) / 128f;
                                System.err.println("Front-back tilt: " + front_back_tilt);
                            }

                            if(rightX != 0)
                            {
                                angular_speed = ((float) rightX) / 128f;
                                System.err.println("Angular speed: " + angular_speed);
                            }

                            if(rightY != 0)
                            {
                                vertical_speed = -1 * ((float) rightY) / 128f;
                                System.err.println("Vertical speed: " + vertical_speed);
                            }

                            if(leftX != 0 || leftY != 0 || rightX != 0 || rightY != 0)
                            {
                                drone.move(left_right_tilt, front_back_tilt, vertical_speed, angular_speed);
                            }
                        }

                        try
                        {
                            Thread.sleep(READ_UPDATE_DELAY_MS);
                        } catch(InterruptedException e)
                        {
                            // Ignore
                        }
                    }
                } finally
                {
                    dev.close();
                }
            } finally
            {
                drone.disconnect();
            }
        } catch(HIDDeviceNotFoundException hex)
        {
            hex.printStackTrace();
            listDevices();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

    private static PS3Controller findController() throws IOException
    {
        HIDDeviceInfo[] devs = HIDManager.listDevices();
        for(int i = 0; i < devs.length; i++)
        {
            if(AfterGlowController.isA(devs[i]))
                return new AfterGlowController(devs[i]);
            if(SonyPS3Controller.isA(devs[i]))
                return new SonyPS3Controller(devs[i]);
        }
        return null;
    }

    private static void listDevices()
    {
        String property = System.getProperty("java.library.path");
        System.err.println(property);

        try
        {
            HIDDeviceInfo[] devs = HIDManager.listDevices();
            System.err.println("Devices:\n\n");
            for(int i = 0; i < devs.length; i++)
            {
                System.err.println("" + i + ".\t" + devs[i]);
                System.err.println("---------------------------------------------\n");
            }
        } catch(IOException e)
        {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
