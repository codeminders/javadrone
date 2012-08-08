package com.codeminders.controltower.config;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.Animation;
import com.codeminders.ardrone.ARDrone.LED;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.util.FileImageRecorder;
import com.codeminders.ardrone.util.FileVideoRecorder;
import com.codeminders.ardrone.util.RecordingSuccessCallback;

import java.io.*;
import java.util.logging.Level;

import org.apache.log4j.Logger;

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
    private String prefString;
    private static final VideoChannel[] VIDEO_CYCLE = {VideoChannel.HORIZONTAL_ONLY,
        VideoChannel.VERTICAL_ONLY, VideoChannel.VERTICAL_IN_HORIZONTAL, VideoChannel.HORIZONTAL_IN_VERTICAL};
    private int video_index = 0;
    private File recFile;
    private FileImageRecorder fir;
    private FileVideoRecorder fvr;

    /**
     * Creates the control from a string that is stored in the java preferences of this app
     * @param prefString 
     */
    public AssignableControl(String prefString) {
        String[] strings = prefString.split("/");
        if (strings.length < 3) {
            throw new IllegalStateException("preference string malformed");
        }
        button = ControllerButton.valueOf(strings[0]);
        command = Command.valueOf(strings[1]);
        delay = Integer.parseInt(strings[2]);
        switch (command) {
            case PLAY_ANIMATION:
                anim = Animation.valueOf(strings[3]);
                duration = Integer.parseInt(strings[4]);
                break;
            case PLAY_LED:
                led = LED.valueOf(strings[3]);
                frequency = Float.parseFloat(strings[4]);
                duration = Integer.parseInt(strings[5]);
                break;
            case RECORD_VIDEO:
            case TAKE_SNAPSHOT:
                try {
                    recFile = new File(strings[3].replace('?', File.separatorChar));
                } catch (Exception e) {
                }
                break;

        }
        this.prefString = prefString;
    }

    public AssignableControl(ControllerButton button, LED led, int delay, float frequency, int duration) {
        this.command = Command.PLAY_LED;
        this.delay = delay;
        this.led = led;
        this.frequency = frequency;
        this.duration = duration;
        prefString = button.name() + "/" + command.name() + "/" + delay + "/" + led.name() + "/" + frequency + "/" + duration;
    }

    public AssignableControl(ControllerButton button, Animation anim, int delay, int duration) {
        this.command = Command.PLAY_ANIMATION;
        this.delay = delay;
        this.anim = anim;
        this.duration = duration;
        prefString = button.name() + "/" + command.name() + "/" + delay + "/" + anim.name() + "/" + duration;
    }

    public AssignableControl(ControllerButton button, Command command, int delay, File file) {
        this.command = command;
        this.delay = delay;
        this.recFile = file;
        prefString = button.name() + "/" + command.name() + "/" + delay + "/" + file.getPath().replace(File.separatorChar, '?');
    }

    public AssignableControl(ControllerButton button, Command command, int delay) {
        this.command = command;
        this.delay = delay;
        prefString = button.name() + "/" + command.name() + "/" + delay;
    }

    /**
     * Sends the command to the supplied drone
     * @param drone
     * @throws IOException 
     */
    public void sendToDrone(final ARDrone drone) throws IOException {
        switch (command) {
            case PLAY_ANIMATION:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending animation command");
                drone.playAnimation(anim, duration);
                break;
            case PLAY_LED:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending LED command");
                drone.playLED(led, frequency, duration);
                break;
            case CLEAR_EMERGENCY:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending clear emergency");
                drone.clearEmergencySignal();
                break;
            case TRIM:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending trim");
                drone.trim();
                break;
            case TAKEOFF:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending takeoff");
                drone.takeOff();
                break;
            case LAND:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending land");
                drone.land();
                break;
            case RESET:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending reset");
                drone.clearEmergencySignal();
                drone.trim();
                break;
            case VIDEO_CYCLE:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending video cycle");
                cycleVideoChannel(drone);
                break;
            case FRONTAL_CAM:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending front cam");
                drone.selectVideoChannel(ARDrone.VideoChannel.VERTICAL_ONLY);
                break;
            case BOTTOM_CAM:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending bottom cam");
                drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
                break;
            case BOTTOM_CAM_SMALL:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending bottom cam small");
                drone.selectVideoChannel(ARDrone.VideoChannel.VERTICAL_IN_HORIZONTAL);
                break;
            case FRONTAL_CAM_SMALL:
                Logger.getLogger(AssignableControl.class.getName()).debug("Sending front cam small");
                drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_IN_VERTICAL);
                break;
            case TAKE_SNAPSHOT:
                Logger.getLogger(AssignableControl.class.getName()).debug("Take snapshot");
                takeSnapshot(drone);
                break;
            case RECORD_VIDEO:
                Logger.getLogger(AssignableControl.class.getName()).debug("Record video");
                recordVideo(drone);
                break;
        }
    }

    private synchronized void takeSnapshot(final ARDrone drone) {
        if (fir == null) {
            fir = new FileImageRecorder(recFile, 0, "SNAPSHOT-", new RecordingSuccessCallback() {

                @Override
                public void recordingSuccess(String filename) {
                    try {
                        AudioPlayer.playResource(this.getClass(), "/com/codeminders/controltower/sounds/camera.aif");
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(AssignableControl.class.getName()).log(Level.SEVERE, "{0}", ex);
                    }
                }

                @Override
                public void recordingError(String filename, String err, Throwable ex) {
                }
            });
            drone.addImageListener(fir);
        }
        fir.activate();
    }

    private synchronized void recordVideo(final ARDrone drone) {

        if (fvr == null) {
            fvr = new FileVideoRecorder(recFile, 0, "VIDEO-", new RecordingSuccessCallback() {

                @Override
                public void recordingSuccess(String filename) {
                    try {
                    	AudioPlayer.playResource(this.getClass(), "/com/codeminders/controltower/sounds/rec_stop.aif");
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(AssignableControl.class.getName()).log(Level.SEVERE, "{0}", ex);
                    }
                }

                @Override
                public void recordingError(String filename, String err, Throwable ex) {
                }
            }, 20);
            drone.addImageListener(fvr);
            fvr.startRecording();
            try {
            	AudioPlayer.playResource(this.getClass(), "/com/codeminders/controltower/sounds/rec_start.aif");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(AssignableControl.class.getName()).log(Level.SEVERE, "{0}", ex);
            }
        } else {
            fvr.finishRecording();
            fvr = null;
        }
    }

    /**
     * Used for VIDEO_CYCLE commands to cycle the video channel
     * @param drone
     * @throws IOException 
     */
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

    public File getRecFile() {
        return recFile;
    }

    /**
     * Gets the complete data of this object as a string for storing into java preferences
     * @return 
     */
    public String getPrefString() {
        return prefString;
    }
}