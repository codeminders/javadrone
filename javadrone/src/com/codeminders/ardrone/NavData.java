package com.codeminders.ardrone;

public class NavData
{
    public static enum Mode { BOOTSTRAP, DEMO }
    public static enum ControlAlgorithm { EULER_ANGELS_CONTROL, ANGULAR_SPEED_CONTROL }

    protected Mode mode;

    // state flags
    protected boolean          flying;
    protected boolean          videoEnabled;
    protected boolean          visionEnabled;
    protected ControlAlgorithm controlAlgorithm;
    protected boolean          altitudeControlActive;
    protected boolean          userFeedbackOn; ///TODO better name
    protected boolean          controlReceived;
    protected boolean          trimReceived;
    protected boolean          trimRunning;
    protected boolean          trimSucceeded;
    protected boolean          navDataDemoOnly;
    protected boolean          navDataBootstrap;
    protected boolean          motorsDown;
    protected boolean          gyrometersDown;
    protected boolean          batteryTooLow;
    protected boolean          batteryTooHigh;
    protected boolean          timerElapsed;
    protected boolean          notEnoughPower;
    protected boolean          angelsOutOufRange;
    protected boolean          tooMuchWind;
    protected boolean          ultrasonicSensorDeaf;
    protected boolean          cutoutSystemDetected;
    protected boolean          PICVersionNumberOK;
    protected boolean          ATCodedThreadOn;
    protected boolean          navDataThreadOn;
    protected boolean          videoThreadOn;
    protected boolean          acquisitionThreadOn;
    protected boolean          controlWatchdogDelayed;
    protected boolean          ADCWatchdogDelayed;
    protected boolean          communicationProblemOccurred;
    protected boolean          emergency;


    protected int sequence;

    public boolean isVideoEnabled()
    {
        return videoEnabled;
    }

    public boolean isFlying()
    {
        return flying;
    }

    public Mode getMode()
    {
        return mode;
    }

    public boolean isTrimReceived()
    {
        return trimReceived;
    }

    public boolean isTrimRunning()
    {
        return trimRunning;
    }

    public boolean isTrimSucceeded()
    {
        return trimSucceeded;
    }

    public boolean isBatteryTooLow()
    {
        return batteryTooLow;
    }

    public boolean isEmergency()
    {
        return emergency;
    }

    public boolean isVisionEnabled()
    {
        return visionEnabled;
    }

    public ControlAlgorithm getControlAlgorithm()
    {
        return controlAlgorithm;
    }

    public boolean isAltitudeControlActive()
    {
        return altitudeControlActive;
    }

    public boolean isUserFeedbackOn()
    {
        return userFeedbackOn;
    }

    public boolean isControlReceived()
    {
        return controlReceived;
    }

    public boolean isNavDataDemoOnly()
    {
        return navDataDemoOnly;
    }

    public boolean isNavDataBootstrap()
    {
        return navDataBootstrap;
    }

    public boolean isMotorsDown()
    {
        return motorsDown;
    }

    public boolean isGyrometersDown()
    {
        return gyrometersDown;
    }

    public boolean isBatteryTooHigh()
    {
        return batteryTooHigh;
    }

    public boolean isTimerElapsed()
    {
        return timerElapsed;
    }

    public boolean isNotEnoughPower()
    {
        return notEnoughPower;
    }

    public boolean isAngelsOutOufRange()
    {
        return angelsOutOufRange;
    }

    public boolean isTooMuchWind()
    {
        return tooMuchWind;
    }

    public boolean isUltrasonicSensorDeaf()
    {
        return ultrasonicSensorDeaf;
    }

    public boolean isCutoutSystemDetected()
    {
        return cutoutSystemDetected;
    }

    public boolean isPICVersionNumberOK()
    {
        return PICVersionNumberOK;
    }

    public boolean isATCodedThreadOn()
    {
        return ATCodedThreadOn;
    }

    public boolean isNavDataThreadOn()
    {
        return navDataThreadOn;
    }

    public boolean isVideoThreadOn()
    {
        return videoThreadOn;
    }

    public boolean isAcquisitionThreadOn()
    {
        return acquisitionThreadOn;
    }

    public boolean isControlWatchdogDelayed()
    {
        return controlWatchdogDelayed;
    }

    public boolean isADCWatchdogDelayed()
    {
        return ADCWatchdogDelayed;
    }

    public boolean isCommunicationProblemOccurred()
    {
        return communicationProblemOccurred;
    }

    public int getSequence()
    {
        return sequence;
    }

