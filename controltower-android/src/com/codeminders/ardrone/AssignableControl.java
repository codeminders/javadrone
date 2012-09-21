package com.codeminders.ardrone;

import java.io.IOException;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.Animation;
import com.codeminders.ardrone.ARDrone.LED;
import com.codeminders.ardrone.ARDrone.VideoChannel;


/**
 * This class represents one control mapping for a button and at the same
 * time implements all control logic for button presses.
 * @author normenhansen
 */
public class AssignableControl {

    public enum Command {

        TAKEOFF, LAND, TRIM, CLEAR_EMERGENCY, PLAY_ANIMATION, PLAY_LED, RESET,
        VIDEO_CYCLE, FRONTAL_CAM, BOTTOM_CAM, BOTTOM_CAM_SMALL, FRONTAL_CAM_SMALL, TAKE_SNAPSHOT, RECORD_VIDEO
    }

    public enum ControllerButton {

        PS, SELECT, START, LEFT_STICK, RIGHT_STICK, TRIANGLE, CIRCLE, CROSS, SQUARE, L1, L2, R1, R2
    }

    public enum ControllerAxis {

        LEFT_X, LEFT_Y, RIGHT_X, RIGHT_Y
    }

    public enum DroneAxis {

        FRONT_BACK, LEFT_RIGHT, UP_DOWN, ROTATE
    }
    private ControllerButton button;
    private Command command;
    private Animation anim;
    private LED led;
    private ControllerAxis controlAxis;
    private DroneAxis droneAxis;
    private float frequency;
    private int duration;
    private int delay;
    
    private static final VideoChannel[] VIDEO_CYCLE = {VideoChannel.HORIZONTAL_ONLY,
        VideoChannel.VERTICAL_ONLY, VideoChannel.VERTICAL_IN_HORIZONTAL, VideoChannel.HORIZONTAL_IN_VERTICAL};
    private int video_index = 0;

    public AssignableControl(ControllerButton button, Command command, int delay) {
        this.command = command;
        this.delay = delay;
    }

    /**
     * Sends the command to the supplied drone
     * @param drone
     * @throws IOException 
     */
    public void sendToDrone(final ARDrone drone) throws IOException {
        switch (command) {
            case PLAY_ANIMATION:
                drone.playAnimation(anim, duration);
                break;
            case PLAY_LED:
                drone.playLED(led, frequency, duration);
                break;
            case CLEAR_EMERGENCY:
                drone.clearEmergencySignal();
                break;
            case TRIM:
                drone.trim();
                break;
            case TAKEOFF:
                drone.takeOff();
                break;
            case LAND:
                drone.land();
                break;
            case RESET:
                drone.clearEmergencySignal();
                drone.trim();
                break;
            case VIDEO_CYCLE:
                cycleVideoChannel(drone);
                break;
            case FRONTAL_CAM:
                drone.selectVideoChannel(ARDrone.VideoChannel.VERTICAL_ONLY);
                break;
            case BOTTOM_CAM:
                drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
                break;
            case BOTTOM_CAM_SMALL:
                drone.selectVideoChannel(ARDrone.VideoChannel.VERTICAL_IN_HORIZONTAL);
                break;
            case FRONTAL_CAM_SMALL:
                drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_IN_VERTICAL);
                break;
            case TAKE_SNAPSHOT:
             
                break;
            case RECORD_VIDEO:
               
                break;
        }
    }

    
    private void cycleVideoChannel(ARDrone drone) throws IOException {
        if (++video_index == VIDEO_CYCLE.length) {
            video_index = 0;
        }
        drone.selectVideoChannel(VIDEO_CYCLE[video_index]);
    }

    public ControllerButton getButton() {
        return button;
    }

    public Command getCommand() {
        return command;
    }

    public Animation getAnim() {
        return anim;
    }

    public LED getLed() {
        return led;
    }

    public ControllerAxis getControlAxis() {
        return controlAxis;
    }

    public DroneAxis getDroneAxis() {
        return droneAxis;
    }

    public float getFrequency() {
        return frequency;
    }

    public int getDuration() {
        return duration;
    }

    public int getDelay() {
        return delay;
    }
}