package com.codeminders.ardrone;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codeminders.ardrone.AssignableControl.ControllerButton;
import com.codeminders.ardrone.controller.PS3Controller;
import com.codeminders.ardrone.controller.PS3ControllerState;
import com.codeminders.ardrone.controller.PS3ControllerStateChange;

public class ControllerThread extends Thread{
    ARDrone drone;
    PS3Controller controller;
    private final ControlMap controlMap = new ControlMap();
    private static float CONTROL_THRESHOLD = 0.5f;
    private static final long READ_UPDATE_DELAY_MS = 5L;
    
    private final AtomicBoolean flipSticks = new AtomicBoolean(false);
    
    public ControllerThread(ARDrone drone, PS3Controller controller) {
        super();
        this.drone = drone;
        this.controller = controller;
    }


    @Override
    public void run() {
        try
        {
            PS3ControllerState oldpad = null;
            while(true)
            {
                PS3ControllerState pad = controller.read();
                PS3ControllerStateChange pad_change = new PS3ControllerStateChange(oldpad, pad);
                oldpad = pad;

                if(pad_change.isStartChanged() && pad_change.isStart())
                {
                    controlMap.sendCommand(drone, ControllerButton.START);
                }
                if(pad_change.isSelectChanged() && pad_change.isSelect())
                {
                    controlMap.sendCommand(drone, ControllerButton.SELECT);
                }
                if(pad_change.isPSChanged() && pad_change.isPS())
                {
                    controlMap.sendCommand(drone, ControllerButton.PS);
                }
                if(pad_change.isTriangleChanged() && pad_change.isTriangle())
                {
                    controlMap.sendCommand(drone, ControllerButton.TRIANGLE);
                }
                if(pad_change.isCrossChanged() && pad_change.isCross())
                {
                    controlMap.sendCommand(drone, ControllerButton.CROSS);
                }
                if(pad_change.isSquareChanged() && pad_change.isSquare())
                {
                    controlMap.sendCommand(drone, ControllerButton.SQUARE);
                }
                if(pad_change.isCircleChanged() && pad_change.isCircle())
                {
                    controlMap.sendCommand(drone, ControllerButton.CIRCLE);
                }
                if(pad_change.isL1Changed() && pad_change.isL1())
                {
                    controlMap.sendCommand(drone, ControllerButton.L1);
                }
                if(pad_change.isR1Changed() && pad_change.isR1())
                {
                    controlMap.sendCommand(drone, ControllerButton.R1);
                }
                if(pad_change.isL2Changed() && pad_change.isL2())
                {
                    controlMap.sendCommand(drone, ControllerButton.L2);
                }
                if(pad_change.isR2Changed() && pad_change.isR2())
                {
                    controlMap.sendCommand(drone, ControllerButton.R2);
                }
                
                    int leftX = pad.getLeftJoystickX();
                    int leftY = pad.getLeftJoystickY();

                    int rightX = pad.getRightJoystickX();
                    int rightY = pad.getRightJoystickY();

                    float left_right_tilt = 0f;
                    float front_back_tilt = 0f;
                    float vertical_speed = 0f;
                    float angular_speed = 0f;

                    if(Math.abs(((float) leftX) / 128f) > CONTROL_THRESHOLD)
                    {
                        left_right_tilt = ((float) leftX) / 128f;
                    }

                    if(Math.abs(((float) leftY) / 128f) > CONTROL_THRESHOLD)
                    {
                        front_back_tilt = ((float) leftY) / 128f;
                    }

                    if(Math.abs(((float) rightX) / 128f) > CONTROL_THRESHOLD)
                    {
                        angular_speed = ((float) rightX) / 128f;
                    }

                    if(Math.abs(-1 * ((float) rightY) / 128f) > CONTROL_THRESHOLD)
                    {
                        vertical_speed = -1 * ((float) rightY) / 128f;
                    }

                    if(left_right_tilt != 0 || front_back_tilt != 0 || vertical_speed != 0 || angular_speed != 0)
                    {
                        if(flipSticks.get())
                        {
                            drone.move(angular_speed, -1 * vertical_speed, -1 * front_back_tilt, left_right_tilt);
                        }
                        else
                        {
                            drone.move(left_right_tilt, front_back_tilt, vertical_speed, angular_speed);

                        }
                    }
                    else
                    {
                        drone.hover();
                    }

                try
                {
                    Thread.sleep(READ_UPDATE_DELAY_MS);
                }
                catch(InterruptedException e)
                {
                    // Ignore
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            try {
                drone.disconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    
}