    private static void parseState(NavData data, int state)
    {
        data.flying                       = (state & 1) != 0;
        data.videoEnabled                 = (state & (1 << 1)) != 0;
        data.visionEnabled                = (state & (1 << 2)) != 0;
        data.controlAlgorithm             = (state & (1 << 3)) != 0 ?
            ControlAlgorithm.ANGULAR_SPEED_CONTROL : ControlAlgorithm.EULER_ANGELS_CONTROL;
        data.altitudeControlActive        = (state & (1 << 4)) != 0;
        data.userFeedbackOn               = (state & (1 << 5)) != 0;
        data.controlReceived              = (state & (1 << 6)) != 0;
        data.trimReceived                 = (state & (1 << 7)) != 0;
        data.trimRunning                  = (state & (1 << 8)) != 0;
        data.trimSucceeded                = (state & (1 << 9)) != 0;
        data.navDataDemoOnly              = (state & (1 << 10)) != 0;
        data.navDataBootstrap             = (state & (1 << 11)) != 0;
        data.motorsDown                   = (state & (1 << 12)) != 0;
        data.gyrometersDown               = (state & (1 << 14)) != 0;
        data.batteryTooLow                = (state & (1 << 15)) != 0;
        data.batteryTooHigh               = (state & (1 << 16)) != 0;
        data.timerElapsed                 = (state & (1 << 17)) != 0;
        data.notEnoughPower               = (state & (1 << 18)) != 0;
        data.angelsOutOufRange            = (state & (1 << 19)) != 0;
        data.tooMuchWind                  = (state & (1 << 20)) != 0;
        data.ultrasonicSensorDeaf         = (state & (1 << 21)) != 0;
        data.cutoutSystemDetected         = (state & (1 << 22)) != 0;
        data.PICVersionNumberOK           = (state & (1 << 23)) != 0;
        data.ATCodedThreadOn              = (state & (1 << 24)) != 0;
        data.navDataThreadOn              = (state & (1 << 25)) != 0;
        data.videoThreadOn                = (state & (1 << 26)) != 0;
        data.acquisitionThreadOn          = (state & (1 << 27)) != 0;
        data.controlWatchdogDelayed       = (state & (1 << 28)) != 0;
        data.ADCWatchdogDelayed           = (state & (1 << 29)) != 0;
        data.communicationProblemOccurred = (state & (1 << 30)) != 0;
        data.emergency                    = (state & (1 << 31)) != 0;
    }

    public static NavData createFromData(byte[] buf)
    {
        NavData data = new NavData();

        System.err.println("NavData packet received. len: " + buf.length);
        int offset = 0;

        data.mode = (buf.length == 24) ? NavData.Mode.BOOTSTRAP : NavData.Mode.DEMO;
        System.err.print("Mode: " + data.getMode() + " ");

        int header = byteArrayToInt(buf, offset);
        offset += 4;

        int state = byteArrayToInt(buf, offset);
        offset += 4;

        data.sequence = byteArrayToInt(buf, offset);
        offset += 4;
        System.err.print("Sequence: " + data.getSequence() + " ");

        int vision = byteArrayToInt(buf, offset);
        offset += 4;
        System.err.print("Vision: 0x" + Integer.toHexString(vision) + " ");

        parseState(data, state);

        //System.err.println();
        //printState(data);

        System.err.println();

        /*
        int i = 0;
        for(byte b : buf)
        {
            System.err.print("0x" + Integer.toHexString((int)b) + " ");
            if(++i % 24 == 0)
                System.err.println();
        }
        System.err.println();
        System.err.println();
        */
        //TODO: calculate checksum

        return data;
    }

    @SuppressWarnings("unused")
    private static void printState(NavData data)
    {
        System.err.println("IsFlying: " + data.isFlying());
        System.err.println("IsVideoEnabled: " + data.isVideoEnabled());
        System.err.println("IsVisionEnabled: " + data.isVisionEnabled());
        System.err.println("controlAlgo: " + data.getControlAlgorithm());
        System.err.println("AltitudeControlActive: " + data.isAltitudeControlActive());
        System.err.println("IsUserFeedbackOn: " + data.isUserFeedbackOn());
        System.err.println("ControlReceived: " + data.isVideoEnabled());
        System.err.println("IsTrimReceived: " + data.isTrimReceived());
        System.err.println("IsTrimRunning: " + data.isTrimRunning());
        System.err.println("IsTrimSucceeded: " + data.isTrimSucceeded());
        System.err.println("IsNavDataDemoOnly: " + data.isNavDataDemoOnly());
        System.err.println("IsNavDataBootstrap: " + data.isNavDataBootstrap());
        System.err.println("IsMotorsDown: " + data.isMotorsDown());
        System.err.println("IsGyrometersDown: " + data.isGyrometersDown());
        System.err.println("IsBatteryLow: " + data.isBatteryTooLow());
        System.err.println("IsBatteryHigh: " + data.isBatteryTooHigh());
        System.err.println("IsTimerElapsed: " + data.isTimerElapsed());
        System.err.println("isNotEnoughPower: " + data.isNotEnoughPower());
        System.err.println("isAngelsOutOufRange: " + data.isAngelsOutOufRange());
        System.err.println("isTooMuchWind: " + data.isTooMuchWind());
        System.err.println("isUltrasonicSensorDeaf: " + data.isUltrasonicSensorDeaf());
        System.err.println("isCutoutSystemDetected: " + data.isCutoutSystemDetected());
        System.err.println("isPICVersionNumberOK: " + data.isPICVersionNumberOK());
        System.err.println("isATCodedThreadOn: " + data.isATCodedThreadOn());
        System.err.println("isNavDataThreadOn: " + data.isNavDataThreadOn());
        System.err.println("isVideoThreadOn: " + data.isVideoThreadOn());
        System.err.println("isAcquisitionThreadOn: " + data.isAcquisitionThreadOn());
        System.err.println("isControlWatchdogDelayed: " + data.isControlWatchdogDelayed());
        System.err.println("isADCWatchdogDelayed: " + data.isADCWatchdogDelayed());
        System.err.println("isCommunicationProblemOccurred: " + data.isCommunicationProblemOccurred());
        System.err.println("IsEmergency: " + data.isEmergency());
    }


