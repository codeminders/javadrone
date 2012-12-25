package com.codeminders.ardrone;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

import com.codeminders.ardrone.AssignableControl.ControllerButton;
import com.codeminders.ardrone.controllers.Controller;
import com.codeminders.ardrone.controllers.ControllerStateChange;
import com.codeminders.ardrone.controllers.GameControllerState;

public class ControllerThread extends Thread {
    ARDrone drone;
    Controller controller;
    private final ControlMap controlMap = new ControlMap();
    private float controlThreshhold = 0.5f;
    
    Object lock = new Object();
    boolean done = false;
    
    private static final long READ_UPDATE_DELAY_MS = 5L;
    private static final long FINIS_TIMEOUT = 2000; // 2 sec.
    
    private static final String TAG = ControllerThread.class.getSimpleName();
    
    private final AtomicBoolean flipSticks = new AtomicBoolean(false);
    
    public ControllerThread(ARDrone drone, Controller controller) {
        super();
        this.drone = drone;
        this.controller = controller;
    }


    @Override
    public void run() {
        try
        {
            GameControllerState oldpad = null;
            while(!done)
            {
                GameControllerState pad = controller.read();
                if (null == drone || null == pad) {
                    continue;
                }
                
                ControllerStateChange pad_change = new ControllerStateChange(oldpad, pad);
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

                    if(Math.abs(((float) leftX) / 128f) > controlThreshhold)
                    {
                        left_right_tilt = ((float) leftX) / 128f;
                    }

                    if(Math.abs(((float) leftY) / 128f) > controlThreshhold)
                    {
                        front_back_tilt = ((float) leftY) / 128f;
                    }

                    if(Math.abs(((float) rightX) / 128f) > controlThreshhold)
                    {
                        angular_speed = ((float) rightX) / 128f;
                    }

                    if(Math.abs(-1 * ((float) rightY) / 128f) > controlThreshhold)
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
            Log.e(TAG, "Faliled read data from controller" , e);
        }
        finally
        {
            try {
                drone.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Faliled to disconnect from drone" , e);
            }
            
            synchronized (lock) {
                lock.notify();
            }
        }
    }


    public void setControlThreshhold(float controlThreshhold) {
        this.controlThreshhold = controlThreshhold;
    }
   
    public void setDrone(ARDrone drone) {
        this.drone = drone;
    }
    
    public void finish()
    {
        done = true;
        try {
            controller.close();
        } catch (IOException ex) {
            Log.e(TAG, "Closing controller connection" , ex);
        }
        
        if (isAlive()) {
            synchronized (lock) {
                try {
                    lock.wait(FINIS_TIMEOUT);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Finish process is interrupted" , e);
                }
            }
        }
    }
    
}