    /**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b)
    {
        return byteArrayToInt(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b, int offset)
    {
        int value = 0;
        for (int i = 3; i >= 0; i--) {
            int shift = i * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The short
     */
    public static int byteArrayToShort(byte[] b, int offset)
    {
        return b[offset]*256 + b[offset+1];
    }



// Define masks for ARDrone state
// 31 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
//  x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x x -> state
//  | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | |
//  | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | FLY MASK : (0) ardrone is landed, (1) ardrone is flying
//  | | | | | | | | | | | | | | | | | | | | | | | | | | | | | | VIDEO MASK : (0) video disable, (1) video enable
//  | | | | | | | | | | | | | | | | | | | | | | | | | | | | | VISION MASK : (0) vision disable, (1) vision enable
//  | | | | | | | | | | | | | | | | | | | | | | | | | | | | CONTROL ALGO : (0) euler angles control, (1) angular speed control
//  | | | | | | | | | | | | | | | | | | | | | | | | | | | ALTITUDE CONTROL ALGO : (0) altitude control inactive (1) altitude control active
//  | | | | | | | | | | | | | | | | | | | | | | | | | | USER feedback : Start button state
//  | | | | | | | | | | | | | | | | | | | | | | | | | Control command ACK : (0) None, (1) one received
//  | | | | | | | | | | | | | | | | | | | | | | | | Trim command ACK : (0) None, (1) one received
//  | | | | | | | | | | | | | | | | | | | | | | | Trim running : (0) none, (1) running
//  | | | | | | | | | | | | | | | | | | | | | | Trim result : (0) failed, (1) succeeded
//  | | | | | | | | | | | | | | | | | | | | | Navdata demo : (0) All navdata, (1) only navdata demo
//  | | | | | | | | | | | | | | | | | | | | Navdata bootstrap : (0) options sent in all or demo mode, (1) no navdata options sent
//  | | | | | | | | | | | | | | | | | | | | Motors status : (0) Ok, (1) Motors Com is down
//  | | | | | | | | | | | | | | | | | |
//  | | | | | | | | | | | | | | | | | Bit means that there's an hardware problem with gyrometers
//  | | | | | | | | | | | | | | | | VBat low : (1) too low, (0) Ok
//  | | | | | | | | | | | | | | | VBat high (US mad) : (1) too high, (0) Ok
//  | | | | | | | | | | | | | | Timer elapsed : (1) elapsed, (0) not elapsed
//  | | | | | | | | | | | | | Power : (0) Ok, (1) not enough to fly
//  | | | | | | | | | | | | Angles : (0) Ok, (1) out of range
//  | | | | | | | | | | | Wind : (0) Ok, (1) too much to fly
//  | | | | | | | | | | Ultrasonic sensor : (0) Ok, (1) deaf
//  | | | | | | | | | Cutout system detection : (0) Not detected, (1) detected
//  | | | | | | | | PIC Version number OK : (0) a bad version number, (1) version number is OK
//  | | | | | | | ATCodec thread ON : (0) thread OFF (1) thread ON
//  | | | | | | Navdata thread ON : (0) thread OFF (1) thread ON
//  | | | | | Video thread ON : (0) thread OFF (1) thread ON
//  | | | | Acquisition thread ON : (0) thread OFF (1) thread ON
//  | | | CTRL watchdog : (1) delay in control execution (> 5ms), (0) control is well scheduled // Check frequency of control loop
//  | | ADC Watchdog : (1) delay in uart2 dsr (> 5ms), (0) uart2 is good // Check frequency of uart2 dsr (com with adc)
//  | Communication Watchdog : (1) com problem, (0) Com is ok // Check if we have an active connection with a client
//  Emergency landing : (0) no emergency, (1) emergency

}
